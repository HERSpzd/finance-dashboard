<template>
  <div class="cyber-board">
    <div class="board-header">
      <span class="header-icon">▶</span>
      <h3 class="board-title">STOCK MARKET</h3>
      <div class="header-line"></div>
    </div>
    <div class="board-content">
      <table class="stock-table">
        <thead>
          <tr>
            <th>INDEX</th>
            <th>VALUE</th>
            <th>CHANGE</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="stock in stocks" :key="stock.name">
            <td class="stock-name">{{ stock.name }}</td>
            <td class="stock-price">{{ stock.price }}</td>
            <td :class="['stock-change', stock.change >= 0 ? 'up' : 'down']">
              {{ stock.change >= 0 ? '+' : '' }}{{ stock.changePercent }}%
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const stocks = ref([
  { name: 'S&P 500', price: '5,123.42', change: 12.5, changePercent: '0.24' },
  { name: 'NASDAQ', price: '16,274.94', change: -45.2, changePercent: '0.28' },
  { name: 'DJIA', price: '38,904.04', change: 150.3, changePercent: '0.39' },
  { name: 'NIKKEI', price: '39,582.00', change: -120.5, changePercent: '0.31' }
])
</script>

<style scoped>
.cyber-board {
  flex: 1.2;
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
}

.board-content {
  flex: 1;
  overflow-y: auto;
}

.stock-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}

.stock-table th {
  text-align: left;
  color: #666;
  padding: 8px;
  border-bottom: 1px solid rgba(0, 243, 255, 0.1);
}

.stock-table td {
  padding: 10px 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.stock-name {
  color: var(--text-color);
  font-weight: bold;
}

.stock-price {
  color: var(--yellow);
}

.stock-change.up {
  color: #00ff00;
  text-shadow: 0 0 5px rgba(0, 255, 0, 0.5);
}

.stock-change.down {
  color: #ff0000;
  text-shadow: 0 0 5px rgba(255, 0, 0, 0.5);
}
</style>
