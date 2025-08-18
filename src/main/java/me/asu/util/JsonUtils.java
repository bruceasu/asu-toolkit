package me.asu.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2020/5/15.
 */
public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }


    public static String serialize(Object data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeForPrint(Object data) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] serializeToBytes(Object data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode deserialize(String data) {
        if (isEmpty(data)) {
            return NullNode.getInstance();
        }
        try {
            return mapper.readTree(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode deserialize(byte[] data) {
        if (data == null || data.length == 0) {
            return NullNode.getInstance();
        }
        try {
            return mapper.readTree(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(String data, TypeReference<T> type) {
        try {
            return mapper.readValue(data, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(String data, Class<T> cls) {
        if (cls == String.class) {
            return (T) data;
        } else {
            try {
                return mapper.readValue(data, createTypeReference(cls));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 使用 TypeReference 反序列化含有泛型的对象
    public static <T, E> T fromJson(String json, Class<T> t, Class<E> e) throws IOException {
        return mapper.readValue(json, mapper.getTypeFactory().constructParametricType(t, e));
    }

    public static Map deserializeToMap(byte[] data) {
        try {
            return mapper.readValue(data, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * to Map
     *
     * @param data json String
     * @return a Map
     */
    public static Map deserializeToMap(String data) {
        try {
            return mapper.readValue(data, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 反序列化泛型集合
    public static <T> List<T> deserializeToList(String data, Class<T> cls) {
        CollectionType listType = mapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, cls);
        try {
            return mapper.readValue(data, listType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<Map<String, Object>> deserializeToList(String data) {
        try {
            return mapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> convertToMap(Object object) {
        //用jackson将bean转换为map
        return mapper.convertValue(object, new TypeReference<Map<String, Object>>() {
        });
    }


    public static List<Map> convertToMapList(List<Object> list) {
        //用jackson将bean转换为List<Map>
        return mapper.convertValue(list, new TypeReference<List<Map>>() {
        });
    }

    public static <T> T convertToObject(Object data, Class<T> cls) {
        if (data == null) return null;
        if (data.getClass() == JsonNode.class) {
            return (T) data;
        } else {
            try {
                return mapper.convertValue(data, createTypeReference(cls));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T convertToObject(Object data, TypeReference<T> cls) {
        if (data == null) return null;
        if (data.getClass() == JsonNode.class) {
            return (T) data;
        } else {
            try {
                return mapper.convertValue(data, cls);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static String asText(JsonNode dtNode, String item) {
        return asText(dtNode, item, null);
    }

    public static String asText(JsonNode dtNode, String item, String defaultValue) {
        if (dtNode == null) {
            return defaultValue;
        }

        JsonNode node = dtNode.get(item);
        if (node == null) {
            return defaultValue;
        }
        return node.asText();
    }

    public static JsonNode at(JsonNode dtNode, String path) {
        if (dtNode == null || isEmpty(path)) {
            return null;
        }

        return dtNode.at(path);
    }

    public static String atAsText(JsonNode dtNode, String path) {
        if (dtNode == null || isEmpty(path)) {
            return null;
        }

        return dtNode.at(path).asText();
    }

    public static <T> TypeReference<T> createTypeReference(Class<T> cls) {
        return new TypeReference<T>() {
            @Override
            public Type getType() {
                return cls;
            }
        };
    }


    private static boolean isEmpty(String path) {
        return path == null || path.trim().isEmpty();
    }


    public static ObjectNode createObject() {
        return mapper.createObjectNode();

    }

    public static ArrayNode createArray() {
        return mapper.createArrayNode();
    }
}
