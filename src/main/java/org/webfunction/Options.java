package org.webfunction;

import java.net.http.HttpClient;

/**
 * Configures a {@link Client} at construction time. All settings are
 * optional; build one with a fluent chain, e.g.:
 * <pre>{@code
 * var options = new Options().bearerAuth("token").version("2");
 * }</pre>
 */
public final class Options {
    private String bearerAuth;
    private String version;
    private boolean pipelined;
    private HttpClient httpClient;

    /** Sent as "Authorization: Bearer &lt;token&gt;" on every call. */
    public Options bearerAuth(String bearerAuth) {
        this.bearerAuth = bearerAuth;
        return this;
    }

    /**
     * Sent as the "Api-Version" header (or, for {@link
     * Client#fromUrl}, as an "api_version" query parameter) on every
     * call.
     */
    public Options version(String version) {
        this.version = version;
        return this;
    }

    /**
     * If true, batches calls into a single HTTP request instead of
     * executing them immediately - see {@link Client#setPipeline} and
     * {@link Pipeline}. Only takes effect if the package declares a
     * pipeline URL; otherwise the client behaves as if false.
     */
    public Options pipelined(boolean pipelined) {
        this.pipelined = pipelined;
        return this;
    }

    /**
     * Used instead of a default {@link HttpClient} for every request
     * this client makes (including fetching the package itself, and
     * pipeline execution).
     */
    public Options httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    String bearerAuthValue() {
        return bearerAuth;
    }

    String versionValue() {
        return version;
    }

    boolean pipelinedValue() {
        return pipelined;
    }

    HttpClient httpClientValue() {
        return httpClient;
    }
}
