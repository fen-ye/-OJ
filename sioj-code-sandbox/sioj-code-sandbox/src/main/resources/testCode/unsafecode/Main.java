import java.nio.file.*;
import java.util.*;
import java.io.*;


/**
 * 读取服务器文件资源（文件信息泄露）
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
        Process exec = Runtime.getRuntime().exec(filePath);
        exec.waitFor();
        BufferedReader in = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("危险程序执行成功");
    }
}
