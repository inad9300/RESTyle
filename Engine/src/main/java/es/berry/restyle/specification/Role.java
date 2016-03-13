
package es.berry.restyle.specification;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
    "isAdmin",
    "isGuest",
    "isA",
    "rateLimit"
})
public class Role {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("isAdmin")
    private Boolean isAdmin;
    @JsonProperty("isGuest")
    private Boolean isGuest;
    @JsonProperty("isA")
    private String isA;
    /**
     * 
     */
    @JsonProperty("rateLimit")
    private RateLimit rateLimit;
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
     *     The isAdmin
     */
    @JsonProperty("isAdmin")
    public Boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * 
     * @param isAdmin
     *     The isAdmin
     */
    @JsonProperty("isAdmin")
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * 
     * @return
     *     The isGuest
     */
    @JsonProperty("isGuest")
    public Boolean getIsGuest() {
        return isGuest;
    }

    /**
     * 
     * @param isGuest
     *     The isGuest
     */
    @JsonProperty("isGuest")
    public void setIsGuest(Boolean isGuest) {
        this.isGuest = isGuest;
    }

    /**
     * 
     * @return
     *     The isA
     */
    @JsonProperty("isA")
    public String getIsA() {
        return isA;
    }

    /**
     * 
     * @param isA
     *     The isA
     */
    @JsonProperty("isA")
    public void setIsA(String isA) {
        this.isA = isA;
    }

    /**
     * 
     * @return
     *     The rateLimit
     */
    @JsonProperty("rateLimit")
    public RateLimit getRateLimit() {
        return rateLimit;
    }

    /**
     * 
     * @param rateLimit
     *     The rateLimit
     */
    @JsonProperty("rateLimit")
    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
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
            if ("isAdmin".equals(name)) {
                if (value instanceof Boolean) {
                    setIsAdmin(((Boolean) value));
                } else {
                    throw new IllegalArgumentException(("property \"isAdmin\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                }
                return true;
            } else {
                if ("isGuest".equals(name)) {
                    if (value instanceof Boolean) {
                        setIsGuest(((Boolean) value));
                    } else {
                        throw new IllegalArgumentException(("property \"isGuest\" is of type \"java.lang.Boolean\", but got "+ value.getClass().toString()));
                    }
                    return true;
                } else {
                    if ("isA".equals(name)) {
                        if (value instanceof String) {
                            setIsA(((String) value));
                        } else {
                            throw new IllegalArgumentException(("property \"isA\" is of type \"java.lang.String\", but got "+ value.getClass().toString()));
                        }
                        return true;
                    } else {
                        if ("rateLimit".equals(name)) {
                            if (value instanceof RateLimit) {
                                setRateLimit(((RateLimit) value));
                            } else {
                                throw new IllegalArgumentException(("property \"rateLimit\" is of type \"RateLimit\", but got "+ value.getClass().toString()));
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

    protected Object declaredPropertyOrNotFound(String name, Object notFoundValue) {
        if ("name".equals(name)) {
            return getName();
        } else {
            if ("isAdmin".equals(name)) {
                return getIsAdmin();
            } else {
                if ("isGuest".equals(name)) {
                    return getIsGuest();
                } else {
                    if ("isA".equals(name)) {
                        return getIsA();
                    } else {
                        if ("rateLimit".equals(name)) {
                            return getRateLimit();
                        } else {
                            return notFoundValue;
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
        Object value = declaredPropertyOrNotFound(name, Role.NOT_FOUND_VALUE);
        if (Role.NOT_FOUND_VALUE!= value) {
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
