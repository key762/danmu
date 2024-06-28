package skiree.host.danmu.model.base;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Routine {
    public String id;
    public String name;
    public String delmark;
    public String rename;
    private String season;
    private String tmdbId;
    private String doubanId;
    public String resource;
    @TableField(exist = false)
    public String resourceName;
    public String path;
    public String start;

    public static String randomId() {
        return "RE" + RandomUtil.randomString(4).toUpperCase();
    }

}