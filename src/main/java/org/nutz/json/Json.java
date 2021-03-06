package org.nutz.json;


import static org.nutz.lang.util.Streams.buffr;
import static org.nutz.lang.util.Streams.fileInr;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.nutz.json.entity.JsonEntity;
import org.nutz.json.entity.NutType;
import org.nutz.json.impl.JsonCompileImplV2;
import org.nutz.json.impl.JsonEntityFieldMakerImpl;
import org.nutz.json.impl.JsonRenderImpl;
import org.nutz.json.impl.ObjConvertImpl;
import org.nutz.lang.util.Files;
import org.nutz.lang.util.Lang;
import org.nutz.lang.util.Mirror;
import org.nutz.lang.util.Streams;

public class Json {

    // =========================================================================
    // ============================Json.fromJson================================
    // =========================================================================

    /**
     * 保存所有的 Json 实体
     */
    private static final ConcurrentHashMap<String, JsonEntity> entities = new ConcurrentHashMap<String, JsonEntity>();
    protected static JsonFormat deft = JsonFormat.nice();
    // =========================================================================
    // ============================Json.toJson==================================
    // =========================================================================
    private static Class<? extends JsonRender> jsonRenderCls;
    private static JsonEntityFieldMaker deftMaker = new JsonEntityFieldMakerImpl();

    /**
     * 从文本输入流中生成 JAVA 对象。
     *
     * @param reader 文本输入流
     * @return JAVA 对象
     */
    public static Object fromJson(Reader reader) throws JsonException {
        // return new org.nutz.json.impl.JsonCompileImpl().parse(reader);
        return new JsonCompileImplV2().parse(reader);
    }

    /**
     * 根据指定的类型，从文本输入流中生成 JAVA 对象。 指定的类型可以是任意 JAVA 对象。
     *
     * @param type   对象类型
     * @param reader 文本输入流
     * @return 指定类型的 JAVA 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(Class<T> type, Reader reader) throws JsonException {
        return (T) parse(type, reader);
    }

    /**
     * 根据指定的类型，从文本输入流中生成 JAVA 对象。 指定的类型可以是任意 JAVA 对象。
     *
     * @param type   对象类型，可以是范型
     * @param reader 文本输入流
     * @return 指定类型的 JAVA 对象
     */
    public static Object fromJson(Type type, Reader reader) throws JsonException {
        return parse(type, reader);
    }

    private static Object parse(Type type, Reader reader) {
        Object obj = fromJson(reader);
        if (type != null) {
            return new ObjConvertImpl(type).convert(obj);
        }
        return obj;
    }

    /**
     * 根据指定的类型，从字符串中生成 JAVA 对象。 指定的类型可以是任意 JAVA 对象。
     *
     * @param type 对象类型，可以是范型
     * @param cs   JSON 字符串
     * @return 指定类型的 JAVA 对象
     */
    public static Object fromJson(Type type, CharSequence cs) throws JsonException {
        return fromJson(type, Lang.inr(cs));
    }

    /**
     * 根据指定的类型，读取指定的 JSON 文件生成 JAVA 对象。 指定的类型可以是任意 JAVA 对象。
     *
     * @param type 对象类型
     * @param f    文件对象
     * @return 指定类型的 JAVA 对象
     */
    public static <T> T fromJsonFile(Class<T> type, File f) {
        BufferedReader br = null;
        try {
            br = buffr(fileInr(f));
            return Json.fromJson(type, br);
        } finally {
            Streams.safeClose(br);
        }
    }

    /**
     * 从 JSON 字符串中，获取 JAVA 对象。 实际上，它就是用一个 Read 包裹了的字符串。
     * <p>
     * 请参看函数 ‘Object fromJson(Reader reader)’ 的描述
     *
     * @param cs JSON 字符串
     * @return JAVA 对象
     * @see #fromJson(Reader reader)
     */
    public static Object fromJson(CharSequence cs) throws JsonException {
        return fromJson(Lang.inr(cs));
    }

