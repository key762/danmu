package skiree.host.danmu.model.base;

import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    public String id;
    public String name;
    public String path;
    public String start;

    public static String randomId() {
        return "DB" + RandomUtil.randomString(4).toUpperCase();
    }

}