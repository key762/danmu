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
    public String id;
    public String name;
    public String delmark;
    public String rename;
    public String source;
    @TableField(exist = false)
    public List<String> sources;
    public String path;
    @TableField(exist = false)
    public String fullPath;
    public String resource;
    @TableField(exist = false)
    public String resourceName;
    @TableField(exist = false)
    public String resourcePath;
    public String type;
    public String start;

    public static String randomId() {
        return "RE" + RandomUtil.randomString(4).toUpperCase();
    }

}