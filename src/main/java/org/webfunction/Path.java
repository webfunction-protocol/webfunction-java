package org.webfunction;

/**
 * An immutable JSONPath expression identifying a value within a
 * not-yet-resolved pipeline response. Ported from the Ruby reference's
 * {@code Promise::Path}.
 */
final class Path {
    private final String raw;

    Path(String raw) {
        this.raw = raw;
    }

    /** The path's JSONPath string, e.g. "$[0].id". */
    @Override
    public String toString() {
        return raw;
    }

    /** Returns a new Path one level deeper, at the named field. */
    Path field(String name) {
        return new Path(raw + "." + name);
    }

    /** Returns a new Path one level deeper, at the given array index. */
    Path index(int i) {
        return new Path(raw + "[" + i + "]");
    }
}
