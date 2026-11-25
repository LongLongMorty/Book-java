package com.bit.dbs.mapper;

import com.bit.dbs.entity.User;
import com.bit.dbs.model.UserModelForList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author fanhongtao
 * 2022/10/13 15:32
 */
public interface UserMapper extends BaseMapper<User> {
    List<UserModelForList> getList();
}
