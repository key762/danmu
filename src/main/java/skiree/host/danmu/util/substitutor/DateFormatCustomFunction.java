package skiree.host.danmu.util.substitutor;

import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

@Component
public class DateFormatCustomFunction extends CustomFunction {
    @Override
    public String functionName() {
        return "FormatDate";
    }

    public String FormatDate(String dateStr, String format) throws Exception {
        return DateUtil.format(DateUtil.parse(dateStr), format);
    }


    public String FormatDate(String dateStr) throws Exception {
        return FormatDate(dateStr, "yyyy-MM-dd HH:mm:ss");
    }

    /*
            String input = "当前时间：$@{FormatDate(${beginDate},yyyy-MM-dd)},执行日期：$@{FormatDate(${end},yyyy-MM-dd)},$@{FormatMoney(123456.789)},$@{FormatMoney(123456.789,4)}";
            HashMap<String, String> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("beginDate", "2021-01-21 12:21:22");
            objectObjectHashMap.put("end", "2023-01-21 12:21:22");
            String output = substitutor.replace(input, objectObjectHashMap);
            System.out.println(output);
     */
}