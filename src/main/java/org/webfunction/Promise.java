package org.webfunction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

/**
 * Stands in for a pipelined call's result before the pipeline has been
 * executed. Ported from the Ruby reference's {@code Promise}.
 *
 * <p>Simplification versus the Ruby original: Ruby's {@code Promise#[]}
 * behaves differently once resolved (it indexes into the real,
 * already-decoded value) versus unresolved (it extends the JSONPath).
 * This port's {@link #field}/{@link #index} always just extend the path,
 * regardless of resolution state - digging into an already-resolved
 * value's real structure would need reflection-heavy navigation of a
 * decoded {@code Object} value, which isn't implemented. Call {@link
 * #value()} or {@link #resolve()} to get the real value once you know
 * the pipeline has run.
 */
@JsonSerialize(using = Promise.Serializer.class)
public final class Promise {

    private final Pipeline pipeline;
    private final Path path;
    private Object value;
    private boolean resolved;

    Promise(Pipeline pipeline, Path path) {
        this.pipeline = pipeline;
        this.path = path;
    }

    /** Returns a new Promise scoped one level deeper, at the named field. */
    public Promise field(String name) {
        return new Promise(pipeline, path.field(name));
    }

    /** Returns a new Promise scoped one level deeper, at the given array index. */
    public Promise index(int i) {
        return new Promise(pipeline, path.index(i));
    }

    /** The promise's JSONPath string. */
    public String path() {
        return path.toString();
    }

    /**
     * Returns the promise's resolved value.
     *
     * @throws UnresolvedPromiseException if the pipeline hasn't been executed yet
     */
    public Object value() {
        if (!resolved) {
            throw new UnresolvedPromiseException(path.toString());
        }
        return value;
    }

    /**
     * Executes the owning pipeline if the promise isn't already
     * resolved, then returns the value.
     */
    public Object resolve() {
        if (!resolved) {
            pipeline.execute(PipelineReturns.ALL);
        }
        return value();
    }

    void markResolved(Object value) {
        this.resolved = true;
        this.value = value;
    }

    boolean isResolved() {
        return resolved;
    }

    /**
     * Serializes the promise as its resolved value if resolved, or as
     * its plain JSONPath string otherwise - matching the wire
     * representation an unresolved pipeline reference takes when
     * embedded as an argument to a later step. Per the pipelining spec,
     * a literal argument string that happens to start with "$" must
     * itself be escaped as "\$" so it isn't mistaken for a promise
     * reference - that escaping is the caller's responsibility for plain
     * string args, since a Promise value here is always an intentional
     * reference, never a literal.
     */
    static final class Serializer extends JsonSerializer<Promise> {
        @Override
        public void serialize(Promise promise, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (promise.resolved) {
                gen.writeObject(promise.value);
            } else {
                gen.writeString(promise.path.toString());
            }
        }
    }
}
