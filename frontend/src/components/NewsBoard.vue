<template>
  <div class="cyber-board">
    <div class="board-header">
      <span class="header-icon">▶</span>
      <h3 class="board-title">REAL-TIME NEWS</h3>
      <div class="header-line"></div>
    </div>
    <div class="board-content">
      <div v-for="i in 10" :key="i" class="news-item">
        <span class="news-time">[14:2{{i}}:00]</span>
        <span class="news-text">SECURE PROTOCOL UPDATED FOR BLOCKCHAIN NODE {{i}}...</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cyber-board {
  flex: 1;
  background: var(--panel-bg);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 243, 255, 0.3);
  padding: 15px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.cyber-board::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 10px;
  height: 10px;
  border-top: 2px solid var(--cyan);
  border-left: 2px solid var(--cyan);
}

.board-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 10px;
}

.header-icon {
  color: var(--cyan);
  font-size: 0.8rem;
}

.board-title {
  margin: 0;
  font-size: 1rem;
  color: var(--cyan);
  text-shadow: var(--neon-blue);
  letter-spacing: 2px;
}

.header-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, var(--cyan), transparent);
  margin-left: 10px;
}

.board-content {
  flex: 1;
  overflow-y: auto;
  font-size: 0.85rem;
  padding-right: 5px;
}

.news-item {
  margin-bottom: 12px;
  border-left: 2px solid transparent;
  padding-left: 10px;
  transition: all 0.3s;
}

.news-item:hover {
  border-left-color: var(--magenta);
  background: rgba(255, 0, 255, 0.05);
}

.news-time {
  color: var(--magenta);
  margin-right: 10px;
}

.news-text {
  color: var(--text-color);
}
</style>
