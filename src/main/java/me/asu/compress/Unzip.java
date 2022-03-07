package me.asu.compress;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Unzip {

    private static Logger log = Logger.getLogger("unzip");

    public static void main(String[] args) throws IOException {
        CmdParser cmdParser = new CmdParser(args).parse();
        String    outDir    = cmdParser.getOutDir();
        String    zipFile   = cmdParser.getZipFile();
        String    encoding  = cmdParser.getEncoding();
        boolean   help      = cmdParser.isHelp();
        if (help) {
            cmdParser.help();
            System.exit(1);
        }
        if (zipFile == null) {
            cmdParser.help();
            System.exit(2);
        }
        if (!Files.exists(Paths.get(zipFile))) {
            System.err.printf("%s is not found.%n", zipFile);
            System.exit(3);
        }

        makeOutputDir(outDir);
        unzip(outDir, zipFile, encoding);
    }


    private static void unzip(String outDir, String zipFile, String encoding)
    throws IOException {
        ZipFile zip = new ZipFile(zipFile, Charset.forName(encoding));
        for (Enumeration enumeration = zip.entries();
                enumeration.hasMoreElements(); ) {
            ZipEntry    entry        = (ZipEntry) enumeration.nextElement();
            String      zipEntryName = entry.getName();
            InputStream in           = zip.getInputStream(entry);

            if (entry.isDirectory()) {      //处理压缩文件包含文件夹的情况
                File fileDir = new File(outDir, zipEntryName);
                fileDir.mkdirs();
                continue;
            }
            File file = new File(outDir, zipEntryName);
            System.out.println("Create  " + file);
            file.getParentFile().mkdirs();
            file.createNewFile();
            OutputStream out  = new FileOutputStream(file);
            byte[]       buff = new byte[1024];
            int          len;
            while ((len = in.read(buff)) > 0) {
                out.write(buff, 0, len);
            }
            in.close();
            out.close();
        }
    }

    private static void makeOutputDir(String outDir) {
        File outFileDir = new File(outDir);
        if (!outFileDir.exists()) {
            boolean isMakDir = outFileDir.mkdirs();
            if (isMakDir) {
                log.info("创建压缩目录成功");
            }
        }
    }

    private static class CmdParser {

        private String[] args;
        private String   outDir;
        private String   zipFile;
        private String   encoding;
        private boolean  help;

        public CmdParser(String... args) {
            this.args = args;
        }

        public String getOutDir() {
            return outDir;
        }

        public String getZipFile() {
            return zipFile;
        }

        public String getEncoding() {
            return encoding;
        }

        public boolean isHelp() {
            return help;
        }

        public CmdParser parse() {
            outDir   = "";
            zipFile  = null;
            encoding = Charset.defaultCharset().name();
            help     = false;
            for (int i = 0; i < args.length; i++) {
                if ("-o".equals(args[i])) {
                    i++;
                    if (i < args.length) {
                        outDir = args[i];
                    }
                } else if ("-e".equals(args[i])) {
                    i++;
                    if (i < args.length) {
                        encoding = args[i];
                    }
                } else if ("-i".equals(args[i])) {
                    i++;
                    if (i < args.length) {
                        zipFile = args[i];
                    }
                } else if ("-h".equals(args[i])) {
                    help = true;
                } else {
                    // ignore
                }
            }
            return this;
        }

        public void help() {
            System.err.println("unzip -i <zip_file> -o <output_directory> -e <path_encoding>");
        }
    }
}
/*
Shift_JIS
windows-31j
GBK
GB2312
GB18030
UTF-8
BIG5
BIG-HKSCS

8859_1 ISO 8859-1
8859_2 ISO 8859-2
8859_3 ISO 8859-3
8859_4 ISO 8859-4
8859_5 ISO 8859-5
8859_6 ISO 8859-6
8859_7 ISO 8859-7
8859_8 ISO 8859-8
8859_9 ISO 8859-9
Big5 Big5 码，繁体中文
CNS11643 CNS 11643，繁体中文
Cp037 美国、加拿大（两种语言，法语）、荷兰、葡萄牙、巴西、澳大利亚
Cp1006 IBM AIX 巴基斯坦（乌尔都语）
Cp1025 IBM 多语种西里尔语：保加利亚、波斯尼亚
黑塞哥维那、马其顿 (FYR)
Cp1026 IBM Latin-5，土耳其
Cp1046 IBM Open Edition US EBCDIC
Cp1097 IBM 伊朗（波斯语）/波斯
Cp1098 IBM 伊朗（波斯语）/波斯 (PC)
Cp1112 IBM 拉脱维亚，立陶宛
Cp1122 IBM 爱沙尼亚
Cp1123 IBM 乌克兰
Cp1124 IBM AIX 乌克兰
Cp1125 IBM 乌克兰 (PC)
Cp1250 Windows 东欧
Cp1251 Windows 斯拉夫语
Cp1252 Windows Latin-1
Cp1253 Windows 希腊
Cp1254 Windows 土耳其
Cp1255 Windows 希伯莱
Cp1256 Windows 阿拉伯
Cp1257 Windows 波罗的语
Cp1258 Windows 越南语
Cp1381 IBM OS/2, DOS 中华人民共和国 (PRC)
Cp1383 IBM AIX 中华人民共和国 (PRC)
Cp273 IBM 奥地利、德国
Cp277 IBM 丹麦、挪威
Cp278 IBM 芬兰、瑞典
Cp280 IBM 意大利
Cp284 IBM 加泰罗尼亚语/西班牙、拉丁美洲西班牙语
Cp285 IBM 英国、爱尔兰
Cp297 IBM 法国
Cp33722 IBM-eucJP - 日语 (5050 的超集)
Cp420 IBM 阿拉伯
Cp424 IBM 希伯莱
Cp437 MS-DOS 美国、澳大利亚、新西兰、南非
Cp500 EBCDIC 500V1
Cp737 PC 希腊
Cp775 PC 波罗的语
Cp838 IBM 泰国扩展 SBCS
Cp850 MS-DOS Latin-1
Cp852 MS-DOS Latin-2
Cp855 IBM 斯拉夫语
Cp857 IBM 土耳其语
Cp860 MS-DOS 葡萄牙语
Cp861 MS-DOS 冰岛语
Cp862 PC 希伯莱
Cp863 MS-DOS 加拿大法语
Cp864 PC 阿拉伯语
Cp865 MS-DOS 日尔曼语
Cp866 MS-DOS 俄语
Cp868 MS-DOS 巴基斯坦语
Cp869 IBM 现代希腊语
Cp870 IBM 多语种 Latin-2
Cp871 IBM 冰岛语
Cp874 IBM 泰国语
Cp875 IBM 希腊语
Cp918 IBM 巴基斯坦（乌尔都语）
Cp921 IBM 拉脱维亚、立陶宛(AIX, DOS)
Cp922 IBM 爱沙尼亚 (AIX, DOS)
Cp930 与 4370 UDC 混合的日语，5026 的超集
Cp933 与 1880 UDC 混合的韩文，5029 的超集
Cp935 与 1880 UDC 混合的简体中文主机，5031 的超集
Cp937 与 6204 UDC 混合的繁体中文，5033 的超集
Cp939 与 4370 UDC 混合的日语拉丁字母，5035 的超集
Cp942 日语 (OS/2)，932 的超集
Cp948 OS/2 中文（台湾），938 超集
Cp949 PC 韩文
Cp950 PC 中文（香港、台湾）
Cp964 AIX 中文（台湾）
Cp970 AIX 韩文
EUCJIS JIS, EUC 编码、日语
GB2312 GB2312, EUC 编码、简体中文
GBK GBK, 简体中文
ISO2022CN ISO 2022 CN, 中文
ISO2022CN_CNS ISO-2022-CN 形式的 CNS 11643，繁体中文
ISO2022CN_GB ISO-2022-CN 形式的 GB 2312，简体中文
ISO2022KR ISO 2022 KR, 韩文
JIS JIS, 日语
JIS0208 JIS 0208, 日语
KOI8_R KOI8-R, 俄语
KSC5601 KS C 5601, 韩文
MS874 Windows 泰国语
MacArabic Macintosh 阿拉伯语
MacCentralEurope Macintosh Latin-2
MacCroatian Macintosh 克罗地亚语
MacCyrillic Macintosh 斯拉夫语
MacDingbat Macintosh Dingbat
MacGreek Macintosh 希腊语
MacHebrew Macintosh 希伯莱语
MacIceland Macintosh 冰岛语
MacRoman Macintosh 罗马语
MacRomania Macintosh 罗马尼亚语
MacSymbol Macintosh 符号
MacThai Macintosh 泰国语
MacTurkish Macintosh 土耳其语
MacUkraine Macintosh 乌克兰语
SJIS Shift-JIS, 日语
UTF8 UTF-8
 */