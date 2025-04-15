package com.siyue.siojcodesandbox.security;

import java.security.Permission;

/**
 * 默认安全管理器
 */
public class DefaultSecurityManager extends SecurityManager{
    // 默认不做任何的权限限制
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何的权限限制" + perm);
        super.checkPermission(perm);
    }
}
