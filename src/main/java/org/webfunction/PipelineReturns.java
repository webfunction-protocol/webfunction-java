package org.webfunction;

/**
 * Selects what a {@link Pipeline#execute} call returns and which
 * promises it fills in. Use {@link #ALL}, {@link #LAST}, or {@link
 * #ofPath(String)}.
 */
public final class PipelineReturns {
    private final String raw;

    private PipelineReturns(String raw) {
        this.raw = raw;
    }

    /**
     * Returns every step's result as an array and fills in every
     * promise from the batch. This is the default per the reference
     * spec.
     */
    public static final PipelineReturns ALL = new PipelineReturns("all");

    /** Returns only the final step's result and fills in only the last promise. */
    public static final PipelineReturns LAST = new PipelineReturns("last");

    /**
     * Returns the value at the given JSONPath expression, passed through
     * verbatim for the server to resolve. No promises are filled in
     * automatically for this mode.
     */
    public static PipelineReturns ofPath(String jsonpath) {
        return new PipelineReturns(jsonpath);
    }

    String raw() {
        return raw;
    }
}
