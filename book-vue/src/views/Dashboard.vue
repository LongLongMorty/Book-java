<template>
  <div class="dashboard">
    <header class="dash-head">
      <div class="brand">
        <span class="brand-seal" aria-hidden="true">数</span>
        <div class="brand-text">
          <h1>数据大屏</h1>
          <p>数字化书籍服务系统 · 馆藏与借阅实时概览</p>
        </div>
      </div>
      <div class="head-right">
        <span class="clock">{{ clock }}</span>
        <button class="dash-btn" @click="load">刷新</button>
        <router-link class="dash-btn" to="/home">返回系统</router-link>
      </div>
    </header>

    <section class="kpi-row">
      <div v-for="item in kpiCards" :key="item.label" class="kpi">
        <span class="kpi-label">{{ item.label }}</span>
        <span class="kpi-value">{{ item.value }}<small>{{ item.unit }}</small></span>
        <span class="kpi-line"></span>
      </div>
    </section>

    <section class="dash-grid">
      <div class="panel trend">
        <h2>近 14 天借阅趋势</h2>
        <div v-if="has('borrowTrend')" ref="trendChart" class="chart chart-lg"></div>
        <div v-else class="empty">暂无借阅数据</div>
      </div>

      <div class="panel roles">
        <h2>角色分布</h2>
        <div v-if="has('roleDistribution')" ref="roleChart" class="chart chart-lg"></div>
        <div v-else class="empty">暂无用户数据</div>
      </div>

      <div class="panel">
        <h2>借阅排行 Top 8</h2>
        <div v-if="has('topBooks')" ref="topBookChart" class="chart chart-md"></div>
        <div v-else class="empty">暂无借阅数据</div>
      </div>

      <div class="panel">
        <h2>库存 Top 8</h2>
        <div v-if="has('stockTop')" ref="stockChart" class="chart chart-md"></div>
        <div v-else class="empty">暂无库存数据</div>
      </div>

      <div class="panel">
        <h2>操作类型分布</h2>
        <div v-if="has('actionDistribution')" ref="actionChart" class="chart chart-md"></div>
        <div v-else class="empty">暂无日志数据</div>
      </div>

      <div class="panel logs">
        <h2>最近动态</h2>
        <ul v-if="has('recentLogs')" class="log-list">
          <li v-for="(log, index) in overview.recentLogs" :key="index">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-user">{{ log.userName }}</span>
            <span class="log-desc">{{ log.description }}</span>
          </li>
        </ul>
        <div v-else class="empty">暂无动态</div>
      </div>
    </section>

    <footer class="dash-foot">
      <span>数据来源：馆藏 / 借阅 / 用户 / 操作日志（只读）</span>
      <span>最后更新：{{ updatedAt || '—' }}</span>
    </footer>
  </div>
</template>

<script>
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const PALETTE = ['#c4553f', '#7fa392', '#c9a45c', '#8b9db3', '#b57f6a', '#9aa88f']
const TEXT = '#d8d2c4'
const MUTED = '#8e988b'
const LINE = 'rgba(217, 205, 184, 0.18)'
const SPLIT = 'rgba(217, 205, 184, 0.08)'

