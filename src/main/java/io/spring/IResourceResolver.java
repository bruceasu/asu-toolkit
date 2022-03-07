
/**
 * 
 */
package io.spring;


public interface IResourceResolver {
	IResource getResource(String s);

	String CLASSPATH_URL_PREFIX = "classpath:";
}
