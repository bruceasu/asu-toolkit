package me.asu.hash;

import java.util.List;
import java.util.Random;

/**
 * 分布策略类
 *
 * @memo: 提供分布策略使用到得公共方法
 */
public class DistributedStrategyUtils {

  /**
   * 根据key的hash值返回List中的一个节点
   *
   * @param <T>   节点类型
   * @param items 节点集合
   * @param key   生成hash的字符串
   * @return 节点对象
   */
  public static <T> T hashGetItem(List<T> items, String key) {

    if (null == items || items.size() == 0) {
      throw new IllegalArgumentException("节点集合不能为空或者空集合");
    }

    long hash = Math.abs(HashAlgorithms.DJB_HASH.hash(key));
    int index = Integer.parseInt(String.valueOf(((hash % items.size()))));

    return items.get(index);
  }

  /**
   * 根据key的hash值返回List中的一个节点
   *
   * @param <T>   节点类型
   * @param items 节点集合
   * @param key   生成hash的字符串
   * @return 节点对象
   */
  public static <T> T hashGetItem(T[] items, String key) {

    if (null == items || items.length == 0) {
      throw new IllegalArgumentException("节点集合不能为空或者空集合");
    }

    long hash = Math.abs(HashAlgorithms.DJB_HASH.hash(key));
    int index = Integer.parseInt(String.valueOf(((hash % items.length))));

    return items[index];
  }

  /**
   * 根据key和节点总数获取节点
   *
   * @param itemsSize 节点总数
   * @param key       生成hash的字符串
   * @return 节点索引
   */
  public static int hashGetItem(int itemsSize, String key) {
    if (itemsSize < 1) {
      throw new IllegalArgumentException("节点集合不能为空");
    }
    long hash = Math.abs(HashAlgorithms.DJB_HASH.hash(key));
    int index = Integer.parseInt(String.valueOf(((hash % itemsSize))));

    return index;
  }

  /**
   * 随机返回节点集合中的一个节点
   *
   * @param <T>   节点类型
   * @param items 节点集合
   * @return 节点对象
   */
  public static <T> T randomGetItem(List<T> items) {

    if (null == items || items.size() == 0) {
      throw new IllegalArgumentException("节点集合不能为空或者空集合");
    }

    Random random = new Random();
    int index = random.nextInt(items.size());

    return items.get(index);
  }

  /**
   * 随机返回节点集合中的一个节点
   *
   * @param <T>   节点类型
   * @param items 节点集合
   * @return 节点对象
   */
  public static <T> T randomGetItem(T[] items) {

    if (null == items || items.length == 0) {
      throw new IllegalArgumentException("节点集合不能为空或者空集合");
    }

    Random random = new Random();
    int index = random.nextInt(items.length);
    return items[index];
  }

  /**
   * 私有构造方法,防止创建实例
   */
  private DistributedStrategyUtils() {
  }
}