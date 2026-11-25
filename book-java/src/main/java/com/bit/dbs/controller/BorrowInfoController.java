package com.bit.dbs.controller;

import com.bit.dbs.entity.BorrowInfo;
import com.bit.dbs.entity.Privilege;
import com.bit.dbs.entity.UserStatus;
import com.bit.dbs.exception.BasicException;
import com.bit.dbs.service.BookService;
import com.bit.dbs.service.BorrowInfoService;
import com.bit.dbs.utils.ResultBody;
import com.bit.dbs.utils.UserUtils;
import com.bit.dbs.vo.BookSearchReqData;
import com.bit.dbs.vo.BorrowInfoData;
import com.bit.dbs.vo.BorrowInfoSearchData;
import com.bit.dbs.vo.PageRspData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author fanhongtao
 * 2022/10/13 21:42
 */
@RestController
@RequestMapping("/borrow")
public class BorrowInfoController {

    @Autowired
    private BorrowInfoService borrowInfoService;

    @Autowired
    private BookService bookService;

    /**
     * 根据条件分页查询
     */
    @PostMapping("/search")
    public ResultBody searchByPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                   @RequestBody BorrowInfoSearchData query) {
        UserUtils.checkPrivilege(Privilege.PRI_READ, "用户无权限查看数据");
        PageRspData<BorrowInfo> pageRspData;
        if ("".equals(query.getBookName()) && "".equals(query.getUserName())) {
            pageRspData = borrowInfoService.listByPage(pageNum, pageSize);
        } else {
            pageRspData = borrowInfoService.searchByPage(pageNum, pageSize, query);
        }
        return ResultBody.success("查询成功", pageRspData);
    }

    //删除一个借阅信息
    @DeleteMapping
    public ResultBody deleteDetail(@RequestBody BorrowInfoData borrowInfo) {
        UserUtils.checkPrivilege(Privilege.PRI_EDIT, "用户无权限修改数据");
        Integer bookId = borrowInfo.getBookId();
        Integer userId = borrowInfo.getUserId();
        boolean flag = borrowInfoService.deleteOne(userId, bookId);
        if (!flag) {
            throw new BasicException(400, "归还失败");
        }
        return ResultBody.success("归还成功");
    }

    //借一本书
    @PostMapping
    public ResultBody borrowBook(@RequestBody BorrowInfoData borrowInfo) {
        UserUtils.checkPrivilege(Privilege.PRI_READ, "用户借阅权限");
        Integer userId = borrowInfo.getUserId();
        Integer bookId = borrowInfo.getBookId();
        boolean flag = borrowInfoService.borrowBook(bookId, userId);
        if (!flag) {
            throw new BasicException(400, "借阅失败");
        }
        return ResultBody.success("借阅成功");
    }
}
