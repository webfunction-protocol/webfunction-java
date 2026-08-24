package org.webfunction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A named, reusable object type definition, referenced elsewhere in the
 * package as a refined "object.&lt;n&gt;" type.
 * See <a href="https://webfunction.org/package#object-definition">webfunction.org/package#object-definition</a>.
 *
 * <p>Per the spec, the same object name can carry two different member
 * lists depending on the context it's referenced from: {@code arguments}
 * when the object is referenced as an argument's type, {@code attributes}
 * when referenced as an endpoint's return type or an attribute's type. An
 * object MAY define both, if it's referenced in both contexts somewhere
 * in the package. See {@link Package#objectInContext} for context-aware
 * lookup.
 */
public record ObjectSchema(String name, List<Argument> arguments, List<Attribute> attributes) {

    @JsonCreator
    public ObjectSchema(
            @JsonProperty("name") String name,
            @JsonProperty("arguments") List<Argument> arguments,
            @JsonProperty("attributes") List<Attribute> attributes
    ) {
        this.name = name;
        this.arguments = arguments == null ? List.of() : List.copyOf(arguments);
        this.attributes = attributes == null ? List.of() : List.copyOf(attributes);
    }
}
