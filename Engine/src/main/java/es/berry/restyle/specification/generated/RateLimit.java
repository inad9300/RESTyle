
package es.berry.restyle.specification.generated;

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
    private Long numOfRequests;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("refreshTime")
    private Long refreshTime;
    protected final static Object NOT_FOUND_VALUE = new Object();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The numOfRequests
     */
    @JsonProperty("numOfRequests")
    public Long getNumOfRequests() {
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
    public void setNumOfRequests(Long numOfRequests) {
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
    public Long getRefreshTime() {
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
    public void setRefreshTime(Long refreshTime) {
        this.refreshTime = refreshTime;
    }

    protected boolean declaredProperty(String name, Object value) {
        if ("numOfRequests".equals(name)) {
            if (value instanceof Long) {
                setNumOfRequests(((Long) value));
            } else {
                throw new IllegalArgumentException(("property \"numOfRequests\" is of type \"java.lang.Long\", but got "+ value.getClass().toString()));
            }
            return true;
        } else {
            if ("refreshTime".equals(name)) {
                if (value instanceof Long) {
                    setRefreshTime(((Long) value));
                } else {
                    throw new IllegalArgumentException(("property \"refreshTime\" is of type \"java.lang.Long\", but got "+ value.getClass().toString()));
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