    /**
     * 根据指定的类型，从字符串中生成 JAVA 对象。 指定的类型可以是任意 JAVA 对象。
     * <p>
     * 请参看函数 ‘<T> T fromJson(Class<T> type, Reader reader)’ 的描述
     *
     * @param type 对象类型
     * @param cs   JSON 字符串
     * @return 特定类型的 JAVA 对象
     * @see #fromJson(Class type, Reader reader)
     */
    public static <T> T fromJson(Class<T> type, CharSequence cs) throws JsonException {
        return fromJson(type, Lang.inr(cs));
    }

    public static Class<? extends JsonRender> getJsonRenderCls() {
        return jsonRenderCls;
    }

    public static void setJsonRenderCls(Class<? extends JsonRender> cls) {
        jsonRenderCls = cls;
    }

    /**
     * 将一个 JAVA 对象转换成 JSON 字符串
     *
     * @param obj JAVA 对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        return toJson(obj, null);
    }

    /**
     * 将一个 JAVA 对象转换成 JSON 字符串，并且可以设定 JSON 字符串的格式化方式
     *
     * @param obj    JAVA 对象
     * @param format JSON 字符串格式化方式 ，若 format 为 null ，则以 JsonFormat.nice() 格式输出
     * @return JSON 字符串
     */
    public static String toJson(Object obj, JsonFormat format) {
        StringWriter writer = new StringWriter();
        toJson(writer, obj, format);
        return writer.toString();
    }

    /**
     * 将一个 JAVA 对象以 JSON 的形式写到一个文本输出流里
     *
     * @param writer 文本输出流
     * @param obj    JAVA 对象
     */
    public static void toJson(Writer writer, Object obj) {
        toJson(writer, obj, null);
    }

    /**
     * 将一个 JAVA 对象以 JSON 的形式写到一个文本输出流里，并且可以设定 JSON 字符串的格式化方式
     *
     * @param writer 文本输出流
     * @param obj    JAVA 对象
     * @param format JSON 字符串格式化方式 ，若 format 为 null ，则以 JsonFormat.nice() 格式输出
     */
    public static void toJson(Writer writer, Object obj, JsonFormat format) {
        try {
            if (format == null) {
                format = deft.clone();
            }
            JsonRender jr;
            Class<? extends JsonRender> jrCls = getJsonRenderCls();
            if (jrCls == null) {
                jr = new JsonRenderImpl();
            } else {
                jr = Mirror.me(jrCls).born();
            }
            jr.setWriter(writer);
            jr.setFormat(format);
            jr.render(obj);

            writer.flush();
        } catch (IOException e) {
            throw Lang.wrapThrow(e, JsonException.class);
        }
    }

    /**
     * 将一个 JAVA 对象以 JSON 的形式写到一个文件里
     *
     * @param f   文本文件
     * @param obj JAVA 对象
     */
    public static void toJsonFile(File f, Object obj) {
        toJsonFile(f, obj, null);
    }

    /**
     * 将一个 JAVA 对象以 JSON 的形式写到一个文件里，并且可以设定 JSON 字符串的格式化方式
     *
     * @param f      文本文件
     * @param obj    JAVA 对象
     * @param format JSON 字符串格式化方式 ，若 format 为 null ，则以 JsonFormat.nice() 格式输出
     */
    public static void toJsonFile(File f, Object obj, JsonFormat format) {
        Writer writer = null;
        try {
            Files.createFileIfNoExists(f);
            writer = Streams.fileOutw(f);
            Json.toJson(writer, obj, format);
            writer.append('\n');
        } catch (IOException e) {
            throw Lang.wrapThrow(e);
        } finally {
            Streams.safeClose(writer);
        }
    }

    // ==================================================================================
    // ====================帮助函数======================================================

    /**
     * 清除 Json 解析器对实体解析的缓存
     */
    public static void clearEntityCache() {
        entities.clear();
    }

