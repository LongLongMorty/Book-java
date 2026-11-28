package com.bit.dbs.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 备份文件展示信息。备份文件放在磁盘目录中，不新增数据库表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupFileInfo {

    private String name;

    private long size;

    private String createdAt;
}
