package me.asu.han;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Char2Mat {

	private int font_size = 48;
	private int font_height = font_size;
	private int font_width = font_size;
	private int size_step = 8;
	private char word = '我';
	private byte[] cbuf;
	private char[] key = {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
	private static File fontDir = new File("");

	public static  File getFontDir() {
		return fontDir;
	}

	public static void setFontDir(File fontDir) {
		Char2Mat.fontDir = fontDir;
	}

	public Char2Mat(int font_size, char word) {
		this.font_size = font_size;
		this.font_height = font_size;
		if (font_size != 12) {
			this.font_width = font_size;
		} else {
			this.font_width = 16;
		}
		this.word = word;
		getMat();
	}

	private void getMat() {
		try {
			int sizeof_byte = size_step;
			int offset_step = font_width * font_height / sizeof_byte;

			byte[] incode = String.valueOf(word).getBytes("GB2312");
			int t1 = (int) (incode[0] & 0xff);
			int t2 = (int) (incode[1] & 0xff);
			int offset = 0;

			// calculate offset for different size font
			if (t1 > 0xa0) {
				if (font_size == 40 || font_size == 48) {
					// 这里暂不处理t1 < 0xa1 + 0x0f的部分,注意大于24的字体都是倒立了的
					offset = ((t1 - 0xa1 - 0x0f) * 94 + (t2 - 0xa1))
							* offset_step;
				} else if (font_size == 12 || font_size == 16 || font_size == 24
						|| font_size == 32) {
					offset = ((t1 - 0xa1) * 94 + (t2 - 0xa1)) * offset_step;
				}
			} else {
				offset = (t1 + 156 - 1) * offset_step;
			}

			cbuf = new byte[offset_step];
			FileInputStream inputStream = new FileInputStream(new File(fontDir,"HZK"
					+ String.valueOf(font_size)));
			inputStream.skip(offset);
			if (inputStream.read(cbuf, 0, offset_step) < 0) {
				System.out.println("read failed!");
				return;
			}

			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void print() {
		if (font_size == 40 || font_size == 48) {
			for (int i = 0; i < font_size; i++) {
				for (int j = 0; j < font_size; j++) {
					int index = i * font_width + j;
					int flag = cbuf[index / size_step] & key[index % size_step];
					System.out.print(flag > 0 ? "●" : "○");
				}
				System.out.println();
			}
		} else if (font_size == 12 || font_size == 16 || font_size == 24 || font_size == 32) {
			for (int j = 0; j < font_size; j++) {
				for (int i = 0; i < font_size; i++) {
					int index = j * font_width + i;
					int flag = cbuf[index / size_step] & key[index % size_step];
					System.out.print(flag > 0 ? "M" : " ");
				}
				System.out.println();
			}
		}
	}

	public static void main(String[] args) {
		Char2Mat.setFontDir(new File("..\\汉字点阵字库"));
		Char2Mat cm = new Char2Mat(12, '我');
		cm.print();

	}
}
