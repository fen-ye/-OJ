import java.nio.file.*;
import java.util.*;
import java.io.*;


/**
 * 向服务器写文件（植入危险程序）
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
        String errorProgram = "java -version 2>&1";
        Files.write(Paths.get(filePath), Arrays.asList(errorProgram));
        System.out.println("植入危险程序");
    }
}
