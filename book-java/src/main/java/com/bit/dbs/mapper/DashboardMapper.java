package com.bit.dbs.mapper;

import com.bit.dbs.vo.DashboardData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据大屏只读查询，不写任何表。
 */
@Mapper
public interface DashboardMapper {

    Long countBooks();

    Long sumStock();

    Long countBorrows();

    Long countUsers();

    Long countTodayActions();

    List<DashboardData.TrendPoint> selectBorrowTrend();

    List<DashboardData.NameValue> selectTopBooks();

    List<DashboardData.NameValue> selectStockTop();

    List<DashboardData.NameValue> selectRoleDistribution();

    List<DashboardData.NameValue> selectActionDistribution();

    List<DashboardData.RecentLog> selectRecentLogs();
}
