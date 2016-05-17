package es.berry.restyle.specification;

import es.berry.restyle.specification.generated.Field;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Set of primitive types allowed in the specification.
 */
public class Types {
    public final static String INT = Field.Type.INT.toString();
    public final static String FLOAT = Field.Type.FLOAT.toString();
    public final static String DECIMAL = Field.Type.DECIMAL.toString();
    public final static String STRING = Field.Type.STRING.toString();
    public final static String BOOL = Field.Type.BOOL.toString();
    public final static String DATE = Field.Type.DATE.toString();
    public final static String TIME = Field.Type.TIME.toString();
    public final static String DATETIME = Field.Type.DATETIME.toString();
    public final static String FILE = Field.Type.FILE.toString();

    public final static Set<String> ALL = new HashSet<>(Arrays.asList(
            INT, FLOAT, DECIMAL, STRING, BOOL, DATE, TIME, DATETIME, FILE));

    // Types whose min and max properties must be ints
    public final static Set<String> MIN_MAX_INT = new HashSet<>(Arrays.asList(
            STRING, INT, FLOAT, DECIMAL, FILE));

    // Types whose min and max properties must be strings
    public final static Set<String> MIN_MAX_STRING = new HashSet<>(Arrays.asList(
            DATE, TIME, DATETIME));
}
