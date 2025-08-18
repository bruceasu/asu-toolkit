package me.asu.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;


/**
 * 2008-4-8 上午08:57:58
 * ReadMe:实现金额转换的工具类
 */
public class CurrencyUtil {

    /**
     * 转换当前数字金额为中文大写
     *
     * @param args BigDecimal类型的金额（万元）
     * @return 中文大写金额
     */
    public static String convertCurrencyToChinese(BigDecimal args) {
        String[] number = new String[]{"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        double testNumber;
        try {
            DecimalFormat df1 = new DecimalFormat("####.000000");
            testNumber = Double.valueOf(df1.format(args)).doubleValue() * 10000;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String destString = "";
        long longNumber1 = (long) testNumber;
        int xiaoshu = (int) ((testNumber * 100 - longNumber1 * 100) + 0.5);
        if (xiaoshu >= 100) // 四舍五入到个位
        {
            longNumber1 += 1;
        }

        int fen = xiaoshu % 10;
        xiaoshu = xiaoshu / 10;

        int jiao = xiaoshu % 10;

        if (longNumber1 > 10E12) {
            System.err.println("数目不能大于壹仟亿");
            return null;
        }

        // 转换分，角
        if (fen != 0) {
            destString = number[fen] + "分" + destString;
            if (jiao != 0) {
                destString = number[jiao] + "角" + destString;
            } else {
                destString = "零" + destString;
            }
        } else {
            if (jiao != 0) {
                destString = number[jiao] + "角" + destString + "整";
            } else {
                destString = "整";
            }
        }

        boolean hasZero = false;
        int yuanModel = (int) (longNumber1 % 10000);
        longNumber1 = longNumber1 / 10000;

        int wanModel = (int) (longNumber1 % 10000);
        longNumber1 = longNumber1 / 10000;

        int yiModel = (int) (longNumber1 % 10000);

        boolean yuanOver = false;
        boolean wanOver = false;

        int longNumber = yuanModel;
        if (longNumber != 0) // 转换圆
        {
            int yuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (yuan == 0) {
                destString = "圆" + destString;
                hasZero = true;
            } else {
                destString = number[yuan] + "圆" + destString;
            }
        } else {
            if (wanModel != 0 || yiModel != 0) {
                destString = "圆" + destString;
            }
            yuanOver = true;
        }

        if (longNumber != 0)// 转换十圆
        {
            int shiyuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (shiyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[shiyuan] + "拾" + destString;
                hasZero = false;
            }
        } else {
            if (wanModel != 0 && !yuanOver) {
                destString = "零" + destString;
            }
            yuanOver = true;
        }

        if (longNumber != 0)// 转换佰圆
        {
            int baiyuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (baiyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[baiyuan] + "佰" + destString;
                hasZero = false;
            }
        } else {
            if (wanModel != 0 && !yuanOver) {
                destString = "零" + destString;
            }
            yuanOver = true;
        }

        if (longNumber != 0)// 转换仟圆
        {
            int qianyuan = longNumber % 10;
            longNumber /= 10;
            if (qianyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[qianyuan] + "仟" + destString;
                hasZero = false;
            }
        } else {
            if (wanModel != 0 && !yuanOver) {
                destString = "零" + destString;
            }
        }

        longNumber = wanModel;
        if (longNumber != 0)// 转换万圆
        {
            int wanyuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (wanyuan == 0) {
                if (!hasZero) {
                    destString = "万" + destString;
                }
                hasZero = true;
            } else {
                destString = number[wanyuan] + "万" + destString;
                hasZero = false;
            }
        } else {
            if (yiModel != 0) {
                destString = "零" + destString;
            }
            wanOver = true;
        }

        if (longNumber != 0)// 转换十万圆
        {
            int shiwanyuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (shiwanyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[shiwanyuan] + "拾" + destString;
                hasZero = false;
            }
        } else {
            if (yiModel != 0 && !wanOver) {
                destString = "零" + destString;
            }
            wanOver = true;
        }

        if (longNumber != 0)// 转换百万圆
        {
            int baiwanyuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (baiwanyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[baiwanyuan] + "佰" + destString;
                hasZero = false;
            }
        } else {
            if (yiModel != 0 && !wanOver) {
                destString = "零" + destString;
            }
            wanOver = true;
        }

        if (longNumber != 0)// 转换千万圆
        {
            int qianwanyuan = longNumber % 10;
            longNumber /= 10;
            if (qianwanyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[qianwanyuan] + "仟" + destString;
                hasZero = false;
            }
        }

        longNumber = yiModel;

        if (longNumber != 0)// 转换亿圆
        {
            int yiyuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (yiyuan == 0) {
                if (!hasZero) {
                    destString = "亿" + destString;
                }
                hasZero = true;
            } else {
                destString = number[yiyuan] + "亿" + destString;
                hasZero = false;
            }
        }

        if (longNumber != 0)// 转换十亿圆
        {
            int shiyiyuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (shiyiyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[shiyiyuan] + "拾" + destString;
                hasZero = false;
            }
        }

        if (longNumber != 0)// 转换百亿圆
        {
            int baiyiyuan = longNumber % 10;
            longNumber = longNumber / 10;
            if (baiyiyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[baiyiyuan] + "佰" + destString;
                hasZero = false;
            }
        }

        if (longNumber != 0)// 转换千亿圆
        {
            int qianyiyuan = longNumber % 10;
            longNumber /= 10;
            if (qianyiyuan == 0) {
                if (!hasZero) {
                    destString = "零" + destString;
                }
                hasZero = true;
            } else {
                destString = number[qianyiyuan] + "仟" + destString;
                hasZero = false;
            }
        }

        return destString;
    }

    /**
     * 单位为万元的金额转换成完整金额
     *
     * @return String
     */
    public static String completeMoney(BigDecimal args) {
        String complete;
        try {
            DecimalFormat df1 = new DecimalFormat("####.000000");
            double testNumber = Double.valueOf(df1.format(args)).doubleValue() * 10000;
            complete = df1.format(testNumber);
        } catch (Exception e) {
            return null;
        }
        return complete;
    }
}
