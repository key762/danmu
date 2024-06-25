package skiree.host.danmu.controller.api;

import cn.hutool.core.io.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
public class FileApiController {

    @GetMapping("/top-folders")
    @ResponseBody
    public List<String> getTopFolders() {
        if (FileUtil.isWindows()){
            return Arrays.stream(File.listRoots())
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
        }
        File rootDir = new File(File.separator);
        return getSubFolders(rootDir);
    }

    @PostMapping("/folders")
    @ResponseBody
    public List<String> getFolders(@RequestBody String folderPath) {
        folderPath = folderPath.replaceAll("\"", "");
        File selectedFolder = new File(folderPath);
        return getSubFolders(selectedFolder);
    }

    private List<String> getSubFolders(File dir) {
        List<String> folders = new ArrayList<>();
        File[] files = dir.listFiles(File::isDirectory);
        if (files != null) {
            for (File file : files) {
                if (!FileUtil.mainName(file).startsWith(".")) {
                    folders.add(file.getAbsolutePath());
                }
            }
        }
        return folders;
    }

}
