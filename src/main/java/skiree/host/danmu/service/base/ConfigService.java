package skiree.host.danmu.service.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import skiree.host.danmu.dao.ConfigMapper;
import skiree.host.danmu.model.ass.AssConf;
import skiree.host.danmu.model.base.Config;
import skiree.host.danmu.service.tmdb.TMDBService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConfigService {

    @Autowired
    public ConfigMapper configMapper;

    public void buildModel(Model model) {
        List<Config> configs = configMapper.selectList(null);
        model.addAttribute("danMuRow", AssConf.row);
        model.addAttribute("danMuSize", AssConf.size);
        model.addAttribute("danMuBG", AssConf.border);
        model.addAttribute("danMuFormat", AssConf.format);
        if (configs != null && !configs.isEmpty()) {
            configs.forEach(config -> {
                model.addAttribute(config.key, config.value);
            });
        }
    }

    public void updateConfig() {
        List<Config> configs = configMapper.selectList(null);
        if (configs != null && !configs.isEmpty()) {
            configs.forEach(config -> {
                try {
                    if (config.key.equals("danMuRow")) {
                        AssConf.row = Integer.parseInt(config.value);
                    }
                    if (config.key.equals("danMuSize")) {
                        AssConf.size = Integer.parseInt(config.value);
                    }
                    if (config.key.equals("danMuBG")) {
                        AssConf.border = Integer.parseInt(config.value);
                    }
                    if (config.key.equals("danMuFormat")) {
                        AssConf.format = config.value;
                    }
                    if (config.key.equals("danMuSpeed")) {
                        AssConf.speed = Integer.parseInt(config.value);
                    }
                } catch (Exception ignore) {
                }
            });
        }
    }

    public void updateConfig(String danMuRow, String danMuSize, String danMuBG, String danMuFormat, String danMuSpeed) {
        List<Config> configs = new ArrayList<>();
        try {
            Integer.parseInt(danMuRow);
            Config c = new Config();
            c.setKey("danMuRow");
            c.setValue(danMuRow);
            configs.add(c);
        } catch (Exception ignore) {
        }
        try {
            Integer.parseInt(danMuSize);
            Config c = new Config();
            c.setKey("danMuSize");
            c.setValue(danMuSize);
            configs.add(c);
        } catch (Exception ignore) {
        }
        try {
            Integer.parseInt(danMuBG);
            Config c = new Config();
            c.setKey("danMuBG");
            c.setValue(danMuBG);
            configs.add(c);
        } catch (Exception ignore) {
        }
        if (danMuFormat != null && !danMuFormat.isEmpty()) {
            Config c = new Config();
            c.setKey("danMuFormat");
            c.setValue(danMuFormat);
            configs.add(c);
        }
        try {
            Integer.parseInt(danMuSpeed);
            Config c = new Config();
            c.setKey("danMuSpeed");
            c.setValue(danMuSpeed);
            configs.add(c);
        } catch (Exception ignore) {
        }
        // 全删全插
        configMapper.delete(null);
        configMapper.insert(configs);
    }
}
