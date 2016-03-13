
package es.berry.restyle.specification;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "numOfRequests",
    "refreshTime"
})
public class RateLimit {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfRequests")
    private Integer numOfRequests;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("refreshTime")
    private Integer refreshTime;
    protected final static Object NOT_FOUND_VALUE = new Object();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The numOfRequests
     */
    @JsonProperty("numOfRequests")
    public Integer getNumOfRequests() {
        return numOfRequests;
    }

    /**
     * 
     * (Required)
     * 
     * @param numOfRequests
     *     The numOfRequests
     */
    @JsonProperty("numOfRequests")
    public void setNumOfRequests(Integer numOfRequests) {
        this.numOfRequests = numOfRequests;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The refreshTime
     */
    @JsonProperty("refreshTime")
    public Integer getRefreshTime() {
        return refreshTime;
    }

    /**
     * 
     * (Required)
     * 
     * @param refreshTime
     *     The refreshTime
     */
    @JsonProperty("refreshTime")
    public void setRefreshTime(Integer refreshTime) {
        this.refreshTime = refreshTime;
    }

    protected boolean declaredProperty(String name, Object value) {
        if ("numOfRequests".equals(name)) {
            if (value instanceof Integer) {
                setNumOfRequests(((Integer) value));
            } else {
                throw new IllegalArgumentException(("property \"numOfRequests\" is of type \"java.lang.Integer\", but got "+ value.getClass().toString()));
            }
            return true;
        } else {
            if ("refreshTime".equals(name)) {
                if (value instanceof Integer) {
                    setRefreshTime(((Integer) value));
                } else {
                    throw new IllegalArgumentException(("property \"refreshTime\" is of type \"java.lang.Integer\", but got "+ value.getClass().toString()));
                }
                return true;
            } else {
                return false;
            }
        }
    }

    protected Object declaredPropertyOrNotFound(String name, Object notFoundValue) {
        if ("numOfRequests".equals(name)) {
            return getNumOfRequests();
        } else {
            if ("refreshTime".equals(name)) {
                return getRefreshTime();
            } else {
                return notFoundValue;
            }
        }
    }

    @SuppressWarnings({
        "unchecked"
    })
    public<T >T get(String name) {
        Object value = declaredPropertyOrNotFound(name, RateLimit.NOT_FOUND_VALUE);
        if (RateLimit.NOT_FOUND_VALUE!= value) {
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
