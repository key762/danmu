package skiree.host.danmu.util.match;

import org.apache.commons.lang3.StringUtils;

public class PlatUtil {

    public static String ckPlat(String url, String suf) {
        if (!StringUtils.isEmpty(url)) {
            if (url.contains("v.qq.com")) {
                return "腾讯" + suf;
            }
            if (url.contains("www.iqiyi.com")) {
                return "爱奇艺" + suf;
            }
            if (url.contains("www.mgtv.com")) {
                return "芒果TV" + suf;
            }
            if (url.contains("v.youku.com")) {
                return "优酷" + suf;
            }
        }
        return "DanMu" + suf;
    }

}
