package me.asu.net.http_client;

import static me.asu.net.http_client.Constants.DEFAULT_CONNECT_TIMEOUT_IN_MILLS;
import static me.asu.net.http_client.Constants.DEFAULT_READ_TIMEOUT_IN_MILLS;
import static me.asu.net.http_client.Constants.METHOD_GET;

import java.net.Proxy;
import java.util.Map;
import lombok.Data;
import me.asu.net.http_client.entity.SimpleEntity;

/**
 * @author Administrator
 */
@Data
public class RequestOptions {

    String              method         = METHOD_GET;
    Header              headers        = Header.create();
    Map<String, Object> params         = null;
    SimpleEntity        data           = null;
    int                 connectTimeout = DEFAULT_CONNECT_TIMEOUT_IN_MILLS;
    int                 readTimeout    = DEFAULT_READ_TIMEOUT_IN_MILLS;
    String              encoding       = null;
    String              url            = null;
    Cookie              cookie;
    Proxy               proxy;
    boolean             largeResp      = false;

    public Cookie getCookie() {
        String s = headers.get("Cookie");
        if (null == s) {
            return new Cookie();
        }
        return new Cookie(s);
    }

    public RequestOptions setCookie(Cookie cookie) {
        headers.set("Cookie", cookie.toString());
        return this;
    }
}
