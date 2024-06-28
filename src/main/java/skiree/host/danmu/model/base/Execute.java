package skiree.host.danmu.model.base;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Execute {
    public String id;
    public String routine;
    @TableField(exist = false)
    public String routineName;
    public String status;
    public String start;
    public String end;

    public Execute() {
    }

    public Execute(String id, String routine, String status) {
        this.id = id;
        this.routine = routine;
        this.status = status;
    }

    public static String randomId() {
        return "RE" + RandomUtil.randomString(6).toUpperCase();
    }
}
