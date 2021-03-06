
package es.berry.restyle.specification.generated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "description",
    "type",
    "autoIncrement",
    "required",
    "min",
    "max",
    "enum",
    "precision",
    "default",
    "onUpdate",
    "unique",
    "pattern",
    "patternOptions",
    "filterable",
    "sortable",
    "readOnly",
    "writeOnly",
    "encrypted"
})
public class Type {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private Field.Type type;
    @JsonProperty("autoIncrement")
    private Boolean autoIncrement;
    @JsonProperty("required")
    private Boolean required;
    @JsonProperty("min")
    private Object min;
    @JsonProperty("max")
    private Object max;
    @JsonProperty("enum")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Object> _enum = new LinkedHashSet<Object>();
    @JsonProperty("precision")
    private List<Long> precision = new ArrayList<Long>();
    @JsonProperty("default")
    private Object _default;
    @JsonProperty("onUpdate")
    private String onUpdate;
    @JsonProperty("unique")
    private Boolean unique;
    @JsonProperty("pattern")
    private String pattern;
    @JsonProperty("patternOptions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> patternOptions = new LinkedHashSet<String>();
    @JsonProperty("filterable")
    private Boolean filterable;
    @JsonProperty("sortable")
    private Boolean sortable;
    @JsonProperty("readOnly")
    private Boolean readOnly;
    @JsonProperty("writeOnly")
    private Boolean writeOnly;
    @JsonProperty("encrypted")
    private Boolean encrypted;
    protected final static Object NOT_FOUND_VALUE = new Object();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public Field.Type getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(Field.Type type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The autoIncrement
     */
    @JsonProperty("autoIncrement")
    public Boolean getAutoIncrement() {
        return autoIncrement;
    }

    /**
     * 
     * @param autoIncrement
     *     The autoIncrement
     */
    @JsonProperty("autoIncrement")
    public void setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    /**
     * 
     * @return
     *     The required
     */
    @JsonProperty("required")
    public Boolean getRequired() {
        return required;
    }

    /**
     * 
     * @param required
     *     The required
     */
    @JsonProperty("required")
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * 
     * @return
     *     The min
     */
    @JsonProperty("min")
    public Object getMin() {
        return min;
    }

    /**
     * 
     * @param min
     *     The min
     */
    @JsonProperty("min")
    public void setMin(Object min) {
        this.min = min;
    }

    /**
     * 
     * @return
     *     The max
     */
    @JsonProperty("max")
    public Object getMax() {
        return max;
    }

    /**
     * 
     * @param max
     *     The max
     */
    @JsonProperty("max")
    public void setMax(Object max) {
        this.max = max;
    }

    /**
     * 
     * @return
     *     The _enum
     */
    @JsonProperty("enum")
    public Set<Object> getEnum() {
        return _enum;
    }

    /**
     * 
     * @param _enum
     *     The enum
     */
    @JsonProperty("enum")
    public void setEnum(Set<Object> _enum) {
        this._enum = _enum;
    }

    /**
     * 
     * @return
     *     The precision
     */
    @JsonProperty("precision")
    public List<Long> getPrecision() {
        return precision;
    }

    /**
     * 
     * @param precision
     *     The precision
     */
    @JsonProperty("precision")
    public void setPrecision(List<Long> precision) {
        this.precision = precision;
    }

    /**
     * 
     * @return
     *     The _default
     */
    @JsonProperty("default")
    public Object getDefault() {
        return _default;
    }

    /**
     * 
     * @param _default
     *     The default
     */
    @JsonProperty("default")
    public void setDefault(Object _default) {
        this._default = _default;
    }

    /**
     * 
     * @return
     *     The onUpdate
     */
    @JsonProperty("onUpdate")
    public String getOnUpdate() {
        return onUpdate;
    }

    /**
     * 
     * @param onUpdate
     *     The onUpdate
     */
    @JsonProperty("onUpdate")
    public void setOnUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
    }

    /**
     * 
     * @return
     *     The unique
     */
    @JsonProperty("unique")
    public Boolean getUnique() {
        return unique;
    }

    /**
     * 
     * @param unique
     *     The unique
     */
    @JsonProperty("unique")
    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    /**
     * 
     * @return
     *     The pattern
     */
    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

    /**
     * 
     * @param pattern
     *     The pattern
     */
    @JsonProperty("pattern")
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 
     * @return
     *     The patternOptions
     */
    @JsonProperty("patternOptions")
    public Set<String> getPatternOptions() {
        return patternOptions;
    }

    /**
     * 
     * @param patternOptions
     *     The patternOptions
     */
    @JsonProperty("patternOptions")
    public void setPatternOptions(Set<String> patternOptions) {
        this.patternOptions = patternOptions;
    }

    /**
     * 
     * @return
     *     The filterable
     */
    @JsonProperty("filterable")
    public Boolean getFilterable() {
        return filterable;
    }

    /**
     * 
     * @param filterable
     *     The filterable
     */
    @JsonProperty("filterable")
    public void setFilterable(Boolean filterable) {
        this.filterable = filterable;
    }

    /**
     * 
     * @return
     *     The sortable
     */
    @JsonProperty("sortable")
    public Boolean getSortable() {
        return sortable;
    }

    /**
     * 
     * @param sortable
     *     The sortable
     */
    @JsonProperty("sortable")
    public void setSortable(Boolean sortable) {
        this.sortable = sortable;
    }

    /**
     * 
     * @return
     *     The readOnly
     */
    @JsonProperty("readOnly")
    public Boolean getReadOnly() {
        return readOnly;
    }

    /**
     * 
     * @param readOnly
     *     The readOnly
     */
    @JsonProperty("readOnly")
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * 
     * @return
     *     The writeOnly
     */
    @JsonProperty("writeOnly")
    public Boolean getWriteOnly() {
        return writeOnly;
    }

    /**
     * 
     * @param writeOnly
     *     The writeOnly
     */
    @JsonProperty("writeOnly")
    public void setWriteOnly(Boolean writeOnly) {
        this.writeOnly = writeOnly;
    }

    /**
     * 
     * @return
     *     The encrypted
     */
    @JsonProperty("encrypted")
    public Boolean getEncrypted() {
        return encrypted;
    }

    /**
     * 
     * @param encrypted
     *     The encrypted
     */
    @JsonProperty("encrypted")
    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    protected boolean declaredProperty(String name, Object value) {
        if ("name".equals(name)) {
            if (value instanceof String) {
                setName(((String) value));
            } else {
                throw new IllegalArgumentException(("property \"name\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
            }
            return true;
        } else {
            if ("description".equals(name)) {
                if (value instanceof String) {
                    setDescription(((String) value));
                } else {
                    throw new IllegalArgumentException(("property \"description\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                }
                return true;
            } else {
                if ("type".equals(name)) {
                    if (value instanceof Field.Type) {
                        setType(((Field.Type) value));
                    } else {
                        throw new IllegalArgumentException(("property \"type\" is of type \"es.berry.restyle.specification.generated.Field.Type\", but got "+ value.getClass().toString()));
                    }
                    return true;
                } else {
                    if ("autoIncrement".equals(name)) {
                        if (value instanceof Boolean) {
                            setAutoIncrement(((Boolean) value));
                        } else {
                            throw new IllegalArgumentException(("property \"autoIncrement\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                        }
                        return true;
                    } else {
                        if ("required".equals(name)) {
                            if (value instanceof Boolean) {
                                setRequired(((Boolean) value));
                            } else {
                                throw new IllegalArgumentException(("property \"required\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                            }
                            return true;
                        } else {
                            if ("min".equals(name)) {
                                if (value instanceof Object) {
                                    setMin(((Object) value));
                                } else {
                                    throw new IllegalArgumentException(("property \"min\" is of type \"java.lang.Object\", but got "+ value.getClass().toString()));
                                }
                                return true;
                            } else {
                                if ("max".equals(name)) {
                                    if (value instanceof Object) {
                                        setMax(((Object) value));
                                    } else {
                                        throw new IllegalArgumentException(("property \"max\" is of type \"java.lang.Object\", but got "+ value.getClass().toString()));
                                    }
                                    return true;
                                } else {
                                    if ("enum".equals(name)) {
                                        if (value instanceof Set) {
                                            setEnum(((Set<Object> ) value));
                                        } else {
                                            throw new IllegalArgumentException(("property \"enum\" is of type \"java.util.Set<java.lang.Object>\", but got "+ value.getClass().toString()));
                                        }
                                        return true;
                                    } else {
                                        if ("precision".equals(name)) {
                                            if (value instanceof List) {
                                                setPrecision(((List<Long> ) value));
                                            } else {
                                                throw new IllegalArgumentException(("property \"precision\" is of type \"java.util.List<java.lang.Long>\", but got "+ value.getClass().toString()));
                                            }
                                            return true;
                                        } else {
                                            if ("default".equals(name)) {
                                                if (value instanceof Object) {
                                                    setDefault(((Object) value));
                                                } else {
                                                    throw new IllegalArgumentException(("property \"default\" is of type \"java.lang.Object\", but got "+ value.getClass().toString()));
                                                }
                                                return true;
                                            } else {
                                                if ("onUpdate".equals(name)) {
                                                    if (value instanceof String) {
                                                        setOnUpdate(((String) value));
                                                    } else {
                                                        throw new IllegalArgumentException(("property \"onUpdate\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                                                    }
                                                    return true;
                                                } else {
                                                    if ("unique".equals(name)) {
                                                        if (value instanceof Boolean) {
                                                            setUnique(((Boolean) value));
                                                        } else {
                                                            throw new IllegalArgumentException(("property \"unique\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                                        }
                                                        return true;
                                                    } else {
                                                        if ("pattern".equals(name)) {
                                                            if (value instanceof String) {
                                                                setPattern(((String) value));
                                                            } else {
                                                                throw new IllegalArgumentException(("property \"pattern\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                                                            }
                                                            return true;
                                                        } else {
                                                            if ("patternOptions".equals(name)) {
                                                                if (value instanceof Set) {
                                                                    setPatternOptions(((Set<String> ) value));
                                                                } else {
                                                                    throw new IllegalArgumentException(("property \"patternOptions\" is of type \"java.util.Set<java.lang.String>\", but got "+ value.getClass().toString()));
                                                                }
                                                                return true;
                                                            } else {
                                                                if ("filterable".equals(name)) {
                                                                    if (value instanceof Boolean) {
                                                                        setFilterable(((Boolean) value));
                                                                    } else {
                                                                        throw new IllegalArgumentException(("property \"filterable\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                                                    }
                                                                    return true;
                                                                } else {
                                                                    if ("sortable".equals(name)) {
                                                                        if (value instanceof Boolean) {
                                                                            setSortable(((Boolean) value));
                                                                        } else {
                                                                            throw new IllegalArgumentException(("property \"sortable\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                                                        }
                                                                        return true;
                                                                    } else {
                                                                        if ("readOnly".equals(name)) {
                                                                            if (value instanceof Boolean) {
                                                                                setReadOnly(((Boolean) value));
                                                                            } else {
                                                                                throw new IllegalArgumentException(("property \"readOnly\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                                                            }
                                                                            return true;
                                                                        } else {
                                                                            if ("writeOnly".equals(name)) {
                                                                                if (value instanceof Boolean) {
                                                                                    setWriteOnly(((Boolean) value));
                                                                                } else {
                                                                                    throw new IllegalArgumentException(("property \"writeOnly\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                                                                }
                                                                                return true;
                                                                            } else {
                                                                                if ("encrypted".equals(name)) {
                                                                                    if (value instanceof Boolean) {
                                                                                        setEncrypted(((Boolean) value));
                                                                                    } else {
                                                                                        throw new IllegalArgumentException(("property \"encrypted\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                                                                    }
                                                                                    return true;
                                                                                } else {
                                                                                    return false;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected Object declaredPropertyOrNotFound(String name, Object notFoundValue) {
        if ("name".equals(name)) {
            return getName();
        } else {
            if ("description".equals(name)) {
                return getDescription();
            } else {
                if ("type".equals(name)) {
                    return getType();
                } else {
                    if ("autoIncrement".equals(name)) {
                        return getAutoIncrement();
                    } else {
                        if ("required".equals(name)) {
                            return getRequired();
                        } else {
                            if ("min".equals(name)) {
                                return getMin();
                            } else {
                                if ("max".equals(name)) {
                                    return getMax();
                                } else {
                                    if ("enum".equals(name)) {
                                        return getEnum();
                                    } else {
                                        if ("precision".equals(name)) {
                                            return getPrecision();
                                        } else {
                                            if ("default".equals(name)) {
                                                return getDefault();
                                            } else {
                                                if ("onUpdate".equals(name)) {
                                                    return getOnUpdate();
                                                } else {
                                                    if ("unique".equals(name)) {
                                                        return getUnique();
                                                    } else {
                                                        if ("pattern".equals(name)) {
                                                            return getPattern();
                                                        } else {
                                                            if ("patternOptions".equals(name)) {
                                                                return getPatternOptions();
                                                            } else {
                                                                if ("filterable".equals(name)) {
                                                                    return getFilterable();
                                                                } else {
                                                                    if ("sortable".equals(name)) {
                                                                        return getSortable();
                                                                    } else {
                                                                        if ("readOnly".equals(name)) {
                                                                            return getReadOnly();
                                                                        } else {
                                                                            if ("writeOnly".equals(name)) {
                                                                                return getWriteOnly();
                                                                            } else {
                                                                                if ("encrypted".equals(name)) {
                                                                                    return getEncrypted();
                                                                                } else {
                                                                                    return notFoundValue;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({
        "unchecked"
    })
    public<T >T get(String name) {
        Object value = declaredPropertyOrNotFound(name, Type.NOT_FOUND_VALUE);
        if (Type.NOT_FOUND_VALUE!= value) {
            return ((T) value);
        } else {
            throw new IllegalArgumentException((("property \""+ name)+"\" is not defined"));
        }
    }

    public void set(String name, Object value) {
        if (!declaredProperty(name, value)) {
            throw new IllegalArgumentException((("property \""+ name)+"\" is not defined"));
        }
    }

    @Generated("org.jsonschema2pojo")
    public enum __ShouldNotBeUsedTypeEnum__ {

        INT("int"),
        FLOAT("float"),
        DECIMAL("decimal"),
        STRING("string"),
        BOOL("bool"),
        DATE("date"),
        TIME("time"),
        DATETIME("datetime"),
        FILE("file");
        private final String value;
        private final static Map<String, Field.Type> CONSTANTS = new HashMap<String, Field.Type>();

        static {
            if (true) {
                
            }
        }

        private __ShouldNotBeUsedTypeEnum__(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static Field.Type fromValue(String value) {
            Field.Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
