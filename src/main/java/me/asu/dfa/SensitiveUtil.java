package me.asu.dfa;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 敏感词工具类
 *
 */
public final class SensitiveUtil {

    public static final String   DEFAULT_SEPARATOR = ",";
    private static      WordTree sensitiveTree     = new WordTree();

    /**
     * @return 是否已经被初始化
     */
    public static boolean isInited() {
        return !sensitiveTree.isEmpty();
    }

    /**
     * 初始化敏感词树
     *
     * @param isAsync 是否异步初始化
     * @param sensitiveWords 敏感词列表
     */
    public static void init(final Collection<String> sensitiveWords, boolean isAsync) {
        if (isAsync) {
            new Thread() {
                @Override
                public void run() {
                    init(sensitiveWords);
                }
            }.start();
        } else {
            init(sensitiveWords);
        }
    }

    /**
     * 初始化敏感词树
     *
     * @param sensitiveWords 敏感词列表
     */
    public static void init(Collection<String> sensitiveWords) {
        sensitiveTree.clear();
        sensitiveTree.addWords(sensitiveWords);
//		log.debug("Sensitive init finished, sensitives: {}", sensitiveWords);
    }

    /**
     * 初始化敏感词树
     *
     * @param sensitiveWords 敏感词列表组成的字符串
     * @param isAsync 是否异步初始化
     * @param separator 分隔符
     */
    public static void init(String sensitiveWords, String separator, boolean isAsync) {
        if (sensitiveWords != null && !sensitiveWords.trim().isEmpty()) {
            init(Arrays.asList(sensitiveWords.split(separator)), isAsync);
        }
    }

    /**
     * 初始化敏感词树，使用逗号分隔每个单词
     *
     * @param sensitiveWords 敏感词列表组成的字符串
     * @param isAsync 是否异步初始化
     */
    public static void init(String sensitiveWords, boolean isAsync) {
        init(sensitiveWords, DEFAULT_SEPARATOR, isAsync);
    }

    /**
     * 是否包含敏感词
     *
     * @param text 文本
     * @return 是否包含
     */
    public static boolean containsSensitive(String text) {
        return sensitiveTree.isMatch(text);
    }


    /**
     * 查找敏感词，返回找到的第一个敏感词
     *
     * @param text 文本
     * @return 敏感词
     */
    public static String getFindedFirstSensitive(String text) {
        return sensitiveTree.match(text);
    }


    /**
     * 查找敏感词，返回找到的所有敏感词
     *
     * @param text 文本
     * @return 敏感词
     */
    public static List<String> getAllSensitiveFound(String text) {
        return sensitiveTree.matchAll(text);
    }

    /**
     * 查找敏感词，返回找到的所有敏感词<br>
     * 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]<br>
     * 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text 文本
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch 是否使用贪婪匹配（最长匹配）原则
     * @return 敏感词
     */
    public static List<String> getAllSensitiveFound(String text,
                                                     boolean isDensityMatch,
                                                     boolean isGreedMatch) {
        return sensitiveTree.matchAll(text, -1, isDensityMatch, isGreedMatch);
    }

}
