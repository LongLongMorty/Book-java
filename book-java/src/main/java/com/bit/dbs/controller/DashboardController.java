package com.bit.dbs.controller;

import com.bit.dbs.entity.Privilege;
import com.bit.dbs.service.DashboardService;
import com.bit.dbs.utils.ResultBody;
import com.bit.dbs.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据大屏：只读聚合，不产生任何数据变更。
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public ResultBody overview() {
        UserUtils.checkPrivilege(Privilege.PRI_READ, "用户无权限查看数据");
        return ResultBody.success("查询成功", dashboardService.getOverview());
    }
}
