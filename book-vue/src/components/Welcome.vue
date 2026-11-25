<template>
  <div class="welcome">
    <section class="welcome-hero">
      <div class="hero-copy">
        <p class="eyebrow">数字化书籍服务系统</p>
        <h1>连接每一本书<br><em>服务每一位读者</em></h1>
        <p class="lede">馆藏有序，借还有度。以书为舟，在字里行间遇见更大的世界。</p>
        <ul class="hero-tags">
          <li>馆藏</li>
          <li>借阅</li>
          <li>归还</li>
          <li>日志</li>
        </ul>
      </div>

      <div class="hero-art" aria-hidden="true">
        <span class="art-sun"></span>
        <span class="art-mountain far"></span>
        <span class="art-mountain near"></span>
        <div class="book-stack">
          <span class="book b1"></span>
          <span class="book b2"></span>
          <span class="book b3"></span>
        </div>
        <span class="art-seal">阅</span>
      </div>
    </section>

    <blockquote class="welcome-quote">「 读书不觉已春深，一寸光阴一寸金。 」</blockquote>
  </div>
</template>

<script>
export default {};
</script>

<style lang='less' scoped>
.welcome {
  padding: 6px 4px 0;
}

.welcome-hero {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 40px;
  align-items: center;
  min-height: 430px;
}

.eyebrow {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 22px;
  color: var(--gold);
  font-family: var(--serif);
  font-size: 12px;
  letter-spacing: 0.28em;
}

.eyebrow::before {
  content: "";
  width: 34px;
  height: 1px;
  background: var(--gold);
}

.hero-copy h1 {
  margin: 0;
  color: var(--ink);
  font-family: var(--serif);
  font-size: 46px;
  font-weight: 600;
  line-height: 1.32;
  letter-spacing: 0.06em;
}

.hero-copy h1 em {
  color: var(--cinnabar);
  font-style: normal;
}

.lede {
  max-width: 30em;
  margin: 24px 0 0;
  color: var(--ink-2);
  font-size: 15px;
  line-height: 2;
  letter-spacing: 0.04em;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  margin: 30px 0 0;
  padding: 0;
  list-style: none;
}

.hero-tags li {
  padding: 0 18px;
  color: var(--ink-2);
  font-family: var(--serif);
  font-size: 13px;
  letter-spacing: 0.18em;
  border-left: 1px solid var(--line-strong);
}

.hero-tags li:first-child {
  padding-left: 0;
  border-left: none;
}

/* 纯 CSS 水墨插画 */
.hero-art {
  position: relative;
  height: 380px;
  overflow: hidden;
  border: 1px solid var(--line);
  background:
    linear-gradient(180deg, rgba(251, 247, 238, 0.2), rgba(239, 231, 216, 0.55)),
    radial-gradient(420px 260px at 72% 18%, rgba(176, 138, 74, 0.14), transparent 65%);
}

.hero-art::before {
  content: "";
  position: absolute;
  inset: 8px;
  border: 1px solid rgba(185, 169, 142, 0.35);
}

.art-sun {
  position: absolute;
  top: 42px;
  right: 54px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 40%, rgba(163, 59, 43, 0.3), rgba(163, 59, 43, 0.08) 62%, transparent 70%);
}

.art-mountain {
  position: absolute;
  left: -4%;
  right: -4%;
  bottom: 0;
}

.art-mountain.far {
  height: 56%;
  background: linear-gradient(180deg, rgba(47, 79, 73, 0.14), rgba(47, 79, 73, 0.03));
  clip-path: polygon(0 74%, 14% 54%, 28% 68%, 42% 38%, 56% 64%, 70% 34%, 84% 58%, 100% 44%, 100% 100%, 0 100%);
}

.art-mountain.near {
  height: 34%;
  background: linear-gradient(180deg, rgba(32, 36, 31, 0.26), rgba(32, 36, 31, 0.07));
  clip-path: polygon(0 84%, 12% 68%, 26% 80%, 40% 58%, 54% 78%, 68% 60%, 82% 78%, 100% 64%, 100% 100%, 0 100%);
}

.book-stack {
  position: absolute;
  left: 50%;
  bottom: 54px;
  width: 220px;
  height: 110px;
  transform: translateX(-50%);
}

.book {
  position: absolute;
  left: 50%;
  transform: translateX(-50%) rotate(-1.5deg);
  border-radius: 2px;
  box-shadow: 0 6px 14px rgba(55, 44, 28, 0.16);
}

.book::after {
  content: "";
  position: absolute;
  left: 10px;
  right: 10px;
  top: 7px;
  height: 1px;
  background: rgba(255, 253, 248, 0.55);
}

.b1 {
  bottom: 0;
  width: 214px;
  height: 30px;
  background: linear-gradient(180deg, #8f2f24, #6f241b);
}

.b2 {
  bottom: 32px;
  width: 196px;
  height: 28px;
  transform: translateX(-50%) rotate(1.2deg);
  background: linear-gradient(180deg, #3c5b54, #2f4f49);
}

.b3 {
  bottom: 62px;
  width: 178px;
  height: 26px;
  background: linear-gradient(180deg, #d8c8a9, #bfa77e);
}

.art-seal {
  position: absolute;
  top: 34px;
  left: 34px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cinnabar);
  font-family: var(--kai);
  font-size: 20px;
  border: 1px solid rgba(163, 59, 43, 0.65);
  transform: rotate(-7deg);
  opacity: 0.75;
}

.welcome-quote {
  margin: 34px 0 0;
  padding-top: 22px;
  color: var(--ink-3);
  font-family: var(--kai);
  font-size: 15px;
  letter-spacing: 0.16em;
  text-align: center;
  border-top: 1px solid var(--line);
}

@media (max-width: 900px) {
  .welcome-hero {
    grid-template-columns: 1fr;
    gap: 24px;
    min-height: 0;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .hero-art {
    height: 280px;
  }
}
</style>
