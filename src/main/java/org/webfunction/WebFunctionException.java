package org.webfunction;

/**
 * Base class for every exception this library throws. Unchecked
 * ({@code extends RuntimeException}) - a deliberate choice discussed up
 * front: modern Java style, less call-site ceremony, at the cost of errors
 * being able to propagate silently if a caller doesn't think to catch
 * them.
 *
 * <p>Mirrors the base {@code WebFunction::Error} class in the Ruby
 * reference client, which every raised error inherits from.
 */
public class WebFunctionException extends RuntimeException {

    private final String code;
    private final transient Object details;

    /**
     * @param code    a machine-readable error code
     * @param message a human-readable description
     * @param details additional structured context - kind-specific, see
     *                each subclass
     */
    public WebFunctionException(String code, String message, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    /** The machine-readable error code. */
    public String getCode() {
        return code;
    }

    /** Additional structured context - kind-specific, see each subclass. */
    public Object getDetails() {
        return details;
    }
}
