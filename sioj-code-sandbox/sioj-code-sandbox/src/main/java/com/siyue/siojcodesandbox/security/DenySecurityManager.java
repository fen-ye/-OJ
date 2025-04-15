package com.siyue.siojcodesandbox.security;

import java.security.Permission;

/**
 * 默认安全管理器
 */
public class DenySecurityManager extends SecurityManager{
    @Override
    public void checkPermission(Permission perm) {
        throw new SecurityException("权限不足");
//        super.checkPermission(perm);
    }
}
