import java.util.*;
/**
 * 无限占用空间
 */
public class Main {
    public static void main(String[] args) throws Exception {
        List<byte[]> list = new ArrayList<>();
        while (true) {
            list.add(new byte[10000]);
        }
    }
}
