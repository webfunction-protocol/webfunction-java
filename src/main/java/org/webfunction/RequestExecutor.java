package org.webfunction;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Low-level request execution, invoking a single Web Function endpoint
 * URL directly via HTTP, without needing a {@link Package}. Mirrors the
 * Ruby reference's low-level {@code WebFunction::Request.execute}.
 *
 * <p>Not paginated result-wrapping - callers that know an endpoint is
 * paginated (via {@link Endpoint#paginated()}) wrap the result
 * themselves; see {@link Client#call} for the normal path.
 */
final class RequestExecutor {

    /** Sent in the User-Agent header, mirroring the other reference clients' "webfunction/&lt;version&gt;". */
    static final String MODULE_VERSION = "0.1.0";

    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.newHttpClient();

    private RequestExecutor() {
    }

    /** Configures a single low-level request. */
    record Options(String bearerAuth, String version, Map<String, Object> args, HttpClient httpClient) {

        Options {
            args = args == null ? Map.of() : args;
        }

        static Options none() {
            return new Options(null, null, Map.of(), null);
        }

        HttpClient client() {
            return httpClient != null ? httpClient : DEFAULT_HTTP_CLIENT;
        }

        /**
         * Builds the standard request headers as a plain string map -
         * used both for the real HTTP request and for a pipeline step's
         * "headers" field, which the pipeline endpoint expects as plain
         * JSON.
         */
        Map<String, String> headerMap() {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("User-Agent", "webfunction-java/" + MODULE_VERSION);
            headers.put("Accept-Encoding", "gzip");
            if (bearerAuth != null && !bearerAuth.isEmpty()) {
                headers.put("Authorization", "Bearer " + bearerAuth);
            }
            if (version != null && !version.isEmpty()) {
                headers.put("Api-Version", version);
            }
            return headers;
        }
    }

    /**
     * Invokes {@code url} directly via HTTP POST.
     *
     * <p>Returns the decoded JSON response value ({@code Map<String,
     * Object>}, {@code List<Object>}, {@code String}, a {@link Number}
     * subtype, {@link Boolean}, or {@code null}) on success (status 200).
     * Throws {@link BadRequestException} on a 400, {@link
     * JsonParseException} if the body isn't valid JSON, or {@link
     * UnexpectedStatusCodeException} for any other status.
     */
    static Object execute(String url, Options opts) {
        String body;
        try {
            body = Json.MAPPER.writeValueAsString(opts.args());
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("encoding request body", e);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body));
        opts.headerMap().forEach(builder::header);

        HttpResponse<byte[]> resp;
        try {
            resp = opts.client().send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("executing request to " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted while executing request to " + url, e);
        }

        byte[] rawBytes = readResponseBody(resp);
        String rawBody = new String(rawBytes, java.nio.charset.StandardCharsets.UTF_8);

        int status = resp.statusCode();
        if (status != 200 && status != 400) {
            throw new UnexpectedStatusCodeException(status, rawBody);
        }

        Object result = null;
        if (rawBytes.length > 0) {
            try {
                result = Json.MAPPER.readValue(rawBytes, Object.class);
            } catch (IOException e) {
                throw new JsonParseException(e.getMessage(), status, rawBody);
            }
        }

        if (status == 400) {
            String code = "WFN_BAD_REQUEST_ERROR";
            String message = "Bad request";
            Object details = Map.of("body", result == null ? Map.of() : result);

            if (result instanceof List<?> triple && triple.size() == 3
                    && triple.get(0) instanceof String c && triple.get(1) instanceof String m) {
                code = c;
                message = m;
                details = triple.get(2);
            }

            throw new BadRequestException(code, message, details);
        }

        return result;
    }

    /**
     * Reads the response body, transparently gunzipping it if the
     * server compressed it (Content-Encoding: gzip).
     *
     * <p>Unlike Go's net/http (which auto-decompresses gzip responses
     * <i>unless</i> the caller sets its own Accept-Encoding header - a
     * real bug hit and fixed in webfunction-go), Java's {@link
     * HttpClient} never auto-decompresses gzip responses under any
     * circumstances, regardless of who set the header. So this
     * decompression is always needed here, unconditionally - not only
     * as a fix for a specific header-interaction quirk.
     */
    static byte[] readResponseBody(HttpResponse<byte[]> resp) {
        byte[] raw = resp.body();
        boolean gzipped = resp.headers().firstValue("Content-Encoding")
                .map(v -> v.equalsIgnoreCase("gzip"))
                .orElse(false);
        if (!gzipped) {
            return raw;
        }
        try (InputStream gz = new GZIPInputStream(new java.io.ByteArrayInputStream(raw));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            gz.transferTo(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("decompressing gzip response", e);
        }
    }
}
