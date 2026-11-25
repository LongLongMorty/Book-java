package com.bit.dbs.service.impl;

import com.bit.dbs.entity.Privilege;
import com.bit.dbs.mapper.UserPrivilegeMapper;
import com.bit.dbs.service.UserPrivilegeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author fanhongtao
 * 2022/10/13 15:32
 */
@Service
public class UserPrivilegeServiceImpl extends ServiceImpl<UserPrivilegeMapper, Privilege> implements UserPrivilegeService {
}
