package com.itvers.toolbox.common;

import com.itvers.toolbox.util.LogUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GooglePlayVersion {
    private final static String TAG = GooglePlayVersion.class.getSimpleName(); // 디버그 태그

    /**
     * 구글플레이 앱버전
     *
     * @param packageName
     * @return
     */
    public static String getGooglePlayVersion(String packageName) {
        try {
            Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packageName).get();

            // 2017 이전
//            Elements elements = document.select(".content");
//            for (Element element : elements) {
//                if (element.attr("itemprop").equals("softwareVersion")) {
//                    return element.text().trim();
//                }
//            }

            // 2018 이전
//            Elements elements = document.select(".htlgb").eq(3);
//            for (Element element : elements) {
//                return element.text().trim();
//            }

            // 2018 이후
            Elements elements = document.body().getElementsByClass("xyOfqd").select(".hAyfc");
            return elements.get(3).child(1).child(0).child(0).ownText();

        } catch (IOException ioe) {
            LogUtil.e(TAG, "getGooglePlayVersion() -> IOException : " + ioe.getLocalizedMessage());
        }
        return null;
    }
}

