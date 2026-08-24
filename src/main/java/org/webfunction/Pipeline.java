package org.webfunction;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Batches several endpoint calls into a single HTTP request. The server
 * runs them in order; a step's arguments can reference an earlier step's
 * not-yet-known result via a {@link Promise}. Ported from the Ruby
 * reference's {@code Pipeline}.
 */
public final class Pipeline {

    /** One queued step, matching the wire shape {@code {url, headers, body}}. */
    private record Step(String url, Map<String, String> headers, Map<String, Object> body) {
    }

    private final String url;
    private final HttpClient httpClient;

    private final List<Step> steps = new ArrayList<>();
    private final List<Promise> promises = new ArrayList<>();

    /** Creates a Pipeline that executes against the given pipeline URL (a package's pipelineUrl). */
    public Pipeline(String url) {
        this(url, null);
    }

    Pipeline(String url, HttpClient httpClient) {
        this.url = url;
        this.httpClient = httpClient;
    }

    /** Appends a step and returns a Promise standing in for its eventual result. */
    Promise addStep(String stepUrl, Map<String, String> headers, Map<String, Object> body) {
        int n = promises.size();
        Promise promise = new Promise(this, new Path("$[" + n + "]"));

        steps.add(new Step(stepUrl, headers, body));
        promises.add(promise);

        return promise;
    }

    /**
     * Runs every step added since the last {@link #execute} (or since
     * the pipeline was created), in one HTTP request, and resets the
     * pipeline for the next batch.
     */
    public Object execute(PipelineReturns returns) {
        String returnsArg = switch (returns.raw()) {
            case "all" -> "$";
            case "last" -> "$[-1:]";
            default -> returns.raw();
        };

        List<Step> batchSteps = List.copyOf(steps);
        List<Promise> batchPromises = List.copyOf(promises);
        reset();

        Object result = RequestExecutor.execute(url, new RequestExecutor.Options(
                null, null,
                Map.of("steps", batchSteps, "returns", returnsArg),
                httpClient
        ));

        switch (returns.raw()) {
            case "all" -> {
                if (result instanceof List<?> arr) {
                    for (int i = 0; i < arr.size() && i < batchPromises.size(); i++) {
                        batchPromises.get(i).markResolved(arr.get(i));
                    }
                }
            }
            case "last" -> {
                if (!batchPromises.isEmpty()) {
                    batchPromises.get(batchPromises.size() - 1).markResolved(result);
                }
            }
            default -> {
                // A custom JSONPath return: the server-side semantics
                // aren't generic enough to know which promise(s), if
                // any, that path corresponds to, so nothing is
                // auto-resolved for this mode - matches the Ruby
                // reference and the Go/PHP/JS ports.
            }
        }

        return result;
    }

    private void reset() {
        steps.clear();
        promises.clear();
    }
}
