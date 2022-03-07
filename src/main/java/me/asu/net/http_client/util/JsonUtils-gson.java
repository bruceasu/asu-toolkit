// package me.asu.net.http_client.util;

// import com.google.gson.Gson;
// import com.google.gson.GsonBuilder;
// import com.google.gson.JsonElement;
// import java.lang.reflect.Type;

// /**
//  * base gson
//  * JSONObject
//  * JSONArray
//  * Gson gson = new Gson();
//  * 容器范型类型
//  * new TypeToken<List<ObjectType>>(){}.getType()
//  * new TypeToken<Map<KeyObjecType,ValueObjectType>>(){}.getType()
//  * <p>
//  * SerializedName注解提供了两个属性，
//  * value,字段名，
//  * alternate，别名，接收一个String数组。
//  * 注：alternate需要2.4版本
//  *
//  * @SerializedName(value = "emailAddress", alternate = {"email", "email_address"})
//  * <p>
//  * 流式
//  * JsonReader
//  * JsonWriter
//  * 在Gson::toJson/Gson::fromJson的接口中亦有接口
//  * <p>
//  * GsonBuilder用法
//  * Gson gson = new GsonBuilder()
//  * // 序列化null
//  * .serializeNulls()
//  * // 设置日期时间格式，另有2个重载方法
//  * // 在序列化和反序化时均生效
//  * .setDateFormat("yyyy-MM-dd")
//  * // 禁此序列化内部类
//  * .disableInnerClassSerialization()
//  * // 生成不可执行的Json（多了 )]}' 这4个字符）
//  * .generateNonExecutableJson()
//  * // 禁止转义html标签
//  * .disableHtmlEscaping()
//  * // 格式化输出
//  * .setPrettyPrinting()
//  * // 只输出使用@Expose注释的字段
//  * .excludeFieldsWithoutExposeAnnotation()
//  * .create();
//  */
// public class JsonUtils {

//     public static String toJson(Object obj) {
//         Gson gson = new Gson();
//         return gson.toJson(obj);
//     }

//     public static String toJsonPretty(Object obj) {
//         Gson gson = new GsonBuilder().setPrettyPrinting().create();
//         return gson.toJson(obj);
//     }

//     public static <T> T fromJson(String json, Class<T> cls) {
//         Gson gson = new Gson();
//         return gson.fromJson(json, cls);
//     }

//     public static <T> T fromJson(String json, Type t) {
//         Gson gson = new Gson();
//         return gson.fromJson(json, t);
//     }

//     public static JsonElement toJsonElement(Object src) {
//         Gson gson = new Gson();
//         return gson.toJsonTree(src);
//     }

//     public static JsonElement toJsonElement(Object src, Type t) {
//         Gson gson = new Gson();
//         return gson.toJsonTree(src, t);
//     }
// }
