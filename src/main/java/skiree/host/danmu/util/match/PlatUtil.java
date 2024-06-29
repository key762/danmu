package skiree.host.danmu.util.match;

import org.apache.commons.lang3.StringUtils;

public class PlatUtil {

    public static String ckPlat(String url, String suf) {
        String temp = basePlat(url);
        if (temp.equals(url)) {
            return "DanMu" + suf;
        }
        temp = temp.replace("视频", "");
        return temp + suf;
    }

    public static String basePlat(String url) {
        if (!StringUtils.isEmpty(url)) {
            if (url.contains("v.qq.com")) {
                return "腾讯视频";
            }
            if (url.contains("www.iqiyi.com")) {
                return "爱奇艺视频";
            }
            if (url.contains("www.mgtv.com")) {
                return "芒果TV";
            }
            if (url.contains("v.youku.com")) {
                return "优酷视频";
            }
            if (url.contains("bilibili.com")) {
                return "Bilibili";
            }
        }
        return url;
    }

}