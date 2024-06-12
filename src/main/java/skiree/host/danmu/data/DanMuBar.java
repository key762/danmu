package skiree.host.danmu.data;

import lombok.Data;

@Data
public class DanMuBar {
    public String pos;
    public Boolean mark;
    public double nTime;

    public DanMuBar(String pos) {
        this.pos = pos;
        this.mark = true;
    }

    public void next() {
        if (this.nTime <= 0) {
            this.mark = true;
        } else {
            this.nTime -= 1;
            if (this.nTime <= 0) {
                this.mark = true;
            }
        }
    }
}
