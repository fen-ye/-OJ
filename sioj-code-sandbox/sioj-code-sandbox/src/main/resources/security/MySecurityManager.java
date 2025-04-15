import java.security.Permission;

/**
 * 我的安全管理器
 *
 * 该类继承自SecurityManager，用于自定义安全管理器的行为。
 * 默认情况下，该类不对任何权限进行限制，但可以通过重写相关方法来实现自定义的权限控制。
 */
public class MySecurityManager extends SecurityManager {

    /**
     * 检查指定的权限
     *
     * 默认情况下，该方法不做任何权限限制，仅输出权限信息并调用父类的checkPermission方法。
     *
     * @param perm 要检查的权限对象
     */
    @Override
    public void checkPermission(Permission perm) {
//        System.out.println("默认不做任何的权限限制" + perm);
//        super.checkPermission(perm);
    }

    /**
     * 检查执行指定命令的权限
     *
     * 该方法调用父类的checkExec方法，不做任何额外的权限限制。
     *
     * @param cmd 要执行的命令
     */
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec 权限异常：" + cmd);
    }

    /**
     * 检查读取指定文件的权限，并考虑上下文
     *
     * 该方法调用父类的checkRead方法，不做任何额外的权限限制。
     *
     * @param file 要读取的文件路径
     */
    @Override
    public void checkRead(String file) {
//        if (file.contains("hutool")) return;
//        throw new SecurityException("checkRead 权限异常：" + file);
    }

    /**
     * 检查写入指定文件的权限
     *
     * 该方法调用父类的checkWrite方法，不做任何额外的权限限制。
     *
     * @param file 要写入的文件路径
     */
    @Override
    public void checkWrite(String file) {

//        throw new SecurityException("checkWrite 权限异常：" + file);

    }

    /**
     * 检查删除指定文件的权限
     *
     * 该方法调用父类的checkDelete方法，不做任何额外的权限限制。
     *
     * @param file 要删除的文件路径
     */
    @Override
    public void checkDelete(String file) {

//        throw new SecurityException("checkDelete 权限异常：" + file);
    }

    /**
     * 检查连接到指定主机和端口的权限
     *
     * 该方法调用父类的checkConnect方法，不做任何额外的权限限制。
     *
     * @param host 要连接的主机名或IP地址
     * @param port 要连接的端口号
     */
    @Override
    public void checkConnect(String host, int port) {
//        throw new SecurityException("checkConnect 权限异常：" + host + ":" + port);
    }
}
