package com.bit.dbs.mapper;

import com.bit.dbs.entity.Book;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author fanhongtao
 * 2022/10/13 15:32
 */
public interface BookMapper extends BaseMapper<Book> {
    boolean subOne(@Param("bookId") Integer bookId);
}
