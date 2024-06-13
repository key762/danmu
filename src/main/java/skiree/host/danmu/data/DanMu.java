package skiree.host.danmu.data;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DanMu {

    private Long offset;
    private Long end;
    private Date time;
    private String content;
    private String style;
    private BigDecimal score;

    public String getStyle() {
        if (!this.style.isEmpty()) {
            if (this.style.contains("gradient_colors")) {
                JSONArray jsonArray = JSONUtil.parseArray(JSONUtil.parse(this.style).getByPath("gradient_colors").toString());
                long endTime = (long) (1920.0 * AssConf.speed);
                String c1 = "0000FF";
                String c2 = "00FF00";
                try {
                    c1 = StringUtils.reverse(jsonArray.get(0).toString());
                    c2 = StringUtils.reverse(jsonArray.get(1).toString());
                } catch (Exception ignore) {
                }
                return "{\\c&H" + c1 + "&\\t(0," + (endTime) + ",\\c&H" + c2 + "&)}";
            } else if (this.style.length() == 6) {
                return "{\\c&H" + StringUtils.reverse(this.style) + "}";
            }
        }
        return style;
    }

    public String getDanMuTime() {
        if (this.offset < 6000L) {
            return getDanMuTime(0L);
        }
        return getDanMuTime(-6000L);
    }

    public double gkTime() {
        return (this.content.length() + 3) * 0.5625d;
    }

    public String getEndDanMuTime() {
        return getDanMuTime(24000L);
    }

    public String getOutPix() {
        return "-960";
    }

    public String getDanMuTime(Long end) {
        return DurationFormatUtils.formatDuration(this.offset + end, "HH:mm:ss.SS");
    }

}
