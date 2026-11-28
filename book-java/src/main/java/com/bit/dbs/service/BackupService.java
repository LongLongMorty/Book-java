package com.bit.dbs.service;

import com.bit.dbs.vo.BackupFileInfo;

import java.io.File;
import java.util.List;

public interface BackupService {

    List<BackupFileInfo> list();

    BackupFileInfo create();

    void delete(String name);

    void restore(String name);

    File getFile(String name);
}
