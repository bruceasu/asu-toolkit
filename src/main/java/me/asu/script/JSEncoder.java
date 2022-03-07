package me.asu.script;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSEncoder脚本压缩工具
 * <p/>
 * 写完代码之后才发现这是JSA(http://sourceforge.net/project/showfiles.php?group_id=175776)
 * 的压缩算法的再实现 针对jquery-1.2.3.min.js这个文件的压缩比率结果比较如下
 * ------------------------------------------------------------------- 原始大小 |
 * JSEncoder | JSA-20071021（2.0 pre-alpha) | jquery packer算法
 * ------------------------------------------------------------------- 53kb |
 * 32kb | 29kb | 29kb
 * -------------------------------------------------------------------
 * 因为JSA进一步将局部变量进行了压缩，因此相比较更小
 * <p/>
 * <p/>
 * <p/>
 * User: (在路上... http://www.cnblogs.com/midea0978) Date: 2008-4-18 Version:0.5
 */
public class JSEncoder {
	public static final String ENCODE_BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_$";

	public boolean isDebug = false;

	/**
	 * @param filename
	 *            js filename
	 * @param offset
	 *            offset>=0指定偏移变量，不同的offset可以实现代码表位置的变换，较小的offset可以获得更大的压缩率
	 * @return 压缩后的代码
	 */
	public String encode(String filename, int offset) throws Exception {
		String jscript = readFileData(filename);
		int size = jscript.length();
		jscript = jscript.replaceAll("\n", " ");
		// 替换\->\\
		jscript = jscript.replaceAll("\\\\", "\\\\\\\\");
		// 替换单引号'=>\'
		jscript = jscript.replaceAll("\\'", "\\\\\\'");

		Pattern p = Pattern.compile("([\\w\\$]+)");
		Matcher m = p.matcher(jscript);
		String element;
		Vector<String> dict = new Vector<String>();
		int index;
		StringBuffer encscript = new StringBuffer();
		StringBuffer dicttab = new StringBuffer();

		debugInfo("=====编码字典对应表=====");
		while (m.find()) {
			element = m.group(1).trim();
			if (!dict.contains(element)) {
				dict.add(element);
				index = dict.size() - 1;
			} else {
				index = dict.indexOf(element);
			}
			debugInfo(index + "==>" + element);
			m.appendReplacement(encscript, Base64Encode(offset + index + 1));
		}
		for (String o : dict)
			dicttab.append(o + "|");
		m.appendTail(encscript);
		debugInfo("=====  编码字典结束  =====");
		debugInfo("Offset=" + offset + ",字典大小=" + dict.size());
		debugInfo("压缩后的代码：\n" + encscript.toString());
		String dictstr = dicttab.substring(0, dicttab.length() - 1).toString();
		debugInfo("字典字符串:\n" + dictstr);
		String res = formatCode(encscript.toString(), dictstr, dict.size(), offset);
		int packsize = res.length();
		DecimalFormat df = new DecimalFormat("######.0");
		System.out.println("\n原始文件大小：" + size + "\n压缩后文件大小：" + packsize);
		System.out.println("=================\n压缩比率：" + df.format((size - packsize) * 100.0 / size)
				+ "%");
		return res;
	}

	public String encode(String jscript) throws Exception {
		if (jscript == null || "".equals(jscript.trim())) {
			return "";
		}
		jscript = jscript.replaceAll("\n", " ");
		// 替换\->\\
		jscript = jscript.replaceAll("\\\\", "\\\\\\\\");
		// 替换单引号'=>\'
		jscript = jscript.replaceAll("\\'", "\\\\\\'");

		Pattern p = Pattern.compile("([\\w\\$]+)");
		Matcher m = p.matcher(jscript);
		String element;
		Vector<String> dict = new Vector<String>();
		int index;
		StringBuffer encscript = new StringBuffer();
		StringBuffer dicttab = new StringBuffer();

		while (m.find()) {
			element = m.group(1).trim();
			if (!dict.contains(element)) {
				dict.add(element);
				index = dict.size() - 1;
			} else {
				index = dict.indexOf(element);
			}
			m.appendReplacement(encscript, Base64Encode(0 + index + 1));
		}
		for (String o : dict)
			dicttab.append(o + "|");
		m.appendTail(encscript);
		String dictstr = dicttab.substring(0, dicttab.length() - 1).toString();
		String res = formatCode(encscript.toString(), dictstr, dict.size(), 0);
		return res;
	}

	private String readFileData(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		StringBuffer sb = new StringBuffer();
		while (in.ready()) {
			sb.append(in.readLine() + "\n");
		}
		in.close();
		return sb.toString();
	}

	private void debugInfo(String txt) {
		if (isDebug)
			System.out.println(txt);
	}

	public static void main(String[] args) {
		System.out.println("JSEncoder 0.5 by midea0978 2008.4");
		System.out.println("=====================================");
		System.out.println("http://www.cnblogs.com/midea0978\n");
		if (args.length < 2) {
			System.out.println("Usage:java JSEncoder.jar jsfile outputfile [offset].");
			System.exit(0);
		}
		try {
			System.out.println("输入文件: " + args[0]);
			System.out.println("输出文件: " + args[1]);
			JSEncoder util = new JSEncoder();
			int offset = args.length >= 3 ? Integer.parseInt(args[2]) : 0;
			String code = util.encode(args[0], offset);
			FileOutputStream fs = new FileOutputStream(args[1]);
			fs.write(code.getBytes());
			fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 仿Base64解码
	 * 
	 * @param c
	 *            待编码的数字
	 * @return 编码值
	 */
	private String Base64Encode(int c) throws Exception {
		String res;
		if (c < 0)
			throw new Exception("Error:Offset必须>=0.");
		if (c > 63)
			res = Base64Encode(c >> 6) + Base64Encode(c & 63);
		else {
			// 为了配合appendReplacement方法的使用，将$替换为\$
			res = c == 63 ? "\\$" : String.valueOf(ENCODE_BASE64.charAt(c));
		}
		return res;
	}

	private String formatCode(String enc, String dict, int size, int offset) {
		StringBuffer str = new StringBuffer();
		str
				.append("/* Compressed by JSEncoder */\neval(function(E,I,A,D,J,K,L,H){function C(A){return A<62?String.fromCharCode(A+=A<26?65:A<52?71:-4):A<63?'_':A<64?'$':C(A>>6)+C(A&63)}while(A>0)K[C(D--)]=I[--A];function N(A){return K[A]==L[A]?A:K[A]}if(''.replace(/^/,String)){var M=E.match(J),B=M[0],F=E.split(J),G=0;if(E.indexOf(F[0]))F=[''].concat(F);do{H[A++]=F[G++];H[A++]=N(B)}while(B=M[G]);H[A++]=F[G]||'';return H.join('')}return E.replace(J,N)}(");
		str.append("'" + enc + "',");
		str.append("'" + dict + "'.split('|'),");
		str.append(size + "," + (size + offset) + ",/[\\w\\$]+/g, {}, {}, []))");
		return str.toString();
	}

}
