package es.berry.restyle.generators;

import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.utils.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Class with generic information about AngularJS, potentially reusable by other plugins with similar targets.
 */
public class AngularJsHelper {

    /**
     * Calculate the best HTML input control attributes for the given resource's field, based on the type and
     * constraints of the latter.
     */
    public static String getHtmlInputAttrs(Field f) {
        final List<String> attrs = new ArrayList<>();

        if (f.getEncrypted())
            attrs.add("type=password");
        else if (f.getType().equals(Field.Type.INT))
            attrs.add("type=number");
        // else: "text", as the HTML default

        if (f.getReadOnly())
            attrs.add("disabled"); // readonly?

        // Not wanted in this case: we are setting things up for partial updates (PATCHs) only
//        if (f.getRequired())
//            attrs.add("required");

        if (f.getMin() != null) {
            if (f.getType().equals(Field.Type.INT))
                attrs.add("min=" + f.getMin());
        }

        if (f.getMax() != null) {
            if (f.getType().equals(Field.Type.INT))
                attrs.add("max=" + f.getMax());
            else if (f.getType().equals(Field.Type.STRING))
                attrs.add("maxlength=" + f.getMax());
        }

        if (f.getPattern() != null)
            attrs.add("pattern=" + f.getPattern());

        return Strings.join(attrs, " ");
    }
}
