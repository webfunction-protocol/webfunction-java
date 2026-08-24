package org.webfunction;

import java.util.Map;

/**
 * Thrown when the server responds with any status other than 200 or 400.
 * {@link #getDetails()} holds the raw status code and response body
 * under the keys "status_code" and "raw_body".
 */
public class UnexpectedStatusCodeException extends WebFunctionException {
    public UnexpectedStatusCodeException(int statusCode, String rawBody) {
        super(
                "WFN_UNEXPECTED_STATUS_CODE_ERROR",
                "Unexpected status code (" + statusCode + ")",
                Map.of("status_code", statusCode, "raw_body", rawBody)
        );
    }
}
