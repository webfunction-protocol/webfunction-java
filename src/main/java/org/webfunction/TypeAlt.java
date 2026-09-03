package org.webfunction;

/**
 * One alternative within a {@link Type}'s union.
 *
 * <p>Ported from webfunction-go's {@code TypeAlt}, itself cross-checked
 * against the Ruby reference client (github.com/webfunction-protocol/webfunction-ruby).
 *
 * @param base       one of: "object", "array", "string", "number",
 *                   "boolean", "null", or "any"
 * @param refinement the dotted suffix narrowing {@code base}, e.g.
 *                   "email" for "string.email", or the referenced
 *                   object's name for "object.&lt;n&gt;". {@code null}
 *                   when there's no refinement.
 * @param of         the element type, present only when {@code base}
 *                   equals "array" and the wire form was a nested array
 *                   (e.g. {@code [["string"]]} means "array of string").
 *                   {@code null} for a bare "array" entry (array of any).
 */
public record TypeAlt(String base, String refinement, Type of) {

    /**
     * Reports whether this alternative is a reference to a named object
     * definition (an "object.&lt;n&gt;" refinement).
     */
    public boolean isObjectRef() {
        return "object".equals(base) && refinement != null && !refinement.isEmpty();
    }

    @Override
    public String toString() {
        return format("default");
    }

    /**
     * Renders this alternative using one of three styles, mirroring the
     * Ruby reference's {@code Type#format(format)}:
     * <ul>
     *   <li>"default": the full dotted form, e.g. "string.email"</li>
     *   <li>"compact": refinement alone if present, else the base, e.g. "email"</li>
     *   <li>"base": the base type alone, e.g. "string"</li>
     * </ul>
     */
    public String format(String style) {
        return switch (style) {
            case "compact" -> refinement != null && !refinement.isEmpty() ? refinement : base;
            case "base" -> base;
            default -> {
                StringBuilder sb = new StringBuilder(base);
                if (refinement != null && !refinement.isEmpty()) {
                    sb.append('.').append(refinement);
                }
                if ("array".equals(base) && of != null) {
                    sb.append('<').append(of.format(style)).append('>');
                }
                yield sb.toString();
            }
        };
    }
}
