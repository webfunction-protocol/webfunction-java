package org.webfunction;

/**
 * Thrown when the server responds with status 400.
 * See <a href="https://webfunction.org">webfunction.org</a>'s error-handling
 * section: a 400 body is either a JSON "error triple" ({@code [code,
 * message, details]}) or, failing that, treated as an opaque body under
 * the code "WFN_BAD_REQUEST_ERROR".
 */
public class BadRequestException extends WebFunctionException {
    public BadRequestException(String code, String message, Object details) {
        super(code, message, details);
    }
}
