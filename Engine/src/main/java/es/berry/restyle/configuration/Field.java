package es.berry.restyle.configuration;

import com.fasterxml.jackson.databind.JsonNode;

public class Field {

    private String name;
    private String description;
    private Boolean required;
    private Boolean readOnly;
    private String type;
    private Integer min;
    private Integer max;
    private Object[] in;
    private Integer[] precision;
    private String defaultExpression;
    private Boolean unique;
    private String pattern;
    private Boolean filterable;
    private Boolean sortable;

    public Field(JsonNode fieldObject) {
        // TODO: initialize
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isRequired() {
        return required;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public String getType() {
        return type;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public Object[] getIn() {
        return in;
    }

    public Integer[] getPrecision() {
        return precision;
    }

    public String getDefaultExpression() {
        return defaultExpression;
    }

    public Boolean isUnique() {
        return unique;
    }

    public String getPattern() {
        return pattern;
    }

    public Boolean isFilterable() {
        return filterable;
    }

    public Boolean isSortable() {
        return sortable;
    }

}
