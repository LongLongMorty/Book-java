<template>
  <!-- 页面容器布局 -->
  <el-container class="home-container">
    <!-- 页面头部 -->
    <el-header class="site-header">
      <div class="brand">
        <span class="brand-seal" aria-hidden="true">书</span>
        <div class="brand-text">
          <span class="brand-name">数字化书籍服务系统</span>
          <span class="brand-sub">DIGITAL BOOK SERVICE</span>
        </div>
      </div>
      <div class="right">
        <router-link class="dash-entry" to="/dashboard">数据大屏</router-link>
        <span class="user-name">{{ $root.$data.userStatus.userName }}（{{ $root.$data.userStatus.roleName.replace(/（.*）$/, '') }}）</span>
        <el-dropdown @command="neDropdown">
          <span class="el-dropdown-link more-link">
            更多<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="pw">修改密码</el-dropdown-item>
            <el-dropdown-item v-if="$root.$data.canEdit" command="list">用户管理</el-dropdown-item>
            <el-dropdown-item @click.native="logout">退出</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="body-container">
      <!-- 页面侧边栏：书脊 -->
      <el-aside :width="isCollapse ? '64px' : '270px'" class="site-aside">
        <!-- 折叠标志 -->
        <div class="menu_collapse" @click="isCollapse = !isCollapse">
          {{ isCollapse ? '展' : '收' }}
        </div>
        <!-- 菜单导航 -->
        <el-menu class="site-menu" unique-opened
                 @open="handleOpen" @close="handleClose"
                 :collapse="isCollapse" :collapse-transition="false" router
                 :default-active="$route.path"
                 @select="handleSelect">
          <el-submenu :index="menu1.path" v-for="(menu1, index) in menuList"
                      :key="menu1.name + index" v-if="menu1.path !== '/sys' || $root.$data.canEdit">
            <!-- 一级菜单模板区 -->
            <template slot="title">
              <i :class="menu1.meta.icon" class="menu-icon"></i>
              <span v-text="menu1.name"></span>
            </template>

            <template v-if="menu1.children!=null">
              <!-- 二级菜单 -->
              <el-menu-item
                :index="menu2.path"
                v-for="(menu2, index) in menu1.children"
                :key="menu2.name + index"
                v-if="menu2.hidden">
                <span class="menu-dot" aria-hidden="true"></span>
                <span>{{ menu2.name }}</span>
              </el-menu-item>
            </template>

          </el-submenu>
        </el-menu>
      </el-aside>

      <el-container class="content-container">
        <!-- 页面主体区域：册页 -->
        <el-main class="site-main">
          <div class="page-shell">
            <router-view></router-view>
          </div>
        </el-main>

        <el-footer class="site-footer">
          <span>Copyright © 2026 BIT · 数字化书籍服务系统</span>
        </el-footer>

      </el-container>
    </el-container>
  </el-container>
</template>

<script>
import bus from '../utils/eventBus'

export default {
  components: {},
  data() {
    return {
      menuList: [],// 左侧菜单列表
      isCollapse: false,// 是否折叠左侧菜单
      lastPath: ''
    };
  },
  methods: {
    // 退出登录按钮点击事件
    async logout() {
      let {data: res} = await this.$http.post("/book/user/logout", {});
      this.$message.success(res.msg);
      // 清空jwt令牌
      window.sessionStorage.clear();
      // 重新跳转到登录页面
      await this.$router.push("/login");
      this.$root.$data.userStatus = {};
      this.$root.clearLogin();
    },
    async checkUserStatus() {
      if (!(await this.$root.checkLogin())) {
        this.$message.error("未登录！");
        await this.$router.push("/login");
      }
    },

    // 获取所有菜单
    getMenuList() {
      this.menuList = this.$router.options.routes.filter(
        (route) => route.hidden
      );
    },

    handleOpen(key, keyPath) {
      console.log('open')
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log('close')
      console.log(key, keyPath);
    },

    handleSelect(key, keyPath) {
      if (key === '/test') {
        bus.$emit('flush')
      }
      console.log('select')
      console.log(key, keyPath)
    },

    neDropdown(command) {
      switch (command) {
        case "pw":
          this.$router.push("/user/pwd");
          break;
        case "list":
          this.$router.push("/user/list");
          break;
      }
    }
  },
  created() {
    //获取菜单列表
    this.getMenuList();
    this.checkUserStatus();
  },
};
</script>

<style lang='less' scoped>
.home-container {
  height: 100%;
  background: var(--paper);
}

