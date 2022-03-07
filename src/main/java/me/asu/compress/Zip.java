package me.asu.compress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

    public static void main(String[] args) throws IOException {
        CmdParser cmdParser = new CmdParser(args).parse();
        String    inDir     = cmdParser.getInDir();
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
        if (!Files.exists(Paths.get(inDir))) {
            System.err.printf("%s is not found.%n", zipFile);
            System.exit(3);
        }

        makeOutputDir(zipFile);
        zip(inDir, zipFile, encoding);
    }

    public static void zip(String inDir, String zipFile, String encoding)
    throws IOException {
//        ZipFile zip = new ZipFile(zipFile, Charset.forName(encoding));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zipFile)); // 创建ZipOutputStream类对象

        Path       path   = Paths.get(inDir).toAbsolutePath();
        List<Path> result = new LinkedList<>();
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new FindJavaVisitor(result));
            result.sort(Comparator.naturalOrder());
            for (Path p : result) {
                addEntry(out, p, path, encoding);
            }
        } else {
            addEntry(out, path, path.getParent(), encoding);
        }
        out.close();

    }

    private static class FindJavaVisitor extends SimpleFileVisitor<Path> {

        private List<Path> result;

        public FindJavaVisitor(List<Path> result) {
            this.result = result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            result.add(file.toAbsolutePath());
            return FileVisitResult.CONTINUE;
        }
    }

    private static void addEntry(ZipOutputStream out,
            Path path,
            Path baseDir,
            String encoding) throws IOException {
        System.out.println("Zipping " + path);
        String name    = baseDir.relativize(path).toString();
        String newName = new String(name.getBytes(encoding), "GBK");
        out.putNextEntry(new ZipEntry(newName)); // 创建新的进入点
        // 创建FileInputStream对象
        byte[] bytes = Files.readAllBytes(path);
        out.write(bytes);
    }

    private static void makeOutputDir(String zipFile) {
        File outFileDir = new File(zipFile);
        outFileDir.getParentFile().mkdirs();
    }

    private static class CmdParser {

        private String[] args;
        private String   inDir;
        private String   zipFile;
        private String   encoding;
        private boolean  help;

        public CmdParser(String... args) {
            this.args = args;
        }

        public String getInDir() {
            return inDir;
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
            inDir    = null;
            zipFile  = "a.zip";
            encoding = Charset.defaultCharset().name();
            help     = false;
            for (int i = 0; i < args.length; i++) {
                if ("-i".equals(args[i])) {
                    i++;
                    if (i < args.length) {
                        inDir = args[i];
                    }
                } else if ("-e".equals(args[i])) {
                    i++;
                    if (i < args.length) {
                        encoding = args[i];
                    }
                } else if ("-o".equals(args[i])) {
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
            System.err.println("zip -i <input_directory> -o <zip_file> -e <path_encoding>");
        }
    }
}
