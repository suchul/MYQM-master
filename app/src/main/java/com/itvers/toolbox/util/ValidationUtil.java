package com.itvers.toolbox.util;

import android.webkit.URLUtil;

import java.util.regex.Pattern;

public class ValidationUtil {

    /**
     * URL 유효성 검사
     *
     * @param url
     * @return
     */
    public static boolean isUrl(String url) {
        return URLUtil.isValidUrl(url);
    }

    /**
     * 이메일 유효성 검사
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email== null) return false;
        boolean isValidEmail = Pattern.matches("[\\w\\~\\-\\.]+@[\\w\\~\\-]+(\\.[\\w\\~\\-]+)+", email.trim());
        return isValidEmail;
    }

    /**
     * 휴대폰 유효성 검사
     *
     * @param cellphoneNumber
     * @return
     */
    public static boolean isCellphoneNumber(String cellphoneNumber) {
        String regex = "^\\s*(010|011|012|013|014|015|016|017|018|019)(-|\\)|\\s)*(\\d{3,4})(-|\\s)*(\\d{4})\\s*$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(cellphoneNumber).matches();
    }


    /**
     * 전화번호 유효성 검사
     *
     * @param telePhoneNumber
     * @return
     */
    public static boolean isTelePhoneNumber(String telePhoneNumber) {
        String regex = "^\\s*(02|031|032|033|041|042|043|051|052|053|054|055|061|062|063|064|070)?(-|\\)|\\s)*(\\d{3,4})(-|\\s)*(\\d{4})\\s*$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(telePhoneNumber).matches();
    }
}
