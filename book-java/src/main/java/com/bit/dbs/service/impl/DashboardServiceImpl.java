package com.bit.dbs.service.impl;

import com.bit.dbs.mapper.DashboardMapper;
import com.bit.dbs.service.DashboardService;
import com.bit.dbs.vo.DashboardData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int TREND_DAYS = 14;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardData getOverview() {
        DashboardData data = new DashboardData();

        DashboardData.Kpi kpi = new DashboardData.Kpi();
        kpi.setBookTotal(nvl(dashboardMapper.countBooks()));
        kpi.setStockTotal(nvl(dashboardMapper.sumStock()));
        kpi.setBorrowTotal(nvl(dashboardMapper.countBorrows()));
        kpi.setUserTotal(nvl(dashboardMapper.countUsers()));
        kpi.setTodayActions(nvl(dashboardMapper.countTodayActions()));
        data.setKpi(kpi);

        data.setBorrowTrend(buildTrend(dashboardMapper.selectBorrowTrend()));
        data.setTopBooks(safeList(dashboardMapper.selectTopBooks()));
        data.setStockTop(safeList(dashboardMapper.selectStockTop()));
        data.setRoleDistribution(safeList(dashboardMapper.selectRoleDistribution()));
        data.setActionDistribution(safeList(dashboardMapper.selectActionDistribution()));
        data.setRecentLogs(safeLogs(dashboardMapper.selectRecentLogs()));
        return data;
    }

    /**
     * 数据库只返回有借阅的日期，这里补齐最近 14 天，保证折线连续。
     */
    private List<DashboardData.TrendPoint> buildTrend(List<DashboardData.TrendPoint> rows) {
        Map<String, Long> counts = new HashMap<>();
        if (rows != null) {
            for (DashboardData.TrendPoint row : rows) {
                counts.put(row.getDate(), row.getCount() == null ? 0L : row.getCount());
            }
        }

        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();

        List<DashboardData.TrendPoint> result = new ArrayList<>(TREND_DAYS);
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            DashboardData.TrendPoint point = new DashboardData.TrendPoint();
            point.setDate(day.format(labelFormatter));
            point.setCount(counts.getOrDefault(day.format(keyFormatter), 0L));
            result.add(point);
        }
        return result;
    }

    private List<DashboardData.NameValue> safeList(List<DashboardData.NameValue> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private List<DashboardData.RecentLog> safeLogs(List<DashboardData.RecentLog> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private long nvl(Long value) {
        return value == null ? 0L : value;
    }
}
