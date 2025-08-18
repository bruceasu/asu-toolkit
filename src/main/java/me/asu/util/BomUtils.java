package me.asu.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class BomUtils {
    private static Logger log = LoggerFactory.getLogger(BomUtils.class);
  /*
    00 00 FE FF    = UTF-32, big-endian
    FF FE 00 00    = UTF-32, little-endian
    EF BB BF       = UTF-8,
    FE FF          = UTF-16, big-endian
    FF FE          = UTF-16, little-endian
  */

    /**
     * Different encodings will have different BOMs. This is for UTF-8.
     */
    private final int[] BYTE_ORDER_MARK_UTF8    = {239, 187, 191};
    private final int[] BYTE_ORDER_MARK_UTF16BE = {254, 255};
    private final int[] BYTE_ORDER_MARK_UTF16LE = {255, 254};
    private final int[] BYTE_ORDER_MARK_UTF32BE = {0, 0, 254, 255};
    private final int[] BYTE_ORDER_MARK_UTF32LE = {255, 254, 0, 0};

    public enum BOM_TYPE {
        UTF8, UTF16BE, UTF16LE, UTF32BE, UTF32LE
    }

    public boolean startsWithBOM(File textFile, BOM_TYPE type) throws IOException {
        if (textFile == null || type == null) return false;
        int[] bom = null;
        switch (type) {
            case UTF8:
                bom = BYTE_ORDER_MARK_UTF8;
                break;
            case UTF16BE:
                bom = BYTE_ORDER_MARK_UTF16BE;
                break;
            case UTF16LE:
                bom = BYTE_ORDER_MARK_UTF16LE;
                break;
            case UTF32BE:
                bom = BYTE_ORDER_MARK_UTF32BE;
                break;
            case UTF32LE:
                bom = BYTE_ORDER_MARK_UTF32LE;
                break;
        }

        return startsWithBOM(textFile, bom);
    }

    public boolean startsWithBOM(File textFile, int[] bom) throws IOException {
        if (textFile == null || bom == null || bom.length == 0) return false;
        boolean result = false;
        if (textFile.length() < bom.length) return false;
        //open as bytes here, not characters
        int[] firstFewBytes = new int[bom.length];
        try (InputStream input = new FileInputStream(textFile);) {
            for (int index = 0; index < bom.length; ++index) {
                firstFewBytes[index] = input.read(); //read a single byte
            }
            result = Arrays.equals(firstFewBytes, bom);
        }
        return result;
    }

    public boolean startsWithBOM(byte[] text, BOM_TYPE type) throws IOException {
        if (text == null || text.length == 0 || type == null) return false;
        int[] bom = null;
        switch (type) {
            case UTF8:
                bom = BYTE_ORDER_MARK_UTF8;
                break;
            case UTF16BE:
                bom = BYTE_ORDER_MARK_UTF16BE;
                break;
            case UTF16LE:
                bom = BYTE_ORDER_MARK_UTF16LE;
                break;
            case UTF32BE:
                bom = BYTE_ORDER_MARK_UTF32BE;
                break;
            case UTF32LE:
                bom = BYTE_ORDER_MARK_UTF32LE;
                break;
        }

        return startsWithBOM(text, bom);
    }

    public boolean startsWithBOM(byte[] text, int[] bom) throws IOException {
        if (text == null || bom == null || bom.length == 0) return false;
        boolean result = false;
        if (text.length < bom.length) return false;
        //open as bytes here, not characters
        int[] firstFewBytes = new int[bom.length];
        InputStream input = null;
        try {
            for (int i = 0; i < bom.length; i++) {
                firstFewBytes[i] = text[i];
            }
            result = Arrays.equals(firstFewBytes, bom);
        } finally {
            input.close();
        }
        return result;
    }

    public byte[] appendBOM(byte[] text, BOM_TYPE type) throws IOException {
        if (type == null) return text;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int[] bom = null;
        switch (type) {
            case UTF8:
                bom = BYTE_ORDER_MARK_UTF8;
                break;
            case UTF16BE:
                bom = BYTE_ORDER_MARK_UTF16BE;
                break;
            case UTF16LE:
                bom = BYTE_ORDER_MARK_UTF16LE;
                break;
            case UTF32BE:
                bom = BYTE_ORDER_MARK_UTF32BE;
                break;
            case UTF32LE:
                bom = BYTE_ORDER_MARK_UTF32LE;
                break;
        }
        for (int i : bom) {
            baos.write(i);
        }

        if (text == null || text.length == 0) return baos.toByteArray();
        baos.write(text);
        return baos.toByteArray();
    }
    public static String appendBOM(String text) {
        if (Strings.isEmpty(text)) {
            return "\ufeff";
        } else {
            return "\ufeff" + text;
        }
    }
    public void appendBOM(Path path , BOM_TYPE type) throws IOException {
        if (!Files.isRegularFile(path)) {
            log.warn("{} is not a regular file.", path);

            return;
        }

        final byte[] text = Files.readAllBytes(path);
        final byte[] bytes = appendBOM(text, type);
        Files.write(path, bytes);
    }

    public void stripBomFrom(String textFile, BOM_TYPE type) throws IOException {
        int[] bom = null;
        if (type == null) type = BOM_TYPE.UTF8;
        switch (type) {
            case UTF8:
                bom = BYTE_ORDER_MARK_UTF8;
                break;
            case UTF16BE:
                bom = BYTE_ORDER_MARK_UTF16BE;
                break;
            case UTF16LE:
                bom = BYTE_ORDER_MARK_UTF16LE;
                break;
            case UTF32BE:
                bom = BYTE_ORDER_MARK_UTF32BE;
                break;
            case UTF32LE:
                bom = BYTE_ORDER_MARK_UTF32LE;
                break;
        }
        File bomFile = new File(textFile);
        long initialSize = bomFile.length();
        long truncatedSize = initialSize - bom.length;
        byte[] memory = new byte[(int) (truncatedSize)];


        try (FileInputStream fis = new FileInputStream(bomFile);
             InputStream input = new BufferedInputStream(fis)) {

            input.skip(bom.length);
            int totalBytesReadIntoMemory = 0;
            while (totalBytesReadIntoMemory < truncatedSize) {
                int bytesRemaining = (int) truncatedSize - totalBytesReadIntoMemory;
                int bytesRead = input.read(memory, totalBytesReadIntoMemory,
                        bytesRemaining);
                if (bytesRead > 0) {
                    totalBytesReadIntoMemory = totalBytesReadIntoMemory + bytesRead;
                }
            }
            writeToFile(memory, bomFile);
        }
        File after = new File(textFile);
        long finalSize = after.length();
        long changeInSize = initialSize - finalSize;
        if (changeInSize != bom.length) {
            throw new RuntimeException(
                    "Change in file size: " + changeInSize + " Expected change: " + bom.length
            );
        }
    }


    private void writeToFile(byte[] bytesWithoutBOM, File textFile)
            throws IOException {
        final File parentFile = textFile.getParentFile();
        if (!parentFile.isDirectory()) {
            parentFile.mkdirs();
        }
        Files.write(textFile.toPath(), bytesWithoutBOM);
    }
} 
