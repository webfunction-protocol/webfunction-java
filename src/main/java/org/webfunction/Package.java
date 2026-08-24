package org.webfunction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * The top-level Web Function package definition.
 * See <a href="https://webfunction.org/package#package-definition">webfunction.org/package#package-definition</a>.
 *
 * <p>Named {@code Package} to match every other reference client in this
 * suite - shadows {@code java.lang.Package} within this library's own
 * source, which is harmless since nothing here needs the JDK's Package
 * type, but worth knowing if you extend this class elsewhere.
 *
 * <p>Deliberately has no {@code eventSourceUrl}/{@code events} fields.
 * See webfunction-go's equivalent note: these appear in an earlier,
 * unconfirmed model but nowhere in the Ruby reference client, and aren't
 * used by any of webfunction-js/webfunction-php either.
 */
public record Package(
        String baseUrl,
        String pipelineUrl,
        String name,
        List<String> flags,
        String version,
        List<String> versions,
        String docs,
        List<Endpoint> endpoints,
        List<ErrorDef> errors,
        List<ObjectSchema> objects
) {

    @JsonCreator
    public Package(
            @JsonProperty("base_url") String baseUrl,
            @JsonProperty("pipeline_url") String pipelineUrl,
            @JsonProperty("name") String name,
            @JsonProperty("flags") List<String> flags,
            @JsonProperty("version") String version,
            @JsonProperty("versions") List<String> versions,
            @JsonProperty("docs") String docs,
            @JsonProperty("endpoints") List<Endpoint> endpoints,
            @JsonProperty("errors") List<ErrorDef> errors,
            @JsonProperty("objects") List<ObjectSchema> objects
    ) {
        this.baseUrl = baseUrl;
        this.pipelineUrl = pipelineUrl;
        this.name = name;
        this.flags = flags == null ? List.of() : List.copyOf(flags);
        this.version = version;
        this.versions = versions == null ? List.of() : List.copyOf(versions);
        this.docs = docs;
        this.endpoints = endpoints == null ? List.of() : List.copyOf(endpoints);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
        this.objects = objects == null ? List.of() : List.copyOf(objects);
    }

    /** Reports whether the package declares the "versioned" flag. */
    public boolean versioned() {
        return Flags.has(flags, "versioned");
    }

    /**
     * Returns the named endpoint, if the package has one by that name.
     * Matches both hyphenated ("find-user") and underscored
     * ("find_user") forms, mirroring the Ruby reference's
     * {@code Package#endpoint}.
     */
    public Optional<Endpoint> endpoint(String name) {
        String dashed = Flags.dashify(name);
        return endpoints.stream().filter(e -> e.name().equals(dashed)).findFirst();
    }

    /**
     * Returns the named object definition, if the package has one by
     * that name. This is the raw lookup with no context filtering - see
     * {@link #objectInContext} for the context-aware version.
     */
    public Optional<ObjectSchema> object(String name) {
        return objects.stream().filter(o -> o.name().equals(name)).findFirst();
    }

    /** Selects which member set an {@link #objectInContext} lookup resolves to. */
    public enum ObjectContext {
        /** Selects an object's arguments - used when the object is referenced as an argument's type. */
        ARGUMENT,
        /** Selects an object's attributes - used when the object is referenced as an endpoint's return type or an attribute's type. */
        ATTRIBUTE
    }

    /**
     * Looks up a named object definition, resolved for the given
     * context. Mirrors the Ruby reference's {@code Package#object(name,
     * context:)}: an object MAY define both arguments and attributes,
     * since it can be referenced in both contexts across a package, so
     * the caller must say which set applies. Returns empty if the object
     * doesn't exist, or defines no members for the requested context.
     */
    public Optional<ObjectSchema> objectInContext(String name, ObjectContext ctx) {
        return object(name).filter(obj -> switch (ctx) {
            case ARGUMENT -> !obj.arguments().isEmpty();
            case ATTRIBUTE -> !obj.attributes().isEmpty();
        });
    }

    /** Returns the named package-level error definition, if present. */
    public Optional<ErrorDef> error(String code) {
        return errors.stream().filter(e -> e.code().equals(code)).findFirst();
    }
}
