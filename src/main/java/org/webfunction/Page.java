package org.webfunction;

import java.net.http.HttpClient;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds one page of results from a paginated endpoint.
 *
 * <p>Paginated responses follow the Web Function pagination contract: a
 * JSON object with "page", "next", and "previous" keys. The "next" and
 * "previous" values are opaque request bodies - call {@link #nextPage()}
 * or {@link #previousPage()} to fetch the adjacent page; never construct
 * or inspect them yourself.
 *
 * <p>Wrapping here is triggered by the endpoint's declared "paginated"
 * flag (see {@link Endpoint#paginated()}), not by sniffing the response
 * shape the way the Ruby/JS/PHP reference clients do - a deliberate,
 * ecosystem-wide deviation first made for webfunction-go.
 *
 * <p>Implements {@link Iterable} so a page can be used directly in a
 * for-each loop, alongside {@link #getItems()} for parity with the other
 * reference clients' accessor-style API.
 *
 * @see <a href="https://webfunction.org/pagination">webfunction.org/pagination</a>
 */
public final class Page implements Iterable<Object> {

    private final List<Object> items;
    private final Map<String, Object> next;
    private final Map<String, Object> previous;

    private final String url;
    private final String bearerAuth;
    private final String version;
    private final HttpClient httpClient;

    private Page(List<Object> items, Map<String, Object> next, Map<String, Object> previous,
                 String url, String bearerAuth, String version, HttpClient httpClient) {
        this.items = List.copyOf(items);
        this.next = next;
        this.previous = previous;
        this.url = url;
        this.bearerAuth = bearerAuth;
        this.version = version;
        this.httpClient = httpClient;
    }

    /** The items on the current page. */
    public List<Object> getItems() {
        return items;
    }

    /** Reports whether a next page is available. */
    public boolean hasNext() {
        return next != null;
    }

    /** Reports whether a previous page is available. */
    public boolean hasPrevious() {
        return previous != null;
    }

    /**
     * Fetches the next page by posting the opaque "next" body back to the
     * same endpoint URL. Empty if there is no next page.
     */
    public Optional<Page> nextPage() {
        return fetch(next);
    }

    /**
     * Fetches the previous page by posting the opaque "previous" body
     * back to the same endpoint URL. Empty if there is no previous page.
     */
    public Optional<Page> previousPage() {
        return fetch(previous);
    }

    @Override
    public Iterator<Object> iterator() {
        return items.iterator();
    }

    private Optional<Page> fetch(Map<String, Object> body) {
        if (body == null) {
            return Optional.empty();
        }
        Object result = RequestExecutor.execute(url, new RequestExecutor.Options(bearerAuth, version, body, httpClient));
        return Optional.of(wrap(result, url, bearerAuth, version, httpClient));
    }

    /**
     * Wraps a decoded response in a {@link Page}. Used by {@link
     * Client#call} for endpoints flagged as paginated.
     *
     * @throws IllegalStateException if the response doesn't actually
     *                                match the {@code {page, next,
     *                                previous}} contract shape - which,
     *                                since wrapping is flag-driven rather
     *                                than shape-sniffed, signals a real
     *                                mismatch between the endpoint's
     *                                declared flag and its actual
     *                                response worth surfacing rather
     *                                than silently ignoring.
     */
    @SuppressWarnings("unchecked")
    static Page wrap(Object result, String url, String bearerAuth, String version, HttpClient httpClient) {
        if (!(result instanceof Map<?, ?> obj)) {
            throw new IllegalStateException(
                    "endpoint is flagged paginated but response was not an object (got "
                            + (result == null ? "null" : result.getClass()) + ")");
        }

        Object rawItems = obj.get("page");
        if (rawItems == null && !obj.containsKey("page")) {
            throw new IllegalStateException("endpoint is flagged paginated but response has no \"page\" key");
        }
        if (!(rawItems instanceof List<?> items)) {
            throw new IllegalStateException(
                    "endpoint is flagged paginated but \"page\" is not an array (got "
                            + (rawItems == null ? "null" : rawItems.getClass()) + ")");
        }

        Map<String, Object> next = asOpaqueBody(obj, "next");
        Map<String, Object> previous = asOpaqueBody(obj, "previous");

        return new Page((List<Object>) items, next, previous, url, bearerAuth, version, httpClient);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asOpaqueBody(Map<?, ?> obj, String key) {
        Object raw = obj.get(key);
        if (raw == null) {
            return null;
        }
        if (!(raw instanceof Map<?, ?>)) {
            throw new IllegalStateException(
                    "paginated response's \"" + key + "\" key is not an object or null (got " + raw.getClass() + ")");
        }
        return (Map<String, Object>) raw;
    }
}
