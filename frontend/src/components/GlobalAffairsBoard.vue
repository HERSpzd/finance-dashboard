<template>
  <div class="cyber-board">
    <div class="board-header">
      <span class="header-icon">▶</span>
      <h3 class="board-title">GLOBAL AFFAIRS</h3>
      <div class="header-line"></div>
    </div>
    <div class="board-content">
      <div v-for="i in 5" :key="i" class="affair-item">
        <div class="affair-title">REGION-{{i}} STABILITY REPORT</div>
        <div class="affair-desc">Geopolitical tension detected in sector {{i * 10}}. Monitoring active.</div>
        <div class="affair-status" :class="{ 'warning': i % 2 === 0 }">STATUS: {{ i % 2 === 0 ? 'VOLATILE' : 'STABLE' }}</div>
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

.header-icon {
  color: var(--magenta);
  font-size: 0.8rem;
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
  margin-left: 10px;
}

.board-content {
  flex: 1;
  overflow-y: auto;
  font-size: 0.85rem;
}

.affair-item {
  margin-bottom: 20px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px dotted rgba(255, 0, 255, 0.2);
}

.affair-title {
  color: var(--magenta);
  font-weight: bold;
  margin-bottom: 5px;
}

.affair-desc {
  color: #888;
  margin-bottom: 5px;
}

.affair-status {
  font-size: 0.75rem;
  color: var(--cyan);
}

.affair-status.warning {
  color: var(--yellow);
  text-shadow: 0 0 5px var(--yellow);
}
</style>
