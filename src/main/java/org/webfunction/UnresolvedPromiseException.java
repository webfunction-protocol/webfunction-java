package org.webfunction;

import java.util.Map;

/**
 * Thrown by {@link Promise#value()} when a pipelined call's result is
 * read before the owning {@link Pipeline} has been executed.
 */
public class UnresolvedPromiseException extends WebFunctionException {
    public UnresolvedPromiseException(String path) {
        super(
                "WFN_UNRESOLVED_PROMISE_ERROR",
                "promise value read before the pipeline was executed",
                Map.of("path", path)
        );
    }
}
