<template>
  <div class="login_container">
    <!-- 水墨背景 -->
    <div class="ink-scene" aria-hidden="true">
      <span class="sun"></span>
      <span class="mountain m-far"></span>
      <span class="mountain m-mid"></span>
      <span class="mountain m-near"></span>
      <span class="mist"></span>
    </div>

    <!-- 竖排对联 -->
    <aside class="couplet" aria-hidden="true">
      <span>连接每一本书</span>
      <span>服务每一位读者</span>
    </aside>

    <!-- 卷轴登录卡 -->
    <div class="login_scroll">
      <div class="scroll-rod top" aria-hidden="true"></div>
      <div class="scroll-rod bottom" aria-hidden="true"></div>
      <div class="seal" aria-hidden="true">阅</div>

      <header class="scroll-head">
        <p class="eyebrow">DIGITAL BOOK SERVICE</p>
        <h1>数字化书籍服务系统</h1>
        <p class="sub">一册在手 · 阅见古今</p>
      </header>

      <!-- 登录表单区域 -->
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0px" class="login_form">
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" placeholder="密码" prefix-icon="el-icon-lock" type="password"
                    show-password></el-input>
        </el-form-item>
        <!-- 登录和重置 -->
        <el-form-item class="btns">
          <el-button type="primary" class="login-btn" @click="submitForm">登 录</el-button>
          <el-button class="reset-btn" @click="resetForm">重 置</el-button>
        </el-form-item>
      </el-form>

      <footer class="scroll-foot">静心 · 阅世 · 知书</footer>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loginForm: {
        username: "admin",
        password: "123456",
      },
      rules: {
        username: [
          {required: true, message: "请输入用户名", trigger: "blur"},
          {min: 3, max: 10, message: "长度在3到10个字符", trigger: "blur"}
        ],
        password: [
          {required: true, message: "请输入密码", trigger: "blur"},
          {min: 6, max: 16, message: "长度在6到16个字符", trigger: "blur"}
        ],
      },
    };
  },

  methods: {
    // 提交登录请求
    submitForm() {
      this.$refs.loginFormRef.validate(async (valid) => {
        // 表单校验失败
        if (!valid) {
          console.log("error submit!!");
          return false;
        } else {
          const {data: res} = await this.$http.post("/book/user/login", this.loginForm);
          //登录失败
          if (res.code !== 200) {
            return this.$message.error(res.msg);
          }
          // 登录成功
          this.$message.success(res.msg);
          // 跳转到主页
          this.$root.$data.userStatus = res.data;
          this.$root.updatePriv();
          await this.$router.push("/home");
        }
      });
    },

    // 重置表单
    resetForm() {
      this.$refs.loginFormRef.resetFields();
    },
  },
};
</script>

