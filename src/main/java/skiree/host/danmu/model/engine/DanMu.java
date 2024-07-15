package skiree.host.danmu.model.engine;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import skiree.host.danmu.model.ass.ASS;
import skiree.host.danmu.model.ass.AssConf;
import skiree.host.danmu.service.base.BaseService;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DanMu extends BaseService {

    private Long offset;
    private Long end;
    private Date time;
    private String content;
    private String style;
    private BigDecimal score;
    private int width;

    public String getStyle() {
        if (this.style != null && !this.style.isEmpty()) {
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
        if (this.style == null) return "";
        return style;
    }

    public String getDanMuTime() {
        return getDanMuTime(0L);
    }

    public String getDanMuTimeEnd() {
//        1920 / 6000 = 0.32
        double WIDTH_TIME = (double) 1920 / (AssConf.speed * 1000);
        int allWidth = this.width + 1920;
        double allTime = (allWidth / WIDTH_TIME);
        return getDanMuTime(Math.round(allTime));
    }

    public int gkTime() {
        double WIDTH_MULTIPLIER = (double) (AssConf.speed * 1000) / 1920;
        this.width = ASS.calculateWidth(this.content + "  ") + (AssConf.border * 2);
        double time = this.width * WIDTH_MULTIPLIER;
        return (int) Math.round(time);
    }

    public String getStart() {
        int widthStart = (int) Math.round(this.width / 2.0);
        return String.valueOf(widthStart + 1920);
    }

    public String getOutPix() {
        int widthEnd = (int) Math.round(this.width / 2.0);
        return String.valueOf(-widthEnd);
    }

    public String getDanMuTime(Long end) {
        return DurationFormatUtils.formatDuration(this.offset + end, "HH:mm:ss.SS");
    }

    public String getTimeMark() {
        double WIDTH_TIME = (double) 1920 / (AssConf.speed * 1000);
        int allWidth = this.width + 1920;
        double allTime = (allWidth / WIDTH_TIME);
        return String.valueOf(Math.round(allTime));
    }
}
