package skiree.host.danmu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultData {
    public int status;
    public String message;
    public Object data;

    public ResultData(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
