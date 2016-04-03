
package es.berry.restyle.specification.generated;

import java.util.HashMap;
import java.util.LinkedHashSet;
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
    "with",
    "type",
    "min",
    "max",
    "embed",
    "onDelete",
    "onUpdate",
    "fields",
    "check",
    "index"
})
public class Relation {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("with")
    private String with;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private Relation.Type type;
    @JsonProperty("min")
    private Long min;
    @JsonProperty("max")
    private Long max;
    @JsonProperty("embed")
    private Boolean embed;
    @JsonProperty("onDelete")
    private String onDelete;
    @JsonProperty("onUpdate")
    private String onUpdate;
    @JsonProperty("fields")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Field> fields = new LinkedHashSet<Field>();
    @JsonProperty("check")
    private String check;
    @JsonProperty("index")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> index = new LinkedHashSet<String>();
    protected final static Object NOT_FOUND_VALUE = new Object();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The with
     */
    @JsonProperty("with")
    public String getWith() {
        return with;
    }

    /**
     * 
     * (Required)
     * 
     * @param with
     *     The with
     */
    @JsonProperty("with")
    public void setWith(String with) {
        this.with = with;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public Relation.Type getType() {
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
    public void setType(Relation.Type type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The min
     */
    @JsonProperty("min")
    public Long getMin() {
        return min;
    }

    /**
     * 
     * @param min
     *     The min
     */
    @JsonProperty("min")
    public void setMin(Long min) {
        this.min = min;
    }

    /**
     * 
     * @return
     *     The max
     */
    @JsonProperty("max")
    public Long getMax() {
        return max;
    }

    /**
     * 
     * @param max
     *     The max
     */
    @JsonProperty("max")
    public void setMax(Long max) {
        this.max = max;
    }

    /**
     * 
     * @return
     *     The embed
     */
    @JsonProperty("embed")
    public Boolean getEmbed() {
        return embed;
    }

    /**
     * 
     * @param embed
     *     The embed
     */
    @JsonProperty("embed")
    public void setEmbed(Boolean embed) {
        this.embed = embed;
    }

    /**
     * 
     * @return
     *     The onDelete
     */
    @JsonProperty("onDelete")
    public String getOnDelete() {
        return onDelete;
    }

    /**
     * 
     * @param onDelete
     *     The onDelete
     */
    @JsonProperty("onDelete")
    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
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
     *     The fields
     */
    @JsonProperty("fields")
    public Set<Field> getFields() {
        return fields;
    }

    /**
     * 
     * @param fields
     *     The fields
     */
    @JsonProperty("fields")
    public void setFields(Set<Field> fields) {
        this.fields = fields;
    }

    /**
     * 
     * @return
     *     The check
     */
    @JsonProperty("check")
    public String getCheck() {
        return check;
    }

    /**
     * 
     * @param check
     *     The check
     */
    @JsonProperty("check")
    public void setCheck(String check) {
        this.check = check;
    }

    /**
     * 
     * @return
     *     The index
     */
    @JsonProperty("index")
    public Set<String> getIndex() {
        return index;
    }

    /**
     * 
     * @param index
     *     The index
     */
    @JsonProperty("index")
    public void setIndex(Set<String> index) {
        this.index = index;
    }

    protected boolean declaredProperty(String name, Object value) {
        if ("with".equals(name)) {
            if (value instanceof String) {
                setWith(((String) value));
            } else {
                throw new IllegalArgumentException(("property \"with\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
            }
            return true;
        } else {
            if ("type".equals(name)) {
                if (value instanceof Relation.Type) {
                    setType(((Relation.Type) value));
                } else {
                    throw new IllegalArgumentException(("property \"type\" is of type \"es.berry.restyle.specification.generated.Relation.Type\", but got "+ value.getClass().toString()));
                }
                return true;
            } else {
                if ("min".equals(name)) {
                    if (value instanceof Long) {
                        setMin(((Long) value));
                    } else {
                        throw new IllegalArgumentException(("property \"min\" is of type \"java.lang.Long\", but got "+ value.getClass().toString()));
                    }
                    return true;
                } else {
                    if ("max".equals(name)) {
                        if (value instanceof Long) {
                            setMax(((Long) value));
                        } else {
                            throw new IllegalArgumentException(("property \"max\" is of type \"java.lang.Long\", but got "+ value.getClass().toString()));
                        }
                        return true;
                    } else {
                        if ("embed".equals(name)) {
                            if (value instanceof Boolean) {
                                setEmbed(((Boolean) value));
                            } else {
                                throw new IllegalArgumentException(("property \"embed\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                            }
                            return true;
                        } else {
                            if ("onDelete".equals(name)) {
                                if (value instanceof String) {
                                    setOnDelete(((String) value));
                                } else {
                                    throw new IllegalArgumentException(("property \"onDelete\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
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
                                    if ("fields".equals(name)) {
                                        if (value instanceof Set) {
                                            setFields(((Set<Field> ) value));
                                        } else {
                                            throw new IllegalArgumentException(("property \"fields\" is of type \"java.util.Set<es.berry.restyle.specification.generated.Field>\", but got "+ value.getClass().toString()));
                                        }
                                        return true;
                                    } else {
                                        if ("check".equals(name)) {
                                            if (value instanceof String) {
                                                setCheck(((String) value));
                                            } else {
                                                throw new IllegalArgumentException(("property \"check\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                                            }
                                            return true;
                                        } else {
                                            if ("index".equals(name)) {
                                                if (value instanceof Set) {
                                                    setIndex(((Set<String> ) value));
                                                } else {
                                                    throw new IllegalArgumentException(("property \"index\" is of type \"java.util.Set<java.lang.String>\", but got "+ value.getClass().toString()));
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

    protected Object declaredPropertyOrNotFound(String name, Object notFoundValue) {
        if ("with".equals(name)) {
            return getWith();
        } else {
            if ("type".equals(name)) {
                return getType();
            } else {
                if ("min".equals(name)) {
                    return getMin();
                } else {
                    if ("max".equals(name)) {
                        return getMax();
                    } else {
                        if ("embed".equals(name)) {
                            return getEmbed();
                        } else {
                            if ("onDelete".equals(name)) {
                                return getOnDelete();
                            } else {
                                if ("onUpdate".equals(name)) {
                                    return getOnUpdate();
                                } else {
                                    if ("fields".equals(name)) {
                                        return getFields();
                                    } else {
                                        if ("check".equals(name)) {
                                            return getCheck();
                                        } else {
                                            if ("index".equals(name)) {
                                                return getIndex();
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

    @SuppressWarnings({
        "unchecked"
    })
    public<T >T get(String name) {
        Object value = declaredPropertyOrNotFound(name, Relation.NOT_FOUND_VALUE);
        if (Relation.NOT_FOUND_VALUE!= value) {
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
    public enum Type {

        HAS_ONE("hasOne"),
        HAS_MANY("hasMany");
        private final String value;
        private final static Map<String, Relation.Type> CONSTANTS = new HashMap<String, Relation.Type>();

        static {
            for (Relation.Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Type(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static Relation.Type fromValue(String value) {
            Relation.Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
