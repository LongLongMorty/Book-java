package com.bit.dbs.vo;

import lombok.Data;

import java.util.List;

/**
 * 数据大屏返回结构。只读聚合，不涉及任何表结构变更。
 */
@Data
public class DashboardData {

    private Kpi kpi;
    private List<TrendPoint> borrowTrend;
    private List<NameValue> topBooks;
    private List<NameValue> stockTop;
    private List<NameValue> roleDistribution;
    private List<NameValue> actionDistribution;
    private List<RecentLog> recentLogs;

    @Data
    public static class Kpi {
        private Long bookTotal;
        private Long stockTotal;
        private Long borrowTotal;
        private Long userTotal;
        private Long todayActions;
    }

    @Data
    public static class TrendPoint {
        private String date;
        private Long count;
    }

    @Data
    public static class NameValue {
        private String name;
        private Long value;
    }

    @Data
    public static class RecentLog {
        private String time;
        private String userName;
        private String description;
    }
}
