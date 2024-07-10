package skiree.host.danmu.model.ass;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class AssConf {

    public static Integer speed = 6;

    public static Integer row = 4;

    public static Integer size = 63;

    public static Integer border = 2;

    public static String format = "${showTitle} - S${seasonNr2}E${episodeNr2} - ${title}";
}
