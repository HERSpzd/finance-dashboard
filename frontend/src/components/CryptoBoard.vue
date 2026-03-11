<template>
  <div class="cyber-board">
    <div class="board-header">
      <span class="header-icon">▶</span>
      <h3 class="board-title">CRYPTO TERMINAL</h3>
      <div class="header-line"></div>
    </div>
    <div class="board-content">
      <div v-if="loading && !cryptoData.length" class="loading-text">INITIALIZING CONNECTION...</div>
      <div v-else class="crypto-list">
        <div v-for="item in cryptoData" :key="item.symbol" class="crypto-card">
          <div class="crypto-info">
            <span class="crypto-symbol">{{ item.symbol }}</span>
            <span class="crypto-price">$ {{ formatPrice(item.price) }}</span>
          </div>
          <div class="crypto-visual">
            <div class="pulse-line"></div>
          </div>
        </div>
      </div>
    </div>
    <div class="board-footer">
      <span class="footer-msg">ENCRYPTED FEED ACTIVE</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const cryptoData = ref([])
const loading = ref(true)

const formatPrice = (price) => {
  return parseFloat(price).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const fetchCryptoData = async () => {
  try {
    const response = await axios.get('/api/crypto/realtime')
    if (response.data && response.data.length > 0) {
      cryptoData.value = response.data
    }
  } catch (error) {
    console.error('CRYPTO_FETCH_ERROR:', error)
    // Mock data for initial UI if API fails
    if (cryptoData.value.length === 0) {
      cryptoData.value = [
        { symbol: 'BTCUSDT', price: '68432.12' },
        { symbol: 'ETHUSDT', price: '3821.45' },
        { symbol: 'SOLUSDT', price: '145.67' }
      ]
    }
  } finally {
    loading.value = false
  }
}

let timer
onMounted(() => {
  fetchCryptoData()
  timer = setInterval(fetchCryptoData, 5000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.cyber-board {
  height: 100%;
  background: var(--panel-bg);
  backdrop-filter: blur(15px);
  border: 1px solid var(--cyan);
  padding: 15px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(0, 243, 255, 0.1);
}

.cyber-board::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 20px;
  height: 20px;
  border-top: 3px solid var(--cyan);
  border-left: 3px solid var(--cyan);
}

.board-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 10px;
}

.board-title {
  margin: 0;
  font-size: 1.2rem;
  color: var(--cyan);
  text-shadow: var(--neon-blue);
  letter-spacing: 3px;
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

.loading-text {
  color: var(--cyan);
  animation: blink 1s infinite;
  text-align: center;
  margin-top: 50px;
}

@keyframes blink {
  50% { opacity: 0.3; }
}

.crypto-card {
  background: rgba(0, 243, 255, 0.05);
  border: 1px solid rgba(0, 243, 255, 0.2);
  margin-bottom: 15px;
  padding: 15px;
  position: relative;
  transition: all 0.3s;
}

.crypto-card:hover {
  background: rgba(0, 243, 255, 0.1);
  border-color: var(--cyan);
  transform: translateX(5px);
}

.crypto-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.crypto-symbol {
  color: var(--text-color);
  font-weight: bold;
  font-size: 1.1rem;
}

.crypto-price {
  color: var(--yellow);
  font-family: 'Courier New', Courier, monospace;
  font-size: 1.2rem;
  text-shadow: 0 0 10px rgba(252, 238, 10, 0.3);
}

.crypto-visual {
  height: 2px;
  background: rgba(0, 243, 255, 0.1);
  margin-top: 10px;
  position: relative;
  overflow: hidden;
}

.pulse-line {
  position: absolute;
  width: 30%;
  height: 100%;
  background: var(--cyan);
  box-shadow: 0 0 10px var(--cyan);
  animation: pulse 2s infinite linear;
}

@keyframes pulse {
  from { left: -30%; }
  to { left: 100%; }
}

.board-footer {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid rgba(0, 243, 255, 0.2);
  font-size: 0.7rem;
  color: rgba(0, 243, 255, 0.5);
}
</style>
