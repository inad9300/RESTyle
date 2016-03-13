
package es.berry.restyle.specification;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "dbms",
    "name",
    "host",
    "port",
    "prefix",
    "admin"
})
public class Database {

    @JsonProperty("dbms")
    private String dbms;
    @JsonProperty("name")
    private String name;
    @JsonProperty("host")
    private String host;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("prefix")
    private String prefix;
    /**
     * 
     */
    @JsonProperty("admin")
    private Admin admin;
    protected final static Object NOT_FOUND_VALUE = new Object();

    /**
     * 
     * @return
     *     The dbms
     */
    @JsonProperty("dbms")
    public String getDbms() {
        return dbms;
    }

    /**
     * 
     * @param dbms
     *     The dbms
     */
    @JsonProperty("dbms")
    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    /**
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
     *     The host
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    /**
     * 
     * @param host
     *     The host
     */
    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 
     * @return
     *     The port
     */
    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    /**
     * 
     * @param port
     *     The port
     */
    @JsonProperty("port")
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 
     * @return
     *     The prefix
     */
    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * 
     * @param prefix
     *     The prefix
     */
    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 
     * @return
     *     The admin
     */
    @JsonProperty("admin")
    public Admin getAdmin() {
        return admin;
    }

    /**
     * 
     * @param admin
     *     The admin
     */
    @JsonProperty("admin")
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    protected boolean declaredProperty(String name, Object value) {
        if ("dbms".equals(name)) {
            if (value instanceof String) {
                setDbms(((String) value));
            } else {
                throw new IllegalArgumentException(("property \"dbms\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
            }
            return true;
        } else {
            if ("name".equals(name)) {
                if (value instanceof String) {
                    setName(((String) value));
                } else {
                    throw new IllegalArgumentException(("property \"name\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                }
                return true;
            } else {
                if ("host".equals(name)) {
                    if (value instanceof String) {
                        setHost(((String) value));
                    } else {
                        throw new IllegalArgumentException(("property \"host\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                    }
                    return true;
                } else {
                    if ("port".equals(name)) {
                        if (value instanceof Integer) {
                            setPort(((Integer) value));
                        } else {
                            throw new IllegalArgumentException(("property \"port\" is of type \"java.lang.Integer\", but got "+ value.getClass().toString()));
                        }
                        return true;
                    } else {
                        if ("prefix".equals(name)) {
                            if (value instanceof String) {
                                setPrefix(((String) value));
                            } else {
                                throw new IllegalArgumentException(("property \"prefix\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                            }
                            return true;
                        } else {
                            if ("admin".equals(name)) {
                                if (value instanceof Admin) {
                                    setAdmin(((Admin) value));
                                } else {
                                    throw new IllegalArgumentException(("property \"admin\" is of type \"Admin\", but got "+ value.getClass().toString()));
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

    protected Object declaredPropertyOrNotFound(String name, Object notFoundValue) {
        if ("dbms".equals(name)) {
            return getDbms();
        } else {
            if ("name".equals(name)) {
                return getName();
            } else {
                if ("host".equals(name)) {
                    return getHost();
                } else {
                    if ("port".equals(name)) {
                        return getPort();
                    } else {
                        if ("prefix".equals(name)) {
                            return getPrefix();
                        } else {
                            if ("admin".equals(name)) {
                                return getAdmin();
                            } else {
                                return notFoundValue;
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
        Object value = declaredPropertyOrNotFound(name, Database.NOT_FOUND_VALUE);
        if (Database.NOT_FOUND_VALUE!= value) {
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
