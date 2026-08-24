package org.webfunction;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Shared, package-private Jackson configuration used throughout the library. */
final class Json {
    private Json() {
    }

    /**
     * The single {@link ObjectMapper} used everywhere in this library.
     * No naming-strategy configuration - every model record has its own
     * explicit {@code @JsonProperty} names (see e.g. {@link Package}),
     * deliberately not relying on Jackson's automatic snake_case-to-
     * camelCase reversal for record constructor parameters. That
     * combination was found to throw an illegal-field-access error on
     * Jackson 2.14 (it fell back to setting the record's final fields
     * directly via reflection instead of using the constructor) -
     * explicit property names sidestep the issue regardless of Jackson
     * version.
     */
    static final ObjectMapper MAPPER = new ObjectMapper();
}