    /**
     * 获取一个 Json 实体
     */
    public static JsonEntity getEntity(Mirror<?> mirror) {
        JsonEntity je = entities.get(mirror.getTypeId());
        if (null == je) {
            je = new JsonEntity(mirror);
            entities.put(mirror.getTypeId(), je);
        }
        return je;
    }

    /**
     * 从 JSON 字符串中，根据获取某种指定类型的 List 对象。
     * <p>
     * 请参看函数 ‘Object fromJson(Type type, CharSequence cs)’ 的描述
     *
     * @param eleType 对象类型
     * @param cs      JSON 字符串
     * @return 特定类型的 JAVA 对象
     * @see #fromJson(Type type, CharSequence cs)
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> fromJsonAsList(Class<T> eleType, CharSequence cs) {
        return (List<T>) fromJson(NutType.list(eleType), cs);
    }

    /**
     * 从 JSON 输入流中，根据获取某种指定类型的 List 对象。
     * <p>
     * 请参看函数 ‘Object fromJson(Type type, Reader reader)’ 的描述
     *
     * @param eleType 对象类型
     * @param reader  JSON 输入流
     * @return 特定类型的 JAVA 对象
     * @see #fromJson(Type type, Reader reader)
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> fromJsonAsList(Class<T> eleType, Reader reader) {
        return (List<T>) fromJson(NutType.list(eleType), reader);
    }

    /**
     * 从 JSON 字符串中，根据获取某种指定类型的 数组 对象。
     * <p>
     * 请参看函数 ‘Object fromJson(Type type, CharSequence cs)’ 的描述
     *
     * @param eleType 对象类型
     * @param cs      JSON 字符串
     * @return 特定类型的 JAVA 对象
     * @see #fromJson(Type type, CharSequence cs)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] fromJsonAsArray(Class<T> eleType, CharSequence cs) {
        return (T[]) fromJson(NutType.array(eleType), cs);
    }

    /**
     * 从 JSON 输入流中，根据获取某种指定类型的 数组 对象。
     * <p>
     * 请参看函数 ‘Object fromJson(Type type, Reader reader)’ 的描述
     *
     * @param eleType 对象类型
     * @param reader  JSON 输入流
     * @return 特定类型的 JAVA 对象
     * @see #fromJson(Class type, Reader reader)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] fromJsonAsArray(Class<T> eleType, Reader reader) {
        return (T[]) fromJson(NutType.array(eleType), reader);
    }

    /**
     * 从 JSON 字符串中，根据获取某种指定类型的 Map 对象。
     * <p>
     * 请参看函数 ‘Object fromJson(Type type, CharSequence cs)’ 的描述
     *
     * @param eleType 对象类型
     * @param cs      JSON 字符串
     * @return 特定类型的 JAVA 对象
     * @see #fromJson(Type type, CharSequence cs)
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> fromJsonAsMap(Class<T> eleType, CharSequence cs) {
        return (Map<String, T>) fromJson(NutType.mapStr(eleType), cs);
    }

    /**
     * 从 JSON 输入流中，根据获取某种指定类型的 Map 对象。
     * <p>
     * 请参看函数 ‘Object fromJson(Type type, Reader reader)’ 的描述
     *
     * @param eleType 对象类型
     * @param reader  JSON 输入流
     * @return 特定类型的 JAVA 对象
     * @see #fromJson(Type type, Reader reader)
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> fromJsonAsMap(Class<T> eleType, Reader reader) {
        return (Map<String, T>) fromJson(NutType.mapStr(eleType), reader);
    }

    public static void setDefaultJsonformat(JsonFormat defaultJf) {
        if (defaultJf == null) {
            defaultJf = JsonFormat.nice();
        }
        Json.deft = defaultJf;
    }

    public static JsonEntityFieldMaker getDefaultFieldMaker() {
        return deftMaker;
    }

    public static void setDefaultFieldMaker(JsonEntityFieldMaker fieldMaker) {
        if (fieldMaker != null) {
            Json.deftMaker = fieldMaker;
        }
    }
}






