 package me.asu.net.http_client.util;

 import java.lang.reflect.Type;
 import org.nutz.json.Json;
 import org.nutz.json.JsonFormat;

 public class JsonUtils {

     public static String toJson(Object obj) {
         return Json.toJson(obj);
     }

     public static String toJsonPretty(Object obj) {
        return Json.toJson(obj, JsonFormat.nice());
     }

     public static <T> T fromJson(String json, Class<T> cls) {
        return Json.fromJson(cls, json);
     }

     public static <T> T fromJson(String json, Type t) {
         return (T)Json.fromJson(t, json);
     }

 }
