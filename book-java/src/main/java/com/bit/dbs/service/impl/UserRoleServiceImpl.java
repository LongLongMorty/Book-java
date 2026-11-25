package com.bit.dbs.service.impl;

import com.bit.dbs.entity.Role;
import com.bit.dbs.mapper.UserRoleMapper;
import com.bit.dbs.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author fanhongtao
 * 2022/10/13 15:32
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, Role> implements UserRoleService {
}
