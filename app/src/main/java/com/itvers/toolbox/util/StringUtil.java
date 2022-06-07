package com.itvers.toolbox.util;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Pattern;

public class StringUtil {
    public static final String TAG = StringUtil.class.getSimpleName();  // 디버그 태그
    private static final char HANGUL_BEGIN_UNICODE = 44032;
    private static final char HANGUL_LAST_UNICODE = 55203;
    private static final char HANGUL_BASE_UNIT = 588;
    private static final char[] INITIAL_SOUND = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    /**
     * 문자열 공백 여부
     *
     * @param string
     * @return
     */
    public static boolean isNull(String string) {
        return !isNotNull(string);
    }

    /**
     * 문자열 공백 여부
     *
     * @param string
     * @return
     */
    public static boolean isNotNull(String string) {
        if ((string != null)
                && (!string.trim().equals(""))
                && (!string.trim().equalsIgnoreCase("null")
                && (string.length() > 0))) return true;
        return false;
    }

    /**
     * 배열 스트링화
     */
    public static String implodeArray(String separator, String... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            sb.append(data[i]);
            sb.append(separator);
        }
        if (data.length > 0) sb.append(data[data.length - 1].trim());
        return sb.toString();
    }

    /**
     * 자음 여부
     *
     * @param searchar
     * @return
     */
    private static boolean isInitialSound(char searchar) {
        for (char c : INITIAL_SOUND) {
            if (c == searchar) {
                return true;
            }
        }
        return false;
    }

    /**
     * 자음 가져오기
     * @param c
     * @return
     */
    public static char getInitialSound(char c) {
        int hanBegin = (c - HANGUL_BEGIN_UNICODE);
        int index = hanBegin / HANGUL_BASE_UNIT;
        return INITIAL_SOUND[index];
    }

    /**
     * 한글 여부
     *
     * @param c
     * @return
     */
    private static boolean isHangul(char c) {
        return HANGUL_BEGIN_UNICODE <= c && c <= HANGUL_LAST_UNICODE;
    }

    /**
     * 초성검색
     *
     * @param value
     * @param search
     * @return
     */
    public static boolean matchString(String value, String search) {
        int t = 0;
        int seof = value.length() - search.length();
        int slen = search.length();
        if (seof < 0)
            return false;
        for (int i = 0; i <= seof; i++) {
            t = 0;
            while (t < slen) {
                if (isInitialSound(search.charAt(t)) == true
                        && isHangul(value.charAt(i + t))) {
                    if (getInitialSound(value.charAt(i + t)) == search.charAt(t))
                        t++;
                    else
                        break;
                } else {
                    if (value.charAt(i + t) == search.charAt(t))
                        t++;
                    else
                        break;
                }
            }
            if (t == slen)
                return true;
        }
        return false;
    }

    /**
     * 숫자인지 여부
     * @param string
     * @return
     */
    public static boolean isNumber(String string) {
        return Pattern.matches("^[0-9]*$", string);
    }

    /**
     * 영대문자 여부
     * @param string
     * @return
     */
    public static boolean isEnglish(String string) {
        return Pattern.matches("^[a-zA-Z]*$", string);
    }

    /**
     * 한글 여부
     * @param string
     * @return
     */
    public static boolean isKorean(String string) {
        return Pattern.matches("^[가-힣]*$", string);
    }

    /**
     * 문자열 컬러 삽입
     * @param string
     * @param start
     * @param end
     * @return
     */
    public static SpannableStringBuilder addTextColor(String string, int start, int end, String color) {
        LogUtil.d(TAG, "addTextColor() -> string : " + string);
        LogUtil.d(TAG, "addTextColor() -> start : " + start);
        LogUtil.d(TAG, "addTextColor() -> end : " + end);
        LogUtil.d(TAG, "addTextColor() -> color : " + color);
        SpannableStringBuilder ssb = new SpannableStringBuilder(string);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    /**
     * 왼쪽으로 0 채우기
     *
     * @param string
     * @param length
     * @param fillChar
     * @return
     */
    public static String fillLeft(String string, int length, String fillChar) {
        String fillString = "";
        for (int i = 0; i < length; i++) {
            fillString += fillChar;
        }
        return fillString.substring(0, fillString.length() - string.length()) + string;
    }
}