/* 顶栏 */
.site-header {
  flex: 0 0 72px;
  height: 72px !important;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  background: linear-gradient(180deg, #fbf7ee, #f4eee2);
  border-bottom: 1px solid var(--line);
  position: relative;
  z-index: 3;
}

.site-header::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -4px;
  height: 1px;
  background: rgba(185, 169, 142, 0.45);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-seal {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cinnabar);
  font-family: var(--kai);
  font-size: 24px;
  border: 2px solid var(--cinnabar);
  transform: rotate(-4deg);
  opacity: 0.9;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.brand-name {
  color: var(--ink);
  font-family: var(--serif);
  font-size: 21px;
  letter-spacing: 0.16em;
}

.brand-sub {
  color: var(--ink-3);
  font-size: 9px;
  letter-spacing: 0.34em;
}

.right {
  display: flex;
  align-items: center;
  gap: 18px;
  color: var(--ink-2);
  font-size: 13px;
}

.dash-entry {
  padding: 5px 10px;
  color: var(--ink-2);
  font-size: 12px;
  letter-spacing: 0.12em;
  border: 1px solid var(--line-strong);
  transition: color 0.15s ease, border-color 0.15s ease;
}

.dash-entry:hover {
  color: var(--cinnabar);
  border-color: var(--cinnabar);
}

.user-name {
  font-family: var(--serif);
  letter-spacing: 0.06em;
}

.user-name::before {
  content: "·";
  margin-right: 8px;
  color: var(--cinnabar);
}

.more-link {
  color: var(--ink-2);
  cursor: pointer;
  transition: color 0.15s ease;
}

.more-link:hover {
  color: var(--cinnabar);
}

.body-container {
  flex: 1;
  min-height: 0;
}

/* 侧栏：书脊 */
.site-aside {
  position: relative;
  background: var(--paper-3);
  border-right: 1px solid var(--line);
  overflow: hidden;
}

.site-aside::after {
  content: "";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(90deg, rgba(96, 76, 46, 0.02) 0, rgba(96, 76, 46, 0.02) 1px, transparent 1px, transparent 5px);
  pointer-events: none;
}

.menu_collapse {
  position: relative;
  z-index: 1;
  height: 34px;
  line-height: 34px;
  text-align: center;
  color: var(--ink-2);
  font-family: var(--serif);
  font-size: 12px;
  letter-spacing: 0.2em;
  border-bottom: 1px solid var(--line);
  cursor: pointer;
  user-select: none;
}

.menu_collapse:hover {
  color: var(--cinnabar);
}

.site-menu {
  position: relative;
  z-index: 1;
  height: calc(100% - 34px);
  overflow-y: auto;
  border-right: none;
  background: transparent;
}

.site-menu ::v-deep .el-submenu__title,
.site-menu ::v-deep .el-menu-item {
  height: 48px;
  line-height: 48px;
  color: var(--ink-2);
  font-family: var(--serif);
  letter-spacing: 0.1em;
  background: transparent;
}

.site-menu ::v-deep .el-submenu__title:hover,
.site-menu ::v-deep .el-menu-item:hover {
  color: var(--cinnabar);
  background: rgba(163, 59, 43, 0.05);
}

.site-menu ::v-deep .el-menu-item.is-active {
  color: var(--cinnabar);
  background: rgba(163, 59, 43, 0.08);
}

.site-menu ::v-deep .el-menu-item.is-active::before {
  content: "";
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  background: var(--cinnabar);
}

.site-menu ::v-deep .el-submenu .el-menu-item {
  min-width: 0;
  padding-left: 48px !important;
  background: transparent;
}

.site-menu ::v-deep .el-menu--inline {
  background: transparent;
}

.menu-icon {
  width: 16px;
  margin-right: 10px;
  color: var(--gold);
  font-size: 15px;
}

.site-menu ::v-deep .el-menu-item.is-active .menu-icon,
.site-menu ::v-deep .el-submenu.is-active .menu-icon {
  color: var(--cinnabar);
}

.menu-dot {
  display: inline-block;
  width: 4px;
  height: 4px;
  margin-right: 10px;
  border-radius: 50%;
  background: var(--gold);
  opacity: 0.85;
}

/* 主体 */
.site-main {
  padding: 26px 30px 18px;
  background: transparent;
  overflow: auto;
}

.site-footer {
  flex: 0 0 34px;
  height: 34px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-3);
  font-family: var(--serif);
  font-size: 11px;
  letter-spacing: 0.18em;
  background: transparent;
}

@media (max-width: 900px) {
  .site-header {
    padding: 0 14px;
  }

  .site-main {
    padding: 14px 12px 12px;
  }
}

@media (max-width: 760px) {
  .brand-name {
    font-size: 17px;
    letter-spacing: 0.08em;
  }

  .brand-sub,
  .user-name {
    display: none;
  }
}
</style>
