<template>
  <div>
    <el-breadcrumb separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>系统管理</el-breadcrumb-item>
      <el-breadcrumb-item>数据备份</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="backup-tip">
      <span class="tip-seal" aria-hidden="true">备</span>
      <div class="tip-text">
        <strong>备份与恢复说明</strong>
        <p>备份文件保存在服务器本地。恢复会覆盖当前数据库，系统会在恢复前自动生成一份安全备份；恢复完成后需要重新登录。</p>
      </div>
    </div>

    <div class="page-toolbar">
      <div class="toolbar-actions">
        <el-button type="primary" :loading="creating" @click="createBackup">立即备份</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
      <span class="backup-count">共 {{ files.length }} 份备份</span>
    </div>

    <div v-if="files.length > 0" class="table-scroll">
    <table class="page-table page-table--backup full-width">
      <tr>
        <th>文件名</th>
        <th style="width: 90px">大小</th>
        <th style="width: 150px">创建时间</th>
        <th style="width: 150px">操作</th>
      </tr>
      <tr v-for="row in files" :key="row.name">
        <td class="file-name" :title="row.name">{{ row.name }}</td>
        <td>{{ formatSize(row.size) }}</td>
        <td>{{ row.createdAt }}</td>
        <td class="backup-actions">
          <el-tooltip content="下载备份" placement="top">
            <el-button size="mini" circle icon="el-icon-download" @click="download(row)"></el-button>
          </el-tooltip>
          <el-tooltip content="恢复备份" placement="top">
            <el-button size="mini" circle type="primary" icon="el-icon-refresh-left"
                       :loading="restoringName === row.name" @click="restore(row)"></el-button>
          </el-tooltip>
          <el-tooltip content="删除备份" placement="top">
            <el-button size="mini" circle type="danger" icon="el-icon-delete"
                       :loading="deletingName === row.name" @click="remove(row)"></el-button>
          </el-tooltip>
        </td>
      </tr>
    </table>
    </div>

    <div v-if="!loading && files.length === 0" class="empty-state">
      <span class="empty-seal" aria-hidden="true">备</span>
      <p class="empty-title">暂无备份文件</p>
      <p class="empty-hint">点击「立即备份」生成第一份数据库备份</p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      files: [],
      loading: false,
      creating: false,
      restoringName: '',
      deletingName: ''
    };
  },

  methods: {
    async load() {
      this.loading = true;
      try {
        const { data: res } = await this.$http.get('/system/backup/list');
        if (res.code !== 200) {
          return this.$message.error(res.msg || '备份列表加载失败');
        }
        this.files = res.data || [];
      } catch (e) {
        this.$message.error('备份列表加载失败');
      } finally {
        this.loading = false;
      }
    },

    async createBackup() {
      this.creating = true;
      try {
        const { data: res } = await this.$http.post('/system/backup/create');
        if (res.code !== 200) {
          return this.$message.error(res.msg || '备份失败');
        }
        this.$message.success('备份成功：' + res.data.name);
        await this.load();
      } catch (e) {
        this.$message.error('备份失败，请检查服务器 mysqldump 配置');
      } finally {
        this.creating = false;
      }
    },

    async download(row) {
      try {
        const res = await this.$http.get('/system/backup/download/' + encodeURIComponent(row.name), {
          responseType: 'blob'
        });
        const blob = new Blob([res.data]);
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = row.name;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } catch (e) {
        this.$message.error('下载失败');
      }
    },

    restore(row) {
      this.$confirm(
        '恢复备份「' + row.name + '」将覆盖当前数据库，是否继续？系统会先自动生成一份安全备份。',
        '危险操作',
        {
          confirmButtonText: '确认恢复',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        this.restoringName = row.name;
        try {
          const { data: res } = await this.$http.post('/system/backup/restore/' + encodeURIComponent(row.name));
          if (res.code !== 200) {
            return this.$message.error(res.msg || '恢复失败');
          }
          this.$message.success(res.msg || '恢复成功，请重新登录');
          setTimeout(() => {
            this.$root.$data.userStatus = {};
            this.$root.clearLogin();
            this.$router.push('/login');
          }, 800);
        } catch (e) {
          this.$message.error('恢复失败，请查看服务器日志');
        } finally {
          this.restoringName = '';
        }
      }).catch(() => {
        this.$message.info('已取消恢复');
      });
    },

    remove(row) {
      this.$confirm('确定删除备份「' + row.name + '」？删除后不可恢复。', '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.deletingName = row.name;
        try {
          const { data: res } = await this.$http.delete('/system/backup/' + encodeURIComponent(row.name));
          if (res.code !== 200) {
            return this.$message.error(res.msg || '删除失败');
          }
          this.$message.success('删除成功');
          await this.load();
        } catch (e) {
          this.$message.error('删除失败');
        } finally {
          this.deletingName = '';
        }
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },

    formatSize(bytes) {
      if (!bytes) return '0 B';
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
      return (bytes / 1024 / 1024).toFixed(1) + ' MB';
    }
  },

  created() {
    this.load();
  }
};
</script>

<style lang="less" scoped>
.backup-tip {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
  padding: 16px 20px;
  background: rgba(239, 231, 216, 0.55);
  border: 1px solid var(--line);
}

.tip-seal {
  flex: 0 0 auto;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cinnabar);
  font-family: var(--kai);
  font-size: 20px;
  border: 1px solid rgba(163, 59, 43, 0.55);
  transform: rotate(-5deg);
}

.tip-text strong {
  display: block;
  margin-bottom: 6px;
  color: var(--ink);
  font-family: var(--serif);
  font-size: 14px;
  letter-spacing: 0.1em;
}

.tip-text p {
  margin: 0;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.9;
}

.backup-count {
  color: var(--ink-3);
  font-family: var(--serif);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.page-table--backup {
  table-layout: fixed;
}

.file-name {
  color: var(--ink);
  font-family: var(--serif);
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.page-table--backup .backup-actions .el-button {
  margin: 0 3px !important;
}

.page-table--backup td:first-child {
  color: var(--ink);
}

@media (max-width: 900px) {
  .page-table--backup {
    min-width: 700px;
  }
}
</style>