export default {
  data() {
    return {
      overview: {
        kpi: {},
        borrowTrend: [],
        topBooks: [],
        stockTop: [],
        roleDistribution: [],
        actionDistribution: [],
        recentLogs: []
      },
      clock: '',
      updatedAt: '',
      timer: null,
      refreshTimer: null,
      charts: {}
    };
  },

  computed: {
    kpiCards() {
      const k = this.overview.kpi || {};
      return [
        { label: '馆藏总量', value: k.bookTotal || 0, unit: '册' },
        { label: '馆藏总库存', value: k.stockTotal || 0, unit: '册' },
        { label: '在借册数', value: k.borrowTotal || 0, unit: '册' },
        { label: '注册用户', value: k.userTotal || 0, unit: '人' },
        { label: '今日操作', value: k.todayActions || 0, unit: '次' }
      ];
    }
  },

  methods: {
    has(key) {
      const value = this.overview[key];
      return Array.isArray(value) && value.length > 0;
    },

    async load() {
      try {
        const { data: res } = await this.$http.get('/dashboard/overview');
        if (res.code !== 200) {
          return this.$message.error(res.msg || '数据加载失败');
        }
        this.overview = res.data;
        const now = new Date();
        this.updatedAt = now.toLocaleTimeString('zh-CN', { hour12: false });
        this.$nextTick(this.renderCharts);
      } catch (e) {
        this.$message.error('数据加载失败，请稍后重试');
      }
    },

    tickClock() {
      this.clock = new Date().toLocaleString('zh-CN', { hour12: false });
    },

    renderCharts() {
      this.renderTrend();
      this.renderRoles();
      this.renderTopBooks();
      this.renderStockTop();
      this.renderActions();
    },

    getChart(refName) {
      const el = this.$refs[refName];
      if (!el) return null;
      let chart = echarts.getInstanceByDom(el);
      if (!chart) chart = echarts.init(el);
      this.charts[refName] = chart;
      return chart;
    },

    renderTrend() {
      if (!this.has('borrowTrend')) return;
      const chart = this.getChart('trendChart');
      if (!chart) return;
      const dates = this.overview.borrowTrend.map(item => item.date);
      const counts = this.overview.borrowTrend.map(item => item.count);
      chart.setOption({
        backgroundColor: 'transparent',
        animation: false,
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#1c211c',
          borderColor: LINE,
          textStyle: { color: TEXT }
        },
        grid: { left: 46, right: 24, top: 28, bottom: 32 },
        xAxis: {
          type: 'category',
          data: dates,
          boundaryGap: false,
          axisLine: { lineStyle: { color: LINE } },
          axisTick: { show: false },
          axisLabel: { color: MUTED, fontSize: 11 }
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: SPLIT } },
          axisLabel: { color: MUTED, fontSize: 11 }
        },
        series: [{
          name: '借阅',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: counts,
          lineStyle: { color: '#c4553f', width: 2 },
          itemStyle: { color: '#c4553f', borderColor: '#141814', borderWidth: 2 },
          areaStyle: { color: 'rgba(196, 85, 63, 0.16)' }
        }]
      }, true);
    },

    renderRoles() {
      if (!this.has('roleDistribution')) return;
      const chart = this.getChart('roleChart');
      if (!chart) return;
      chart.setOption({
        backgroundColor: 'transparent',
        animation: false,
        color: PALETTE,
        tooltip: {
          trigger: 'item',
          backgroundColor: '#1c211c',
          borderColor: LINE,
          textStyle: { color: TEXT }
        },
        legend: {
          bottom: 0,
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { color: MUTED, fontSize: 11 }
        },
        series: [{
          type: 'pie',
          radius: ['50%', '70%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          itemStyle: { borderColor: '#141814', borderWidth: 2 },
          label: { color: TEXT, fontSize: 11, formatter: '{b}\n{c}' },
          labelLine: { lineStyle: { color: LINE } },
          data: this.overview.roleDistribution
        }]
      }, true);
    },

    renderTopBooks() {
      if (!this.has('topBooks')) return;
      const chart = this.getChart('topBookChart');
      if (!chart) return;
      const rows = this.overview.topBooks.slice().reverse();
      chart.setOption({
        backgroundColor: 'transparent',
        animation: false,
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: '#1c211c',
          borderColor: LINE,
          textStyle: { color: TEXT }
        },
        grid: { left: 92, right: 24, top: 12, bottom: 20 },
        xAxis: {
          type: 'value',
          minInterval: 1,
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: SPLIT } },
          axisLabel: { color: MUTED, fontSize: 11 }
        },
        yAxis: {
          type: 'category',
          data: rows.map(item => item.name),
          axisLine: { lineStyle: { color: LINE } },
          axisTick: { show: false },
          axisLabel: { color: TEXT, fontSize: 11 }
        },
        series: [{
          type: 'bar',
          data: rows.map(item => item.value),
          barWidth: 12,
          itemStyle: { color: '#c9a45c', borderRadius: [0, 2, 2, 0] }
        }]
      }, true);
    },

    renderStockTop() {
      if (!this.has('stockTop')) return;
      const chart = this.getChart('stockChart');
      if (!chart) return;
      const rows = this.overview.stockTop.slice().reverse();
      chart.setOption({
        backgroundColor: 'transparent',
        animation: false,
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: '#1c211c',
          borderColor: LINE,
          textStyle: { color: TEXT }
        },
        grid: { left: 92, right: 24, top: 12, bottom: 20 },
        xAxis: {
          type: 'value',
          minInterval: 1,
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: SPLIT } },
          axisLabel: { color: MUTED, fontSize: 11 }
        },
        yAxis: {
          type: 'category',
          data: rows.map(item => item.name),
          axisLine: { lineStyle: { color: LINE } },
          axisTick: { show: false },
          axisLabel: { color: TEXT, fontSize: 11 }
        },
        series: [{
          type: 'bar',
          barWidth: 12,
          data: rows.map(item => ({
            value: item.value,
            itemStyle: {
              color: item.value <= 1 ? '#c4553f' : '#7fa392',
              borderRadius: [0, 2, 2, 0]
            }
          }))
        }]
      }, true);
    },

    renderActions() {
      if (!this.has('actionDistribution')) return;
      const chart = this.getChart('actionChart');
      if (!chart) return;
      chart.setOption({
        backgroundColor: 'transparent',
        animation: false,
        color: PALETTE,
        tooltip: {
          trigger: 'item',
          backgroundColor: '#1c211c',
          borderColor: LINE,
          textStyle: { color: TEXT }
        },
        legend: {
          bottom: 0,
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { color: MUTED, fontSize: 11 }
        },
        series: [{
          type: 'pie',
          radius: ['48%', '68%'],
          center: ['50%', '44%'],
          itemStyle: { borderColor: '#141814', borderWidth: 2 },
          label: { color: TEXT, fontSize: 11, formatter: '{b}\n{c}' },
          labelLine: { lineStyle: { color: LINE } },
          data: this.overview.actionDistribution
        }]
      }, true);
    },

    resizeCharts() {
      Object.keys(this.charts).forEach(key => {
        if (this.charts[key]) this.charts[key].resize();
      });
    }
  },

  async created() {
    this.tickClock();
    if (!(await this.$root.checkLogin())) {
      await this.$router.push('/login');
      return;
    }
    await this.load();
  },

  mounted() {
    this.timer = setInterval(this.tickClock, 1000);
    this.refreshTimer = setInterval(this.load, 60000);
    window.addEventListener('resize', this.resizeCharts);
  },

  beforeDestroy() {
    clearInterval(this.timer);
    clearInterval(this.refreshTimer);
    window.removeEventListener('resize', this.resizeCharts);
    Object.keys(this.charts).forEach(key => {
      if (this.charts[key]) this.charts[key].dispose();
    });
    this.charts = {};
  }
};
</script>

