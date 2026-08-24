package org.webfunction;

import java.util.Map;

/**
 * Thrown when a response body (200 or 400) is not valid JSON.
 * {@link #getDetails()} holds "status_code" and "raw_body".
 */
public class JsonParseException extends WebFunctionException {
    public JsonParseException(String message, int statusCode, String rawBody) {
        super(
                "WFN_JSON_PARSE_ERROR",
                message,
                Map.of("status_code", statusCode, "raw_body", rawBody)
        );
    }
}
