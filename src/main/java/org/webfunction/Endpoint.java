package org.webfunction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * Describes a single callable endpoint within a package.
 * See <a href="https://webfunction.org/package#endpoint-definition">webfunction.org/package#endpoint-definition</a>.
 */
public record Endpoint(
        String name,
        Type returns,
        List<String> flags,
        String group,
        String docs,
        List<ErrorDef> errors,
        List<Argument> arguments,
        List<Attribute> attributes
) {

    @JsonCreator
    public Endpoint(
            @JsonProperty("name") String name,
            @JsonProperty("returns") Type returns,
            @JsonProperty("flags") List<String> flags,
            @JsonProperty("group") String group,
            @JsonProperty("docs") String docs,
            @JsonProperty("errors") List<ErrorDef> errors,
            @JsonProperty("arguments") List<Argument> arguments,
            @JsonProperty("attributes") List<Attribute> attributes
    ) {
        this.name = name;
        this.returns = returns;
        this.flags = flags == null ? List.of() : List.copyOf(flags);
        this.group = group;
        this.docs = docs;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
        this.arguments = arguments == null ? List.of() : List.copyOf(arguments);
        this.attributes = attributes == null ? List.of() : List.copyOf(attributes);
    }

    /**
     * Reports whether the endpoint declares the given flag (e.g.
     * "paginated", "bearer_auth", "private", "error_triple",
     * "capture_bearer").
     */
    public boolean hasFlag(String flag) {
        return Flags.has(flags, flag);
    }

    /**
     * Reports whether the endpoint declares the "paginated" flag.
     *
     * <p>Per an explicit ecosystem-wide decision (first made for
     * webfunction-go), pagination in this client is detected via this
     * flag, not by sniffing the response shape the way the Ruby
     * reference client does.
     */
    public boolean paginated() {
        return hasFlag("paginated");
    }

    /** Reports whether the endpoint requires a bearer token. */
    public boolean bearerAuth() {
        return hasFlag("bearer_auth");
    }

    /** Reports whether the endpoint is internal-only. */
    public boolean isPrivate() {
        return hasFlag("private");
    }

    /** Returns the named argument, if the endpoint has one by that name. */
    public Optional<Argument> argument(String name) {
        return arguments.stream().filter(a -> a.name().equals(name)).findFirst();
    }

    /** Returns the named returned attribute, if the endpoint has one by that name. */
    public Optional<Attribute> attribute(String name) {
        return attributes.stream().filter(a -> a.name().equals(name)).findFirst();
    }

    /** Returns the named endpoint-level error definition, if present. */
    public Optional<ErrorDef> error(String code) {
        return errors.stream().filter(e -> e.code().equals(code)).findFirst();
    }
}
