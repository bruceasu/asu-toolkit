package me.asu.net.http_client.entity;

import me.asu.net.http_client.Constants;
import me.asu.util.Bytes;
import me.asu.net.http_client.util.JsonUtils;

public class JsonEntity implements SimpleEntity {

    Object object;

    public JsonEntity(Object object) {
        this.object = object;
    }

    @Override
    public byte[] getContent() {
        return Bytes.toBytes(toContent());
    }

    public String toContent() {
        if (object == null) {
            return "";
        } else {
            return JsonUtils.toJson(object);
        }
    }

    @Override
    public String getContentType() {
        return Constants.MIME_JSON_UTF8;
    }
}
