
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
    "title",
    "description",
    "version",
    "baseUrl",
    "dataFormats",
    "authenticationMechanism",
    "encoding",
    "timeZone",
    "dateFormat",
    "roles",
    "database",
    "types",
    "resources"
})
public class Spec {

    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("version")
    private String version;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("baseUrl")
    private String baseUrl;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dataFormats")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> dataFormats = new LinkedHashSet<String>();
    @JsonProperty("authenticationMechanism")
    private String authenticationMechanism;
    @JsonProperty("encoding")
    private String encoding;
    @JsonProperty("timeZone")
    private String timeZone;
    @JsonProperty("dateFormat")
    private String dateFormat;
    @JsonProperty("roles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Role> roles = new LinkedHashSet<Role>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("database")
    private Database database;
    @JsonProperty("types")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Type> types = new LinkedHashSet<Type>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("resources")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Resource> resources = new LinkedHashSet<Resource>();
    protected final static Object NOT_FOUND_VALUE = new Object();

    /**
     * 
     * @return
     *     The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     *     The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
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
     *     The version
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * 
     * @param version
     *     The version
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The baseUrl
     */
    @JsonProperty("baseUrl")
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 
     * (Required)
     * 
     * @param baseUrl
     *     The baseUrl
     */
    @JsonProperty("baseUrl")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The dataFormats
     */
    @JsonProperty("dataFormats")
    public Set<String> getDataFormats() {
        return dataFormats;
    }

    /**
     * 
     * (Required)
     * 
     * @param dataFormats
     *     The dataFormats
     */
    @JsonProperty("dataFormats")
    public void setDataFormats(Set<String> dataFormats) {
        this.dataFormats = dataFormats;
    }

    /**
     * 
     * @return
     *     The authenticationMechanism
     */
    @JsonProperty("authenticationMechanism")
    public String getAuthenticationMechanism() {
        return authenticationMechanism;
    }

    /**
     * 
     * @param authenticationMechanism
     *     The authenticationMechanism
     */
    @JsonProperty("authenticationMechanism")
    public void setAuthenticationMechanism(String authenticationMechanism) {
        this.authenticationMechanism = authenticationMechanism;
    }

    /**
     * 
     * @return
     *     The encoding
     */
    @JsonProperty("encoding")
    public String getEncoding() {
        return encoding;
    }

    /**
     * 
     * @param encoding
     *     The encoding
     */
    @JsonProperty("encoding")
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * 
     * @return
     *     The timeZone
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * 
     * @param timeZone
     *     The timeZone
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * 
     * @return
     *     The dateFormat
     */
    @JsonProperty("dateFormat")
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * 
     * @param dateFormat
     *     The dateFormat
     */
    @JsonProperty("dateFormat")
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * 
     * @return
     *     The roles
     */
    @JsonProperty("roles")
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * 
     * @param roles
     *     The roles
     */
    @JsonProperty("roles")
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The database
     */
    @JsonProperty("database")
    public Database getDatabase() {
        return database;
    }

    /**
     * 
     * (Required)
     * 
     * @param database
     *     The database
     */
    @JsonProperty("database")
    public void setDatabase(Database database) {
        this.database = database;
    }

    /**
     * 
     * @return
     *     The types
     */
    @JsonProperty("types")
    public Set<Type> getTypes() {
        return types;
    }

    /**
     * 
     * @param types
     *     The types
     */
    @JsonProperty("types")
    public void setTypes(Set<Type> types) {
        this.types = types;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The resources
     */
    @JsonProperty("resources")
    public Set<Resource> getResources() {
        return resources;
    }

    /**
     * 
     * (Required)
     * 
     * @param resources
     *     The resources
     */
    @JsonProperty("resources")
    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    protected boolean declaredProperty(String name, Object value) {
        if ("title".equals(name)) {
            if (value instanceof String) {
                setTitle(((String) value));
            } else {
                throw new IllegalArgumentException(("property \"title\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
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
                if ("version".equals(name)) {
                    if (value instanceof String) {
                        setVersion(((String) value));
                    } else {
                        throw new IllegalArgumentException(("property \"version\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                    }
                    return true;
                } else {
                    if ("baseUrl".equals(name)) {
                        if (value instanceof String) {
                            setBaseUrl(((String) value));
                        } else {
                            throw new IllegalArgumentException(("property \"baseUrl\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                        }
                        return true;
                    } else {
                        if ("dataFormats".equals(name)) {
                            if (value instanceof Set) {
                                setDataFormats(((Set<String> ) value));
                            } else {
                                throw new IllegalArgumentException(("property \"dataFormats\" is of type \"java.util.Set<java.lang.String>\", but got "+ value.getClass().toString()));
                            }
                            return true;
                        } else {
                            if ("authenticationMechanism".equals(name)) {
                                if (value instanceof String) {
                                    setAuthenticationMechanism(((String) value));
                                } else {
                                    throw new IllegalArgumentException(("property \"authenticationMechanism\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                                }
                                return true;
                            } else {
                                if ("encoding".equals(name)) {
                                    if (value instanceof String) {
                                        setEncoding(((String) value));
                                    } else {
                                        throw new IllegalArgumentException(("property \"encoding\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                                    }
                                    return true;
                                } else {
                                    if ("timeZone".equals(name)) {
                                        if (value instanceof String) {
                                            setTimeZone(((String) value));
                                        } else {
                                            throw new IllegalArgumentException(("property \"timeZone\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                                        }
                                        return true;
                                    } else {
                                        if ("dateFormat".equals(name)) {
                                            if (value instanceof String) {
                                                setDateFormat(((String) value));
                                            } else {
                                                throw new IllegalArgumentException(("property \"dateFormat\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                                            }
                                            return true;
                                        } else {
                                            if ("roles".equals(name)) {
                                                if (value instanceof Set) {
                                                    setRoles(((Set<Role> ) value));
                                                } else {
                                                    throw new IllegalArgumentException(("property \"roles\" is of type \"java.util.Set<es.berry.restyle.specification.generated.Role>\", but got "+ value.getClass().toString()));
                                                }
                                                return true;
                                            } else {
                                                if ("database".equals(name)) {
                                                    if (value instanceof Database) {
                                                        setDatabase(((Database) value));
                                                    } else {
                                                        throw new IllegalArgumentException(("property \"database\" is of type \"es.berry.restyle.specification.generated.Database\", but got "+ value.getClass().toString()));
                                                    }
                                                    return true;
                                                } else {
                                                    if ("types".equals(name)) {
                                                        if (value instanceof Set) {
                                                            setTypes(((Set<Type> ) value));
                                                        } else {
                                                            throw new IllegalArgumentException(("property \"types\" is of type \"java.util.Set<es.berry.restyle.specification.generated.Type>\", but got "+ value.getClass().toString()));
                                                        }
                                                        return true;
                                                    } else {
                                                        if ("resources".equals(name)) {
                                                            if (value instanceof Set) {
                                                                setResources(((Set<Resource> ) value));
                                                            } else {
                                                                throw new IllegalArgumentException(("property \"resources\" is of type \"java.util.Set<es.berry.restyle.specification.generated.Resource>\", but got "+ value.getClass().toString()));
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

    protected Object declaredPropertyOrNotFound(String name, Object notFoundValue) {
        if ("title".equals(name)) {
            return getTitle();
        } else {
            if ("description".equals(name)) {
                return getDescription();
            } else {
                if ("version".equals(name)) {
                    return getVersion();
                } else {
                    if ("baseUrl".equals(name)) {
                        return getBaseUrl();
                    } else {
                        if ("dataFormats".equals(name)) {
                            return getDataFormats();
                        } else {
                            if ("authenticationMechanism".equals(name)) {
                                return getAuthenticationMechanism();
                            } else {
                                if ("encoding".equals(name)) {
                                    return getEncoding();
                                } else {
                                    if ("timeZone".equals(name)) {
                                        return getTimeZone();
                                    } else {
                                        if ("dateFormat".equals(name)) {
                                            return getDateFormat();
                                        } else {
                                            if ("roles".equals(name)) {
                                                return getRoles();
                                            } else {
                                                if ("database".equals(name)) {
                                                    return getDatabase();
                                                } else {
                                                    if ("types".equals(name)) {
                                                        return getTypes();
                                                    } else {
                                                        if ("resources".equals(name)) {
                                                            return getResources();
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

    @SuppressWarnings({
        "unchecked"
    })
    public<T >T get(String name) {
        Object value = declaredPropertyOrNotFound(name, Spec.NOT_FOUND_VALUE);
        if (Spec.NOT_FOUND_VALUE!= value) {
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
