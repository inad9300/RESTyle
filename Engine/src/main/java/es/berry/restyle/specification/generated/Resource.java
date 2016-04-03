
package es.berry.restyle.specification.generated;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "displayName",
    "plural",
    "description",
    "base",
    "abstract",
    "inheritanceStrategy",
    "paginable",
    "idInjection",
    "fields",
    "acceptExtraFields",
    "relations",
    "check",
    "index",
    "acl"
})
public class Resource {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("displayName")
    private String displayName;
    @JsonProperty("plural")
    private String plural;
    @JsonProperty("description")
    private String description;
    @JsonProperty("base")
    private String base;
    @JsonProperty("abstract")
    private Boolean _abstract;
    @JsonProperty("inheritanceStrategy")
    private String inheritanceStrategy;
    @JsonProperty("paginable")
    private Boolean paginable;
    @JsonProperty("idInjection")
    private Boolean idInjection;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fields")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Field> fields = new LinkedHashSet<Field>();
    @JsonProperty("acceptExtraFields")
    private Boolean acceptExtraFields;
    @JsonProperty("relations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Relation> relations = new LinkedHashSet<Relation>();
    @JsonProperty("check")
    private String check;
    @JsonProperty("index")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> index = new LinkedHashSet<String>();
    @JsonProperty("acl")
    private Object acl;
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
     *     The displayName
     */
    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 
     * @param displayName
     *     The displayName
     */
    @JsonProperty("displayName")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 
     * @return
     *     The plural
     */
    @JsonProperty("plural")
    public String getPlural() {
        return plural;
    }

    /**
     * 
     * @param plural
     *     The plural
     */
    @JsonProperty("plural")
    public void setPlural(String plural) {
        this.plural = plural;
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
     * @return
     *     The base
     */
    @JsonProperty("base")
    public String getBase() {
        return base;
    }

    /**
     * 
     * @param base
     *     The base
     */
    @JsonProperty("base")
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * 
     * @return
     *     The _abstract
     */
    @JsonProperty("abstract")
    public Boolean getAbstract() {
        return _abstract;
    }

    /**
     * 
     * @param _abstract
     *     The abstract
     */
    @JsonProperty("abstract")
    public void setAbstract(Boolean _abstract) {
        this._abstract = _abstract;
    }

    /**
     * 
     * @return
     *     The inheritanceStrategy
     */
    @JsonProperty("inheritanceStrategy")
    public String getInheritanceStrategy() {
        return inheritanceStrategy;
    }

    /**
     * 
     * @param inheritanceStrategy
     *     The inheritanceStrategy
     */
    @JsonProperty("inheritanceStrategy")
    public void setInheritanceStrategy(String inheritanceStrategy) {
        this.inheritanceStrategy = inheritanceStrategy;
    }

    /**
     * 
     * @return
     *     The paginable
     */
    @JsonProperty("paginable")
    public Boolean getPaginable() {
        return paginable;
    }

    /**
     * 
     * @param paginable
     *     The paginable
     */
    @JsonProperty("paginable")
    public void setPaginable(Boolean paginable) {
        this.paginable = paginable;
    }

    /**
     * 
     * @return
     *     The idInjection
     */
    @JsonProperty("idInjection")
    public Boolean getIdInjection() {
        return idInjection;
    }

    /**
     * 
     * @param idInjection
     *     The idInjection
     */
    @JsonProperty("idInjection")
    public void setIdInjection(Boolean idInjection) {
        this.idInjection = idInjection;
    }

    /**
     * 
     * (Required)
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
     * (Required)
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
     *     The acceptExtraFields
     */
    @JsonProperty("acceptExtraFields")
    public Boolean getAcceptExtraFields() {
        return acceptExtraFields;
    }

    /**
     * 
     * @param acceptExtraFields
     *     The acceptExtraFields
     */
    @JsonProperty("acceptExtraFields")
    public void setAcceptExtraFields(Boolean acceptExtraFields) {
        this.acceptExtraFields = acceptExtraFields;
    }

    /**
     * 
     * @return
     *     The relations
     */
    @JsonProperty("relations")
    public Set<Relation> getRelations() {
        return relations;
    }

    /**
     * 
     * @param relations
     *     The relations
     */
    @JsonProperty("relations")
    public void setRelations(Set<Relation> relations) {
        this.relations = relations;
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

    /**
     * 
     * @return
     *     The acl
     */
    @JsonProperty("acl")
    public Object getAcl() {
        return acl;
    }

    /**
     * 
     * @param acl
     *     The acl
     */
    @JsonProperty("acl")
    public void setAcl(Object acl) {
        this.acl = acl;
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
            if ("displayName".equals(name)) {
                if (value instanceof String) {
                    setDisplayName(((String) value));
                } else {
                    throw new IllegalArgumentException(("property \"displayName\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                }
                return true;
            } else {
                if ("plural".equals(name)) {
                    if (value instanceof String) {
                        setPlural(((String) value));
                    } else {
                        throw new IllegalArgumentException(("property \"plural\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
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
                        if ("base".equals(name)) {
                            if (value instanceof String) {
                                setBase(((String) value));
                            } else {
                                throw new IllegalArgumentException(("property \"base\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                            }
                            return true;
                        } else {
                            if ("abstract".equals(name)) {
                                if (value instanceof Boolean) {
                                    setAbstract(((Boolean) value));
                                } else {
                                    throw new IllegalArgumentException(("property \"abstract\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                }
                                return true;
                            } else {
                                if ("inheritanceStrategy".equals(name)) {
                                    if (value instanceof String) {
                                        setInheritanceStrategy(((String) value));
                                    } else {
                                        throw new IllegalArgumentException(("property \"inheritanceStrategy\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                                    }
                                    return true;
                                } else {
                                    if ("paginable".equals(name)) {
                                        if (value instanceof Boolean) {
                                            setPaginable(((Boolean) value));
                                        } else {
                                            throw new IllegalArgumentException(("property \"paginable\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                        }
                                        return true;
                                    } else {
                                        if ("idInjection".equals(name)) {
                                            if (value instanceof Boolean) {
                                                setIdInjection(((Boolean) value));
                                            } else {
                                                throw new IllegalArgumentException(("property \"idInjection\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
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
                                                if ("acceptExtraFields".equals(name)) {
                                                    if (value instanceof Boolean) {
                                                        setAcceptExtraFields(((Boolean) value));
                                                    } else {
                                                        throw new IllegalArgumentException(("property \"acceptExtraFields\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                                                    }
                                                    return true;
                                                } else {
                                                    if ("relations".equals(name)) {
                                                        if (value instanceof Set) {
                                                            setRelations(((Set<Relation> ) value));
                                                        } else {
                                                            throw new IllegalArgumentException(("property \"relations\" is of type \"java.util.Set<es.berry.restyle.specification.generated.Relation>\", but got "+ value.getClass().toString()));
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
                                                                if ("acl".equals(name)) {
                                                                    if (value instanceof Object) {
                                                                        setAcl(((Object) value));
                                                                    } else {
                                                                        throw new IllegalArgumentException(("property \"acl\" is of type \"java.lang.Object\", but got "+ value.getClass().toString()));
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

    protected Object declaredPropertyOrNotFound(String name, Object notFoundValue) {
        if ("name".equals(name)) {
            return getName();
        } else {
            if ("displayName".equals(name)) {
                return getDisplayName();
            } else {
                if ("plural".equals(name)) {
                    return getPlural();
                } else {
                    if ("description".equals(name)) {
                        return getDescription();
                    } else {
                        if ("base".equals(name)) {
                            return getBase();
                        } else {
                            if ("abstract".equals(name)) {
                                return getAbstract();
                            } else {
                                if ("inheritanceStrategy".equals(name)) {
                                    return getInheritanceStrategy();
                                } else {
                                    if ("paginable".equals(name)) {
                                        return getPaginable();
                                    } else {
                                        if ("idInjection".equals(name)) {
                                            return getIdInjection();
                                        } else {
                                            if ("fields".equals(name)) {
                                                return getFields();
                                            } else {
                                                if ("acceptExtraFields".equals(name)) {
                                                    return getAcceptExtraFields();
                                                } else {
                                                    if ("relations".equals(name)) {
                                                        return getRelations();
                                                    } else {
                                                        if ("check".equals(name)) {
                                                            return getCheck();
                                                        } else {
                                                            if ("index".equals(name)) {
                                                                return getIndex();
                                                            } else {
                                                                if ("acl".equals(name)) {
                                                                    return getAcl();
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

    @SuppressWarnings({
        "unchecked"
    })
    public<T >T get(String name) {
        Object value = declaredPropertyOrNotFound(name, Resource.NOT_FOUND_VALUE);
        if (Resource.NOT_FOUND_VALUE!= value) {
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

}
