package com.bit.dbs.service;

import com.bit.dbs.entity.Book;
import com.bit.dbs.entity.BorrowInfo;
import com.bit.dbs.entity.UserLog;
import com.bit.dbs.vo.BookSearchReqData;
import com.bit.dbs.vo.BorrowInfoSearchData;
import com.bit.dbs.vo.PageRspData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author fanhongtao
 * 2022/10/13 15:32
 */
public interface BorrowInfoService extends IService<BorrowInfo> {

    PageRspData<BorrowInfo> listByPage(Integer pageNum, Integer pageSize);

    PageRspData<BorrowInfo> searchByPage(Integer pageNum, Integer pageSize, BorrowInfoSearchData query);

    boolean borrowBook(Integer bookId, Integer userId);

    boolean deleteOne(Integer userId, Integer bookId);
}
