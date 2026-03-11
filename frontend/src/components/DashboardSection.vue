<template>
  <section class="dashboard-section">
    <div class="section-header">
      <div class="header-accent"></div>
      <div class="header-text">
        <span class="title-main">{{ title }}</span>
        <span class="title-sub">/ {{ subTitle }}</span>
      </div>
      <div class="header-line"></div>
    </div>
    <div class="section-content">
      <slot></slot>
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: String,
  subTitle: String
})
</script>

<style scoped>
.dashboard-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 4px;
}

.header-accent {
  width: 4px;
  height: 24px;
  background: var(--cyan);
  box-shadow: 0 0 10px var(--cyan);
}

.header-text {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.title-main {
  font-size: 1.2rem;
  font-weight: 900;
  color: #fff;
  letter-spacing: 2px;
}

.title-sub {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.header-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, var(--border-color), transparent);
}

.section-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 严格限制为 3 列 */
  grid-auto-rows: 400px;
  gap: 24px;
}

@media (max-width: 1200px) {
  .section-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .section-content {
    grid-template-columns: 1fr;
  }
}
</style>
