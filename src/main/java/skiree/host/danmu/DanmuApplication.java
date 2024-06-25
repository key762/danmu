package skiree.host.danmu;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import skiree.host.danmu.service.core.Stratege;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.plugin.core.config.EnablePluginRegistries;

@MapperScan("skiree.host.danmu.dao")
@EnablePluginRegistries({Stratege.class})
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class DanmuApplication {

    public static void main(String[] args) {
        SpringApplication.run(DanmuApplication.class, args);
    }

}