<style lang="less" scoped>
.login_container {
  position: relative;
  height: 100%;
  overflow: hidden;
  background:
    radial-gradient(900px 520px at 78% 18%, rgba(176, 138, 74, 0.1), transparent 60%),
    linear-gradient(180deg, #f7f1e5 0%, #efe6d5 100%);
}

/* 水墨背景 */
.ink-scene {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.sun {
  position: absolute;
  right: 18%;
  top: 14%;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 40%, rgba(163, 59, 43, 0.22), rgba(163, 59, 43, 0.05) 65%, transparent 70%);
}

.mountain {
  position: absolute;
  left: -5%;
  right: -5%;
  bottom: 0;
}

.m-far {
  height: 46%;
  background: linear-gradient(180deg, rgba(47, 79, 73, 0.1), rgba(47, 79, 73, 0.02));
  clip-path: polygon(0 78%, 12% 58%, 24% 70%, 38% 42%, 52% 66%, 66% 38%, 80% 62%, 92% 48%, 100% 66%, 100% 100%, 0 100%);
}

.m-mid {
  height: 34%;
  background: linear-gradient(180deg, rgba(47, 79, 73, 0.16), rgba(47, 79, 73, 0.04));
  clip-path: polygon(0 82%, 10% 66%, 22% 78%, 36% 52%, 48% 74%, 62% 50%, 76% 72%, 88% 58%, 100% 76%, 100% 100%, 0 100%);
}

.m-near {
  height: 22%;
  background: linear-gradient(180deg, rgba(32, 36, 31, 0.22), rgba(32, 36, 31, 0.06));
  clip-path: polygon(0 88%, 14% 74%, 30% 86%, 46% 64%, 60% 82%, 74% 66%, 88% 84%, 100% 72%, 100% 100%, 0 100%);
}

.mist {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 16%;
  height: 90px;
  background: linear-gradient(180deg, transparent, rgba(247, 241, 229, 0.75), transparent);
}

/* 竖排对联 */
.couplet {
  position: absolute;
  left: 8%;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 28px;
  writing-mode: vertical-rl;
  color: rgba(32, 36, 31, 0.62);
  font-family: var(--kai);
  font-size: 22px;
  letter-spacing: 0.35em;
}

.couplet span {
  padding-left: 14px;
  border-left: 1px solid rgba(163, 59, 43, 0.25);
}

/* 卷轴卡片 */
.login_scroll {
  position: absolute;
  right: 16%;
  top: 50%;
  transform: translateY(-50%);
  width: 420px;
  max-height: 92vh;
  padding: 58px 42px 30px;
  background: var(--paper-2);
  border: 1px solid var(--line);
  box-shadow: 0 30px 70px rgba(55, 44, 28, 0.16);
  overflow: auto;
}

.login_scroll::before {
  content: "";
  position: absolute;
  inset: 8px;
  border: 1px solid rgba(185, 169, 142, 0.4);
  pointer-events: none;
}

.scroll-rod {
  position: absolute;
  left: -14px;
  right: -14px;
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(180deg, #8a6f4d, #c8b18b 45%, #7d6344);
  box-shadow: 0 2px 6px rgba(55, 44, 28, 0.25);
}

.scroll-rod.top {
  top: -6px;
}

.scroll-rod.bottom {
  bottom: -6px;
}

.seal {
  position: absolute;
  top: 26px;
  right: 30px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cinnabar);
  font-family: var(--kai);
  font-size: 22px;
  border: 2px solid var(--cinnabar);
  transform: rotate(-7deg);
  opacity: 0.85;
}

.scroll-head {
  margin-bottom: 28px;
  text-align: center;
}

.eyebrow {
  margin: 0 0 12px;
  color: var(--ink-3);
  font-size: 10px;
  letter-spacing: 0.42em;
}

.scroll-head h1 {
  margin: 0;
  color: var(--ink);
  font-family: var(--serif);
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.16em;
}

.sub {
  margin: 12px 0 0;
  color: var(--ink-2);
  font-family: var(--kai);
  font-size: 13px;
  letter-spacing: 0.2em;
}

.login_form {
  position: relative;
  z-index: 1;
  padding: 0;
}

.login_form ::v-deep .el-form-item {
  margin-bottom: 18px;
}

.login_form ::v-deep .el-input__inner {
  height: 44px;
  line-height: 44px;
  background: rgba(255, 253, 248, 0.92);
}

.btns {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  margin-bottom: 0;
}

.login-btn {
  flex: 1;
  height: 44px;
  letter-spacing: 0.3em;
}

.reset-btn {
  width: 96px;
  height: 44px;
  margin-left: 0;
}

.scroll-foot {
  margin-top: 22px;
  color: var(--ink-3);
  font-family: var(--kai);
  font-size: 12px;
  letter-spacing: 0.3em;
  text-align: center;
}

@media (max-width: 900px) {
  .login_scroll {
    right: 50%;
    transform: translate(50%, -50%);
    width: min(88vw, 420px);
    padding: 48px 26px 24px;
  }

  .sun {
    right: 8%;
  }
}

@media (max-width: 760px) {
  .couplet {
    display: none;
  }
}
</style>
