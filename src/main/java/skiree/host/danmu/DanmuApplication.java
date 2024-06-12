package skiree.host.danmu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import skiree.host.danmu.core.Stratege;

@SpringBootApplication
@EnablePluginRegistries({Stratege.class})
public class DanmuApplication {

    public static void main(String[] args) {
        SpringApplication.run(DanmuApplication.class, args);
    }

}
