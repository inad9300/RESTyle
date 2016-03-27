package es.berry.restyle.specification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Types {
    public final static String INT = "int";
    public final static String FLOAT = "float";
    public final static String DECIMAL = "decimal";
    public final static String STRING = "string";
    public final static String BOOL = "bool";
    public final static String DATE = "date";
    public final static String TIME = "time";
    public final static String DATETIME = "datetime";
    public final static String FILE = "file";

    public final static Set<String> ALL = new HashSet<String>(Arrays.asList(
            INT, FLOAT, DECIMAL, STRING, BOOL, DATE, TIME, DATETIME, FILE));
}
