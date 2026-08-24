package org.webfunction;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
     *
     * <p>FAIL_ON_UNKNOWN_PROPERTIES is disabled: a real production
     * package (api.reservepay.com) was found to send an undocumented
     * "hint" field on Argument that doesn't appear in the Ruby reference
     * client, the webfunction.org spec text this model was built from,
     * or any other client in this suite. Rather than hard-fail on
     * fields this model doesn't yet know about - which would make every
     * future protocol addition a breaking change for existing consumers
     * - unknown fields are silently ignored. See Argument's doc comment
     * for the open question of what "hint" actually is.
     */
    static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}