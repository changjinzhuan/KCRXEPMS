package cn.kcrxorg.kcrxepmsrs.mbutil;

public class MyTools {
    public static String addZeroForNum(String str, int strLength) {

        int strLen = str.length();

        if (strLen < strLength) {

            while (strLen < strLength) {

                StringBuffer sb = new StringBuffer();

                sb.append("0").append(str);// 左补0

                // sb.append(str).append("0");//右补0

                str = sb.toString();

                strLen = str.length();

            }

        }
        return str;
    }
}
