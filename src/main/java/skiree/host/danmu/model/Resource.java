package skiree.host.danmu.model;

import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    private String id;
    private String name;
    private String path;

    public static String randomId() {
        return "DB" + RandomUtil.randomString(4).toUpperCase();
    }

}