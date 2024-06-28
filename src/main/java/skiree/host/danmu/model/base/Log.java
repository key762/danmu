package skiree.host.danmu.model.base;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("execute_log")
public class Log {
    @TableId
    public String id;
    public String execute;
    public String time;
    public String orderNum;
    public String content;


    public static String randomId() {
        return "LG" + RandomUtil.randomString(8).toUpperCase();
    }

}
