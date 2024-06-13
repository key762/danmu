package skiree.host.danmu.core.iqiyi;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpUtil;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import skiree.host.danmu.core.iqiyi.modle.Node;
import skiree.host.danmu.data.DanMu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iQiYiAssEngine {

    public static Map<Long, List<DanMu>> getDanMu(Node node) {
        Map<Long, List<DanMu>> res = new HashMap<>();
        while (node.getDuration() == null || node.getId() == null) {
            try {
                String resultInfo = HttpUtil.get(node.getUrl(), CharsetUtil.CHARSET_UTF_8);
                Matcher matcher1 = Pattern.compile("\"videoDuration\":(\\d+),").matcher(resultInfo);
                if (matcher1.find()) {
                    int duration = Integer.parseInt(matcher1.group(1));
                    node.setDuration(duration);
                }
                Matcher matcher2 = Pattern.compile("window\\.QiyiPlayerProphetData=\\{\"tvid\":(\\d+),").matcher(resultInfo);
                if (matcher2.find()) {
                    String tvIdStr = matcher2.group(1);
                    node.setId(tvIdStr);
                }
            }catch (Exception e){
                node.setDuration(null);
                node.setId(null);
            }
        }
        int i_length = (int) Math.ceil(node.getDuration() / 300);
        for (int i = 1; i < i_length + 1; i++) {
            String i_url = "https://cmts.iqiyi.com/bullet/" + node.getId().substring(node.getId().length() - 4, node.getId().length() - 2) + "/" + node.getId().substring(node.getId().length() - 2) + "/" + node.getId() + "_300_" + i + ".z";
            getExtraDanMu(res, i_url);
        }
        return res;
    }

    public static void getExtraDanMu(Map<Long, List<DanMu>> all, String url) {
        Map<Long, List<DanMu>> res = new HashMap<>();
        try {
            String dmDecompressedString = new String(decompress(downloadData(url)), "UTF-8");
            Document document = XmlUtil.parseXml(dmDecompressedString);
            NodeList bulletInfoList = document.getDocumentElement().getElementsByTagName("bulletInfo");
            List<Element> bulletInfoElements = new ArrayList<>();
            for (int i = 0; i < bulletInfoList.getLength(); i++) {
                Element bulletInfoElement = (Element) bulletInfoList.item(i);
                bulletInfoElements.add(bulletInfoElement);
            }
            for (Element bulletInfoElement : bulletInfoElements) {
                String content = bulletInfoElement.getElementsByTagName("content").item(0).getTextContent();
                String likeCount = bulletInfoElement.getElementsByTagName("likeCount").item(0).getTextContent();
                String showTime = bulletInfoElement.getElementsByTagName("showTime").item(0).getTextContent();
                String color = bulletInfoElement.getElementsByTagName("color").item(0).getTextContent();
                Long offset = Long.parseLong(showTime) * 1000;
                DanMu danMu = new DanMu();
                danMu.setOffset(offset);
                danMu.setContent(content);
                danMu.setStyle(color);
                danMu.setScore(new BigDecimal(likeCount));
                if (res.containsKey(offset)) {
                    res.get(offset).add(danMu);
                } else {
                    List<DanMu> list = new ArrayList<>();
                    list.add(danMu);
                    res.put(offset, list);
                }
            }
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
        all.putAll(res);
    }

    public static byte[] downloadData(String url) throws IOException {
        URL dataUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) dataUrl.openConnection();
        connection.setRequestMethod("GET");
        try (InputStream inputStream = connection.getInputStream()) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    public static byte[] decompress(byte[] compressedData) throws IOException, CompressorException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream decompressorStream = new CompressorStreamFactory()
                .createCompressorInputStream(inputStream)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = decompressorStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return outputStream.toByteArray();
    }

}
