package org.webfunction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a single named ERROR_CODE a package or endpoint can return.
 * Named {@code ErrorDef} (not {@code Error}) to avoid colliding with
 * {@code java.lang.Error}.
 * See <a href="https://webfunction.org/package#error-definition">webfunction.org/package#error-definition</a>.
 */
public record ErrorDef(String code, String docs) {

    @JsonCreator
    public ErrorDef(@JsonProperty("code") String code, @JsonProperty("docs") String docs) {
        this.code = code;
        this.docs = docs;
    }
}
