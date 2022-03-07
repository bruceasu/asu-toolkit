package me.asu.cache;

import lombok.Data;

/**
 * 缓存对象
 *
 * @param <K> Key类型
 * @param <V> Value类型
 */
@Data
public class CacheObj<K, V> {
	
	final K key;
	final V obj;
	
	/** 上次访问时间 */
	long lastAccess; 
	/** 访问次数 */
	long accessCount;
	/** 对象存活时长，0表示永久存活*/
	long ttl;
	
	public CacheObj(K key, V obj, long ttl) {
		this.key = key;
		this.obj = obj;
		this.ttl = ttl;
		this.lastAccess = System.currentTimeMillis();
	}
	
	/**
	 * 判断是否过期
	 * 
	 * @return 是否过期
	 */
	public boolean isExpired() {
		return (this.ttl > 0) && (this.lastAccess + this.ttl < System.currentTimeMillis());
	}
	
	/**
	 * 获取值
	 * 
	 * @param isUpdateLastAccess 是否更新最后访问时间
	 * @return 获得对象
	 * @since 4.0.10
	 */
	public V get(boolean isUpdateLastAccess) {
		if(isUpdateLastAccess) {
			lastAccess = System.currentTimeMillis();
		}
		accessCount++;
		return obj;
	}

}
