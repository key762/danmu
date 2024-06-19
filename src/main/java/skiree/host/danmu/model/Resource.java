package skiree.host.danmu.model;

import cn.hutool.core.util.RandomUtil;
import lombok.Data;

@Data
public class Resource {
    private String id;
    private String name;
    private String path;

    public static String randomId() {
        return "DB" + RandomUtil.randomString(4).toUpperCase();
    }

}
