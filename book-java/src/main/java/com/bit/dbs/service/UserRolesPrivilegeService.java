package com.bit.dbs.service;

import com.bit.dbs.entity.Privilege;
import com.bit.dbs.entity.Role2Privilege;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author fanhongtao
 * 2022/10/13 15:32
 */
public interface UserRolesPrivilegeService extends IService<Role2Privilege> {
    List<Role2Privilege> getByRoleId(Integer roleId);
    List<Privilege> getPrivilegesByRoleId(Integer roleId);
}
