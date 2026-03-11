<template>
  <div class="cyber-board">
    <div class="board-header">
      <span class="header-icon">▶</span>
      <h3 class="board-title">AI TECHNOLOGY</h3>
      <div class="header-line"></div>
    </div>
    <div class="board-content">
      <div class="ai-news">
        <div v-for="i in 4" :key="i" class="ai-card">
          <div class="ai-tag">NEURAL-NET</div>
          <div class="ai-title">GPT-{{i+4}} CORE ARCHITECTURE LEAKED</div>
          <div class="ai-meta">COMPUTE POWER: {{ i * 100 }} TFLOPS</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cyber-board {
  flex: 1;
  background: var(--panel-bg);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 0, 255, 0.3);
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
  border-top: 2px solid var(--magenta);
  border-left: 2px solid var(--magenta);
}

.board-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 10px;
}

.board-title {
  margin: 0;
  font-size: 1rem;
  color: var(--magenta);
  text-shadow: var(--neon-magenta);
  letter-spacing: 2px;
}

.header-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, var(--magenta), transparent);
}

.board-content {
  flex: 1;
  overflow-y: auto;
}

.ai-card {
  margin-bottom: 15px;
  padding: 10px;
  border-right: 3px solid var(--magenta);
  background: rgba(255, 0, 255, 0.03);
}

.ai-tag {
  font-size: 0.6rem;
  color: var(--magenta);
  border: 1px solid var(--magenta);
  display: inline-block;
  padding: 1px 4px;
  margin-bottom: 5px;
}

.ai-title {
  font-size: 0.85rem;
  color: var(--text-color);
  margin-bottom: 5px;
}

.ai-meta {
  font-size: 0.7rem;
  color: #666;
}
</style>
