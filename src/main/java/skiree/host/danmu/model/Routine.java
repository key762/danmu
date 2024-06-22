package skiree.host.danmu.model;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Routine {
    private String id;
    private String name;
    private String delmark;
    private String rename;
    private String source;
    private String path;
    private String resource;
    @TableField(exist = false)
    private String resourceName;
    @TableField(exist = false)
    private String resourcePath;
    private String type;

    public static String randomId() {
        return "RE" + RandomUtil.randomString(4).toUpperCase();
    }

}