<style lang="less" scoped>
.dashboard {
  min-height: 100vh;
  padding: 18px 24px 12px;
  box-sizing: border-box;
  color: #f4eee2;
  background-color: #101410;
  background-image:
    radial-gradient(900px 480px at 12% -10%, rgba(196, 85, 63, 0.12), transparent 60%),
    radial-gradient(900px 520px at 92% 110%, rgba(127, 163, 146, 0.1), transparent 60%),
    linear-gradient(rgba(217, 205, 184, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(217, 205, 184, 0.035) 1px, transparent 1px);
  background-size: auto, auto, 44px 44px, 44px 44px;
}

.dash-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(217, 205, 184, 0.16);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-seal {
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c4553f;
  font-family: var(--kai);
  font-size: 24px;
  border: 2px solid #c4553f;
  transform: rotate(-4deg);
}

.brand-text h1 {
  margin: 0;
  color: #f4eee2;
  font-family: var(--serif);
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.18em;
}

.brand-text p {
  margin: 5px 0 0;
  color: #8e988b;
  font-size: 11px;
  letter-spacing: 0.18em;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #b9c1b3;
  font-size: 12px;
}

.clock {
  font-family: var(--serif);
  letter-spacing: 0.08em;
  color: #d8d2c4;
}

.dash-btn {
  padding: 6px 14px;
  color: #d8d2c4;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-decoration: none;
  background: transparent;
  border: 1px solid rgba(217, 205, 184, 0.28);
  cursor: pointer;
  transition: all 0.15s ease;
}

.dash-btn:hover {
  color: #f4eee2;
  border-color: #c9a45c;
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-top: 20px;
}

.kpi {
  position: relative;
  padding: 12px 14px 10px;
  background: rgba(24, 29, 24, 0.8);
  border: 1px solid rgba(217, 205, 184, 0.14);
}

.kpi::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 26px;
  height: 2px;
  background: #c9a45c;
}

