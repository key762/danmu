package skiree.host.danmu.data;

import lombok.Data;

import java.util.List;

@Data
public class Detail {
    private String desc;
    private boolean enable;
    private boolean immediately;
    private String cron;
    private String delMark;
    private String reName;
    private List<String> source;
    private String path;
    public String type;
    public String tvId;
}
