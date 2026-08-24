package org.webfunction;

import java.util.List;

/** Small internal helpers shared by {@link Package} and {@link Endpoint}. */
final class Flags {
    private Flags() {
    }

    static boolean has(List<String> flags, String want) {
        return flags != null && flags.contains(want);
    }

    /**
     * Converts underscores to hyphens, so idiomatic Java-ish or Ruby/
     * Python-ish caller-supplied names ("find_user") match the
     * hyphenated endpoint names used on the wire ("find-user").
     */
    static String dashify(String name) {
        return name.replace('_', '-');
    }
}
