/**
 * 无限睡眠
 */
public class Main {
    public static void main(String[] args) throws Exception {

        long ONE_HOUR = 60 * 60 * 1000L;
        Thread.sleep(ONE_HOUR);
        System.out.println("Hello World!终于睡醒了");
    }
}
