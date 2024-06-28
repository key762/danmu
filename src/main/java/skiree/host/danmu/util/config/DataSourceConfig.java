package skiree.host.danmu.util.config;

import cn.hutool.core.io.FileUtil;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Configuration
public class DataSourceConfig {

    @Value("classpath:db/danmu.db")
    private Resource databaseResource;

    @Bean
    public DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        try {
            String basePath = System.getProperty("user.dir") + "/".replace("file:/", "/") + "/db";
            String dbPath = basePath + "/danmu.db";
            Path databaseFile = Paths.get(dbPath);
            if (!Files.exists(databaseFile)) {
                FileUtil.mkdir(basePath);
                Files.copy(databaseResource.getInputStream(), databaseFile, StandardCopyOption.REPLACE_EXISTING);
            }
            dataSource.setUrl("jdbc:sqlite:" + dbPath);
        } catch (Exception e) {
            throw new RuntimeException("初始化danMu.db时异常", e);
        }
        dataSource.setValidationQuery("SELECT 1");
        return dataSource;
    }

}