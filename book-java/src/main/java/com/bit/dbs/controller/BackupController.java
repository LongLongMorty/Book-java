package com.bit.dbs.controller;

import com.bit.dbs.entity.Privilege;
import com.bit.dbs.service.BackupService;
import com.bit.dbs.utils.ResultBody;
import com.bit.dbs.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * 系统管理 - 数据备份/恢复。
 * 仅管理员（PRI_EDIT）可操作。
 */
@RestController
@RequestMapping("/system/backup")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @GetMapping("/list")
    public ResultBody list() {
        checkPrivilege();
        return ResultBody.success("查询成功", backupService.list());
    }

    @PostMapping("/create")
    public ResultBody create() {
        checkPrivilege();
        return ResultBody.success("备份成功", backupService.create());
    }

    @DeleteMapping("/{name:.+}")
    public ResultBody delete(@PathVariable("name") String name) {
        checkPrivilege();
        backupService.delete(name);
        return ResultBody.success("删除成功");
    }

    @PostMapping("/restore/{name:.+}")
    public ResultBody restore(@PathVariable("name") String name) {
        checkPrivilege();
        backupService.restore(name);
        return ResultBody.success("恢复成功，请重新登录");
    }

    @GetMapping("/download/{name:.+}")
    public ResponseEntity<Resource> download(@PathVariable("name") String name) {
        checkPrivilege();
        File file = backupService.getFile(name);
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private void checkPrivilege() {
        UserUtils.checkPrivilege(Privilege.PRI_EDIT, "用户无权限进行数据备份操作");
    }
}
