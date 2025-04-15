import java.nio.file.*;
import java.util.*;
import java.io.*;


/**
 * 读取服务器文件资源（文件信息泄露）
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/application.yml";
        List<String> list = Files.readAllLines(Paths.get(filePath));
        System.out.println(String.join("\n", list));

    }
}
