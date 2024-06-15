package skiree.host.danmu.model.task;

import lombok.Data;

import java.util.List;

@Data
public class Detail {
    private String desc;
    private boolean enable;
    private boolean immediately;
    public boolean timer;
    private String cron;
    private String delMark;
    private String reName;
    private List<String> source;
    private String path;
    public String type;
}
