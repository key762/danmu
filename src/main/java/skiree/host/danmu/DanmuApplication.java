package skiree.host.danmu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import skiree.host.danmu.core.Stratege;

@SpringBootApplication
@MapperScan("skiree.host.danmu.dao")
@EnablePluginRegistries({Stratege.class})
public class DanmuApplication {

    public static void main(String[] args) {
        SpringApplication.run(DanmuApplication.class, args);
    }

}