.kpi-label {
  display: block;
  color: #8e988b;
  font-size: 12px;
  letter-spacing: 0.14em;
}

.kpi-value {
  display: block;
  margin-top: 8px;
  color: #f4eee2;
  font-family: var(--serif);
  font-size: 32px;
  font-weight: 600;
  line-height: 1;
}

.kpi-value small {
  margin-left: 5px;
  color: #8e988b;
  font-family: var(--sans);
  font-size: 11px;
  font-weight: 400;
}

.kpi-line {
  display: block;
  margin-top: 12px;
  height: 1px;
  background: linear-gradient(90deg, rgba(196, 85, 63, 0.7), transparent);
}

.dash-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 12px;
  margin-top: 12px;
}

.panel {
  position: relative;
  grid-column: span 4;
  padding: 12px 14px 8px;
  background: rgba(22, 27, 22, 0.82);
  border: 1px solid rgba(217, 205, 184, 0.14);
  min-width: 0;
}

.panel::before {
  content: "";
  position: absolute;
  top: -1px;
  left: -1px;
  width: 14px;
  height: 14px;
  border-top: 1px solid #c9a45c;
  border-left: 1px solid #c9a45c;
}

.panel.trend {
  grid-column: span 8;
}

.panel.roles {
  grid-column: span 4;
}

.panel.logs {
  grid-column: span 12;
}

.panel h2 {
  margin: 0 0 6px;
  color: #d8d2c4;
  font-family: var(--serif);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.14em;
}

.panel h2::before {
  content: "";
  display: inline-block;
  width: 3px;
  height: 12px;
  margin-right: 8px;
  vertical-align: -1px;
  background: #c4553f;
}

.chart {
  width: 100%;
}

.chart-lg {
  height: 230px;
}

.chart-md {
  height: 190px;
}

.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 190px;
  color: #6f796c;
  font-family: var(--serif);
  font-size: 13px;
  letter-spacing: 0.12em;
}

.log-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2px 28px;
  margin: 4px 0 0;
  padding: 0;
  list-style: none;
}

.log-list li {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 5px 0;
  border-bottom: 1px solid rgba(217, 205, 184, 0.07);
  font-size: 12px;
}

.log-time {
  flex: 0 0 auto;
  color: #8e988b;
  font-family: var(--serif);
  font-size: 11px;
}

.log-user {
  flex: 0 0 70px;
  color: #c4553f;
  font-family: var(--serif);
}

.log-desc {
  color: #c9c2b3;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.dash-foot {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 8px;
  color: #6f796c;
  font-size: 11px;
  letter-spacing: 0.08em;
  border-top: 1px solid rgba(217, 205, 184, 0.1);
}

@media (max-height: 900px) {
  .dashboard {
    padding: 14px 22px 10px;
  }

  .dash-head {
    padding-bottom: 8px;
  }

  .brand-seal {
    width: 40px;
    height: 40px;
    font-size: 21px;
  }

  .brand-text h1 {
    font-size: 22px;
  }

  .kpi-row {
    margin-top: 10px;
    gap: 10px;
  }

  .kpi {
    padding: 10px 14px 8px;
  }

  .kpi-value {
    font-size: 26px;
  }

  .dash-grid {
    gap: 10px;
    margin-top: 10px;
  }

  .panel {
    padding: 10px 12px 6px;
  }

  .chart-lg {
    height: 200px;
  }

  .chart-md {
    height: 168px;
  }

  .empty {
    height: 168px;
  }

  .log-list li {
    padding: 4px 0;
  }

  .dash-foot {
    margin-top: 8px;
    padding-top: 6px;
  }
}

@media (max-width: 1200px) {
  .panel,
  .panel.trend,
  .panel.roles,
  .panel.logs {
    grid-column: span 6;
  }

  .panel.trend,
  .panel.logs {
    grid-column: span 12;
  }

  .log-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .dashboard {
    padding: 14px;
  }

  .dash-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .head-right {
    flex-wrap: wrap;
  }

  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .panel,
  .panel.trend,
  .panel.roles,
  .panel.logs {
    grid-column: span 12;
  }
}
</style>
