package me.asu.text;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JavaLexer {

    /* 50 个 */
    private static final String     reserveWords[] = {"abstract", "boolean",
            "break", "byte", "case", "catch", "char", "class", "continue",
            "default", "do", "double", "else", "extends", "final", "finally",
            "float", "for", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "strictfp", "enum", "goto",
            "const", "assert"};
    private              FileReader fd;
    private              int        state;
    private              char       ch;
    private              String     info; // 结果串
    private              String     temp; // 临时存储
    private              int        lineNum;

    public JavaLexer() {
        info    = "";
        temp    = "";
        lineNum = 1;

        analyze();

        write(info);
    }

    private void analyze() {
        getChar();
        do {
            if (ch == (char) -1 && temp.trim().isEmpty()) {
                // 已经读取到最后一个字符，且没有待处理字符
                return;
            }
            if (ch == '\n') {
                lineNum++;
            }
            switch (state) {
                case 0:
                    temp = "";
                    if (ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n') {
                        toNextCharAndChangeState(0);
                    } else if (ch == '/') {
                        toNextCharAndStoreTempAndChangeState(1);
                    } else if (isDigital(ch)) {
                        toNextCharAndStoreTempAndChangeState(5);
                    } else if (isOperator1(ch)) {
                        toNextCharAndStoreTempAndChangeState(8);
                    } else if (ch == '!') {
                        toNextCharAndStoreTempAndChangeState(9);
                    } else if (isOperator2(ch)) {
                        writeInfo((ch + ""), "运算符");
                        getChar();
                    } else if (isBoundary(ch)) {
                        writeInfo((ch + ""), "界符");
                        getChar();
                    } else if (ch == '"') {
                        toNextCharAndStoreTempAndChangeState(10);
                    } else if (isLetter(ch)) {
                        toNextCharAndStoreTempAndChangeState(11);
                    } else if (ch == '\'') {
                        toNextCharAndStoreTempAndChangeState(14);
                    } else if (ch == '-' || ch == '+') {
                        toNextCharAndStoreTempAndChangeState(16);
                    } else if (ch == '|') {
                        toNextCharAndStoreTempAndChangeState(17);
                    } else if (ch == '&') {
                        toNextCharAndStoreTempAndChangeState(18);
                    } else if (ch == (char) -1) {
                        // 程序应该结束
                    } else {
                        // 非法字符
                        error(1);
                        return;
                    }
                    break;
                case 1:
                    if (ch == '/') {
                        toNextCharAndChangeState(2);
                    } else if (ch == '*') {
                        toNextCharAndChangeState(3);
                    } else {
                        state = 8;
                    }
                    break;
                case 2:
                    // 处理注释
                    if (ch == '\n') {
                        state = 0;
                        System.out.println("Comment: " + temp);
                        getChar();
                    } else {
                        temp += ch;
                        getChar();
                    }
                    break;
                case 3:
                    // 处理注释
                    if (ch == '*') {
                        toNextCharAndChangeState(4);
                    } else {
                        temp += ch;
                        getChar();
                    }
                    break;
                case 4:
                    // 处理注释
                    if (ch == '/') {
                        toNextCharAndChangeState(0);
                        System.out.println("Comment: " + temp);
                    } else {
                        toNextCharAndChangeState(3);
                    }
                    break;
                case 5:
                    if (isDigital(ch)) {
                        temp += ch;
                        getChar();
                    } else {
                        state = 6;
                    }
                    break;
                case 6:
                    if (ch == '.') {
                        toNextCharAndStoreTempAndChangeState(7);
                    } else {
                        writeInfo(temp, "常数");
                    }
                    break;
                case 7:
                    if (isDigital(ch)) {
                        toNextCharAndStoreTempAndChangeState(13);
                    } else {
                        error(4);
                        return;
                    }
                    break;
                case 8:
                    if (ch == '=') {
                        temp += ch;
                        writeInfo(temp, "运算符");
                        getChar();
                    } else {
                        writeInfo(temp, "运算符");
                    }
                    break;
                case 9:
                    if (ch == '=') {
                        temp += ch;
                        writeInfo(temp, "运算符");
                        getChar();
                    } else {
                        writeInfo((ch + ""), "界符");
                        //error(2);
                        //return;
                    }
                    break;
                case 10:
                    if (ch == '"') {
                        temp += ch;
                        writeInfo(temp, "常量");
                        getChar();
                    } else if (ch == '\\') {
                        for (int i = 0; i < 2; i++) {
                            temp += ch;
                            getChar();
                        }
                        state = 10;
                    } else {
                        toNextCharAndStoreTempAndChangeState(10);
                    }
                    break;
                case 11:
                    if (isDigital(ch) || isLetter(ch) || ch == '_') {
                        toNextCharAndStoreTempAndChangeState(11);
                    } else {
                        state = 12;
                    }
                    break;
                case 12:

                    if (isReserve(temp)) {
                        writeInfo(temp, "保留字");
                        writeInfo((ch + ""), "界符");
                        getChar();
                    } else {
                        writeInfo(temp, "标识符");
                        writeInfo((ch + ""), "界符");
                        getChar();
                    }
                    break;
                case 13:
                    if (isDigital(ch)) {
                        toNextCharAndStoreTempAndChangeState(13);
                    } else {
                        writeInfo(temp, "常数");
                    }
                    break;
                case 14:
                    if (ch == '\'') {
                        temp += ch;
                        if (isLegalChar(temp)) {
                            writeInfo(temp, "常量");
                        } else {
                            error(9);
                            return;
                        }
                        getChar();
                    } else if (ch == '\\') {
                        for (int i = 0; i < 2; i++) {
                            temp += ch;
                            getChar();
                        }
                        state = 14;
                    } else {
                        toNextCharAndStoreTempAndChangeState(14);
                    }
                    break;
                case 16:
                    if (isDigital(ch)) {
                        toNextCharAndStoreTempAndChangeState(5);
                    } else {
                        state = 8;
                    }
                    break;
                case 17:
                    if (ch == '|') {
                        temp += ch;
                        writeInfo(temp, "运算符");
                        getChar();
                    } else {
                        writeInfo(temp, "运算符");
                    }
                    break;
                case 18:
                    if (ch == '&') {
                        temp += ch;
                        writeInfo(temp, "运算符");
                        getChar();
                    } else {
                        writeInfo(temp, "运算符");
                    }
                    break;
                default:
                    error(3);
                    return;
            }
        } while (true);

        //analyze();
    }

    private boolean isLegalChar(String temp) {
        char[]  ch          = temp.toCharArray();
        int     length      = ch.length;
        boolean isLegalChar = false;

        /*
         * Char a = ''; // error
         * char b = ' ';
         * // length = 3;
         * char c = '\n';
         * //length = 4;
         * \b \n \r \t \" \' \\
         * char d = '\122';
         * // length <= 6;
         */
        if (length == 2) { // ''
            isLegalChar = false;
        } else if (length == 3) {
            isLegalChar = true;
        } else if (length == 4) {
            if ((ch[1] == '\\') && (ch[2] == 'b' || ch[2] == 'n' || ch[2] == 'r'
                    || ch[2] == 't' || ch[2] == '\"' || ch[2] == '\''
                    || ch[2] == '\\' || isDigital(ch[2]))) {
                isLegalChar = true;
            }
        } else if (length <= 6) {
            if (ch[1] == '\\') {
                for (int i = 2; i < (length - 1); i++) {
                    if (!isDigital(ch[i])) {
                        isLegalChar = false;
                        break;
                    }
                    isLegalChar = true;
                }
            } else {
                System.out.println('*');
                isLegalChar = false;
            }
        } else {
            isLegalChar = false;
        }

        return isLegalChar;
    }

    private void toNextCharAndChangeState(int state) {
        temp += ch;
        this.state = state;
        getChar();
    }

    private void toNextCharAndStoreTempAndChangeState(int state) {
        temp += ch;
        this.state = state;
        getChar();
    }

    private boolean isReserve(String temp2) {
        for (int i = 0; i < 50; i++) {
            if (temp.equals(reserveWords[i])) {
                return true;
            }
        }
        return false;
    }

    private void writeInfo(String value, String type) {
        info += lineNum + ": < " + type + " , " + value + " >\r\n";
        state = 0;
    }

    private boolean isLetter(char ch) {
        if ((ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122) || ch > 128) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isBoundary(char ch) {
        if (ch == ',' || ch == ';' || ch == '(' || ch == ')' || ch == '['
                || ch == ']' || ch == '{' || ch == '}') {
            return true;
        }
        return false;
    }

    private boolean isOperator1(char ch) { // / * = < >
        if (ch == '/' || ch == '*' || ch == '=' || ch == '<' || ch == '>') {
            return true;
        }
        return false;
    }

    private boolean isOperator2(char ch) { // ? . :
        if (ch == '?' || ch == '.' || ch == ':') {
            return true;
        }
        return false;
    }

    private boolean isDigital(char ch) {
        if (ch >= 48 && ch <= 57) {
            return true;
        } else {
            return false;
        }
    }

    private void error(int i) {
        info += "\n词法分析出错\r\n错误定位：" + i;
    }

    private void getChar() {
        try {
            if (fd == null) {
                fd = new FileReader(
                        "src\\main\\java\\me\\asu\\text\\JavaLexer.java");
            }

            ch = (char) fd.read();

            if (ch == -1) {
                // 当从一个文件中读取数据时，在数据最后会返回一个int型-1来表示结束
                fd.close();
            }
        } catch (IOException e) {

        }
    }

    private void write(String info) {
        try {
            FileWriter fw = new FileWriter(
                    "target\\result.txt");

            fw.write(info);
            fw.flush(); // 刷新数据，将数据写入文件中

            fw.close();
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) throws IOException {
        new JavaLexer();
    }
}
