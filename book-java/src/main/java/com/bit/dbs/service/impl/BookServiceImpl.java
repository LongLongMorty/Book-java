package com.bit.dbs.service.impl;

import com.bit.dbs.entity.Book;
import com.bit.dbs.entity.BorrowInfo;
import com.bit.dbs.exception.BasicException;
import com.bit.dbs.mapper.BookMapper;
import com.bit.dbs.mapper.BorrowInfoMapper;
import com.bit.dbs.service.BookService;
import com.bit.dbs.vo.BookSearchReqData;
import com.bit.dbs.vo.PageRspData;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

/**
 * @author fanhongtao
 * 2022/10/13 15:32
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    private BorrowInfoMapper borrowInfoMapper;

    @Override
    public PageRspData<Book> listByPage(Integer pageNum, Integer pageSize) {
        return pageQuery(pageNum, pageSize);
    }

    @Override
    public PageRspData<Book> searchByPage(Integer pageNum, Integer pageSize, BookSearchReqData query) {
        LambdaQueryWrapper<Book> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotBlank(query.getBookName()), Book::getBookName, query.getBookName());
        wrapper.like(StringUtils.isNotBlank(query.getBookAuthor()), Book::getBookAuthor, query.getBookAuthor());
        IPage<Book> page = this.baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageRspData.of(page.getRecords(), page.getTotal(), page.getPages());
    }

    @Override
    @Transactional
    public boolean add(Book reqData) {
        LambdaQueryWrapper<Book> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Book::getISBN, reqData.getISBN());
        Book book = this.getOne(wrapper);
        if (ObjectUtil.isNotNull(book)) {
            throw new BasicException(400, "书籍信息已存在");
        }
        return this.save(reqData);
    }

    @Override
    @Transactional
    public boolean updateBook(Book reqData) {
        Book book = this.getById(reqData.getBookId());
        if (Objects.isNull(book)) {
            throw new BasicException(400, "书籍不存在");
        }
        return this.updateById(reqData);
    }

    @Override
    @Transactional
    public boolean removeBook(Integer bookId) {
        Book book = this.getById(bookId);
        if (Objects.isNull(book)) {
            throw new BasicException(400, "书籍不存在");
        }
        Number borrowCount = borrowInfoMapper.selectCount(
                Wrappers.<BorrowInfo>lambdaQuery().eq(BorrowInfo::getBookId, bookId));
        if (borrowCount != null && borrowCount.longValue() > 0) {
            throw new BasicException(400, "该书籍仍有借阅记录，请先归还后再删除");
        }
        return this.removeById(bookId);
    }

    @Override
    public boolean subOne(Integer bookId) {
        return this.baseMapper.subOne(bookId);
    }

    @Override
    @Transactional
    public int addAll(MultipartFile file) {
        int num = 0;
        Book book = new Book();
        try {
            InputStream bb = file.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(bb);
            BufferedReader reader = new BufferedReader(streamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                if (temp.length == 0) {
                    break;
                }
                book.setBookName(temp[0].trim());
                book.setBookAuthor(temp[1].trim());
                book.setISBN(temp[2].trim());
                book.setStock(Integer.parseInt(temp[3].trim()));
                if (add(book)) {
                    num++;
                } else {
                    throw new BasicException(400, "上传失败");
                }
            }
            reader.close();
            bb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    private PageRspData<Book> pageQuery(Integer pageNum, Integer pageSize) {
        IPage<Book> page = new Page<>(pageNum, pageSize);
        // 分页
        page = this.baseMapper.selectPage(page, null);
        // 构造结果
        List<Book> records = page.getRecords();
        return PageRspData.of(records, page.getTotal(), page.getPages());
    }
}
