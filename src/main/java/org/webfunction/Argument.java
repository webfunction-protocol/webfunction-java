package org.webfunction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Describes a single argument accepted by an endpoint.
 * See <a href="https://webfunction.org/package#argument-definition">webfunction.org/package#argument-definition</a>.
 */
public record Argument(String name, Type type, String group, List<Object> choices, List<String> flags, String docs) {

    /**
     * Explicit, annotated canonical constructor - deliberately not
     * relying on Jackson's automatic record-parameter-name detection
     * combined with the snake_case naming strategy, which was found to
     * throw an illegal-field-access error on Jackson 2.14 (it fell back
     * to setting the record's final fields directly via reflection
     * instead of using the constructor). Explicit @JsonProperty names
     * sidestep that regardless of Jackson version.
     */
    @JsonCreator
    public Argument(
            @JsonProperty("name") String name,
            @JsonProperty("type") Type type,
            @JsonProperty("group") String group,
            @JsonProperty("choices") List<Object> choices,
            @JsonProperty("flags") List<String> flags,
            @JsonProperty("docs") String docs
    ) {
        this.name = name;
        this.type = type;
        this.group = group;
        this.choices = choices == null ? List.of() : List.copyOf(choices);
        this.flags = flags == null ? List.of() : List.copyOf(flags);
        this.docs = docs;
    }

    /** Reports whether the argument declares the "required" flag. */
    public boolean required() {
        return flags.contains("required");
    }

    /** Reports whether the argument is not required. */
    public boolean optional() {
        return !required();
    }
}
