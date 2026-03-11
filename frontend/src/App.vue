<template>
  <div class="cyber-app" :class="{ 'lang-switching': isSwitching }">
    <div class="grid-background"></div>
    <header class="cyber-header">
      <div class="header-left">
        <h1 class="header-title neon-text">{{ $t('header.title') }}</h1>
        <div class="header-status">
          <span class="status-indicator"></span>
          <span>{{ $t('header.system') }}</span>
          <span class="status-divider">|</span>
          <span class="network-icon">⚡</span>
          <span>{{ $t('header.network') }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="timezone-badge">{{ timezoneLabel }}</div>
        <div class="time-display digital-clock">{{ currentTime }}<span class="ms">{{ milliseconds }}</span></div>
        <el-button-group class="lang-switch">
          <el-button 
            :class="{ 'is-active': locale === 'zh' }" 
            @click="toggleLocale('zh')"
            size="small"
          >ZH</el-button>
          <el-button 
            :class="{ 'is-active': locale === 'en' }" 
            @click="toggleLocale('en')"
            size="small"
          >EN</el-button>
        </el-button-group>
      </div>
    </header>

    <main class="modular-matrix">
      <DashboardSection :title="$t('sections.news')" sub-title="FINANCIAL NEWS">
        <TimelineCard 
          v-for="board in sections.news" 
          :key="board.id"
          v-bind="board"
        />
      </DashboardSection>

      <DashboardSection :title="$t('sections.global')" sub-title="GLOBAL AFFAIRS">
        <TimelineCard 
          v-for="board in sections.global" 
          :key="board.id"
          v-bind="board"
        />
      </DashboardSection>

      <DashboardSection :title="$t('sections.ai')" sub-title="AI TRENDS">
        <TimelineCard 
          v-for="board in sections.ai" 
          :key="board.id"
          v-bind="board"
        />
      </DashboardSection>

      <DashboardSection :title="$t('sections.tech')" sub-title="TECH TRENDS">
        <TimelineCard 
          v-for="board in sections.tech" 
          :key="board.id"
          v-bind="board"
        />
      </DashboardSection>

      <DashboardSection :title="$t('sections.stocks')" sub-title="A-SHARE & US-STOCKS">
        <TimelineCard 
          v-for="board in sections.stocks" 
          :key="board.id"
          v-bind="board"
        />
      </DashboardSection>

      <DashboardSection :title="$t('sections.crypto')" sub-title="CRYPTO TERMINAL">
        <TimelineCard 
          v-for="board in sections.crypto" 
          :key="board.id"
          v-bind="board"
        />
      </DashboardSection>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import TimelineCard from './components/TimelineCard.vue'
import DashboardSection from './components/DashboardSection.vue'

// 配置 axios 基础路径
// 开发环境：直连本地 Spring Boot
// 部署环境（Nginx 反代）：保持同源，避免 CORS
axios.defaults.baseURL = import.meta.env.DEV ? 'http://localhost:8081' : ''

const { locale, t } = useI18n()
const currentTime = ref('')
const milliseconds = ref('')
const timezoneLabel = ref('')
const isSwitching = ref(false)

const toggleLocale = (lang) => {
  if (locale.value === lang) return
  isSwitching.value = true
  setTimeout(() => {
    locale.value = lang
    isSwitching.value = false
  }, 150)
}

const updateTime = () => {
  const now = new Date()
  
  // 获取城市级时区名称 (例如: Beijing Time, Sydney Time)
  const tzName = new Intl.DateTimeFormat('en-US', { timeZoneName: 'long' }).formatToParts(now)
    .find(part => part.type === 'timeZoneName').value;
  
  // 简化时区显示，提取城市或区域感
  let label = tzName.toUpperCase();
  if (label.includes('CHINA STANDARD TIME')) label = 'BEIJING TIME';
  if (label.includes('AUSTRALIAN EASTERN')) label = 'SYDNEY TIME';
  timezoneLabel.value = label;

  const options = { 
    year: 'numeric', month: '2-digit', day: '2-digit', 
    hour: '2-digit', minute: '2-digit', second: '2-digit', 
    hour12: false
  }
  const formatted = new Intl.DateTimeFormat('zh-CN', options).format(now)
  const parts = formatted.split(' ')
  currentTime.value = parts[0] + ' ' + parts[1]
  milliseconds.value = '.' + now.getMilliseconds().toString().padStart(3, '0')
}

// Modular Data Structure
const sections = reactive({
  news: [],
  global: [],
  ai: [],
  tech: [],
  stocks: [],
  crypto: []
})

const initData = () => {
  sections.news = [
    { id: 'n1', type: 'news', titleKey: 'boards.wallstreet', tagKey: 'tags.flash', data: [] },
    { id: 'n2', type: 'news', titleKey: 'boards.cailian', tagKey: 'tags.flash', data: [] },
    { id: 'n3', type: 'news', titleKey: 'boards.nasdaq', tagKey: 'tags.active', data: [] }
  ]
  sections.global = [
    { id: 'g1', type: 'geo', titleKey: 'boards.geopolitical', tagKey: 'tags.stable', data: [] }
  ]
  sections.ai = [
    { id: 'a1', type: 'tech', titleKey: 'boards.aitech', tagKey: 'tags.hot', data: [] }
  ]
  sections.tech = [
    { id: 't1', type: 'tech', titleKey: 'boards.aitech', tagKey: 'tags.active', boardIndex: 2, data: [] }
  ]
  sections.stocks = [
    { id: 's1', type: 'stock', titleKey: 'boards.stocks', tagKey: 'tags.index', data: [] },
    { id: 's2', type: 'stock', titleKey: 'boards.usStocks', tagKey: 'tags.index', data: [] }
  ]
  sections.crypto = [
    { id: 'c1', type: 'stock', titleKey: 'boards.crypto', tagKey: 'tags.volatile', data: [] }
  ]
}

const updateStockData = async () => {
  try {
    const [stockRes, cryptoRes] = await Promise.all([
      axios.get('/api/stock/realtime'),
      axios.get('/api/crypto/realtime')
    ])

    // 更新 A 股大盘 (sections.stocks[0])
    if (stockRes.data && stockRes.data.length > 0) {
      const aShareData = stockRes.data.filter(item => item.market === 'A_SHARE')
      const usShareData = stockRes.data.filter(item => item.market === 'US_SHARE')

      sections.stocks[0].data = aShareData.map(item => ({
        id: item.symbol,
        time: 0, 
        content: item.displayContent,
        isUp: item.isUp, 
        isNew: item.marketStatus !== 'CLOSED', // 核心：休市则不触发动画
        marketStatus: item.marketStatus
      }))
      
      sections.stocks[1].data = usShareData.map(item => ({
        id: item.symbol,
        time: 0, 
        content: item.displayContent,
        isUp: item.isUp, 
        isNew: item.marketStatus !== 'CLOSED', // 核心：休市则不触发动画
        marketStatus: item.marketStatus
      }))
      
      const currentStocks = [...sections.stocks[0].data, ...sections.stocks[1].data]
      setTimeout(() => {
        currentStocks.forEach(item => item.isNew = false)
      }, 1000)
    }

    // 更新币圈动向 (sections.crypto[0])
    if (cryptoRes.data && cryptoRes.data.length > 0) {
      sections.crypto[0].data = cryptoRes.data.map(item => ({
        id: item.symbol,
        time: 0, 
        content: item.displayContent,
        isUp: item.isUp, 
        isNew: true 
      }))
      
      const currentCryptos = sections.crypto[0].data
      setTimeout(() => {
        currentCryptos.forEach(item => item.isNew = false)
      }, 1000)
    }
  } catch (error) {
    console.warn('Failed to fetch realtime market data.')
  }
}

const updateDataFlow = async () => {
  // 仅从后端获取真实新闻数据
  try {
    const [wscnRes, cailianRes, yahooRes, globalRes, aiRes, techRes] = await Promise.all([
      axios.get('/api/news/wallstreet'),
      axios.get('/api/news/cailian'),
      axios.get('/api/news/yahoo'),
      axios.get('/api/news/global'),
      axios.get('/api/news/ai'),
      axios.get('/api/news/tech')
    ])

    if (wscnRes.data && wscnRes.data.length > 0) {
      sections.news[0].data = wscnRes.data.map(item => ({
        id: item.id,
        time: Math.floor((Date.now() - item.time) / 60000), 
        content: item.content,
        isNew: false
      })).slice(0, 10)
    }

    if (cailianRes.data && cailianRes.data.length > 0) {
      sections.news[1].data = cailianRes.data.map(item => ({
        id: item.id,
        time: Math.floor((Date.now() - item.time) / 60000), 
        content: item.content,
        isNew: false
      })).slice(0, 10)
    }

    if (yahooRes.data && yahooRes.data.length > 0) {
      sections.news[2].data = yahooRes.data.map(item => ({
        id: item.id,
        time: Math.floor((Date.now() - item.time) / 60000), 
        content: item.content,
        isNew: false
      })).slice(0, 10)
    }

    // 更新国际局势 (sections.global[0])
    if (globalRes.data && globalRes.data.length > 0) {
      sections.global[0].data = globalRes.data.map(item => ({
        id: item.id,
        time: item.time, // 已经是 HH:mm 格式
        content: item.content,
        isNew: false
      }))
    }

    // 更新 AI 动向 (sections.ai[0])
    if (aiRes.data && aiRes.data.length > 0) {
      sections.ai[0].data = aiRes.data.map(item => ({
        id: item.id,
        time: item.time, // 已经是 HH:mm 格式
        content: item.content,
        isNew: false
      }))
    }

    // 更新科技动向 (sections.tech[0])
    if (techRes.data && techRes.data.length > 0) {
      sections.tech[0].data = techRes.data.map(item => ({
        id: item.id,
        time: item.time, // 已经是 HH:mm 格式
        content: item.content,
        isNew: false
      }))
    }
  } catch (error) {
    console.warn('Failed to fetch news data from backend.')
  }
}

let timer, dataTimer, stockTimer, socket
const connectWebSocket = () => {
  const isDev = window.location.port === '5173'
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsHost = isDev ? 'localhost:8081' : window.location.host
  socket = new WebSocket(`${wsProtocol}//${wsHost}/ws/news`)
  
  socket.onmessage = (event) => {
    const payload = JSON.parse(event.data)
    const { type, data } = payload
    
    // 处理推送过来的新新闻
    const newItem = {
      id: data.id,
      time: 0, // 刚推送的视为 0 分钟前
      content: data.content,
      isNew: true
    }

    if (type === 'wallstreet') {
      sections.news[0].data.unshift(newItem)
      if (sections.news[0].data.length > 10) sections.news[0].data.pop()
    } else if (type === 'cailian') {
      sections.news[1].data.unshift(newItem)
      if (sections.news[1].data.length > 10) sections.news[1].data.pop()
    } else if (type === 'yahoo') {
      sections.news[2].data.unshift(newItem)
      if (sections.news[2].data.length > 10) sections.news[2].data.pop()
    }

    // 2秒后移除高亮
    setTimeout(() => { newItem.isNew = false }, 2000)
  }

  socket.onclose = () => {
    console.warn('WebSocket closed, reconnecting in 5s...')
    setTimeout(connectWebSocket, 5000)
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 16)
  initData()
  updateDataFlow() // 初始加载新闻
  updateStockData() // 初始加载股市
  dataTimer = setInterval(updateDataFlow, 60000) // 轮询降频，因为有了推送
  stockTimer = setInterval(updateStockData, 2000) // 股市高频轮询：2秒一次
  connectWebSocket()
})

onUnmounted(() => {
  clearInterval(timer)
  clearInterval(dataTimer)
  clearInterval(stockTimer)
  if (socket) socket.close()
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Share+Tech+Mono&display=swap');

:root {
  --bg-color: #0a0a0c;
  --panel-bg: rgba(20, 24, 33, 0.85);
  --cyan: #00f3ff;
  --magenta: #ff00ff;
  --neon-green: #39ff14;
  --yellow: #fcee0a;
  --text-primary: #e0e0e0;
  --text-secondary: #8a96a3;
  --border-color: rgba(0, 243, 255, 0.2);
}

body {
  margin: 0;
  padding: 0;
  background-color: var(--bg-color);
  color: var(--text-primary);
  font-family: 'Share Tech Mono', 'Courier New', Courier, monospace;
}

.cyber-app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: opacity 0.3s ease;
}

.cyber-app.lang-switching { opacity: 0.6; }

.grid-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(0, 243, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 243, 255, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  perspective: 1000px;
  transform: rotateX(20deg);
  transform-origin: top;
  z-index: -1;
  pointer-events: none;
}

.cyber-header {
  height: 70px;
  position: sticky;
  top: 0;
  padding: 0 30px;
  background: rgba(10, 10, 12, 0.95);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid var(--border-color);
  z-index: 1000;
  box-shadow: 0 0 20px rgba(0, 243, 255, 0.1);
}

.header-title {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 900;
  letter-spacing: 4px;
  text-transform: uppercase;
}

.neon-text {
  color: var(--cyan);
  text-shadow: 0 0 5px var(--cyan), 0 0 10px var(--cyan), 0 0 20px rgba(0, 243, 255, 0.5);
  animation: neon-pulse 2s infinite ease-in-out;
}

@keyframes neon-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

.header-status {
  font-size: 0.7rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 5px;
}

.status-indicator {
  width: 6px;
  height: 6px;
  background: var(--neon-green);
  border-radius: 50%;
  box-shadow: 0 0 8px var(--neon-green);
  animation: blink 1s infinite;
}

@keyframes blink { 50% { opacity: 0.3; } }

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.timezone-badge {
  background: rgba(0, 243, 255, 0.1);
  border: 1px solid var(--border-color);
  color: var(--cyan);
  padding: 2px 8px;
  font-size: 0.7rem;
  letter-spacing: 1px;
  font-weight: bold;
}

.digital-clock {
  font-size: 1.4rem;
  color: var(--yellow);
  text-shadow: 0 0 10px rgba(252, 238, 10, 0.3);
  min-width: 280px;
  text-align: right;
}

.ms {
  font-size: 0.8rem;
  opacity: 0.6;
  margin-left: 5px;
}

.lang-switch .el-button {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  font-family: inherit;
  transition: all 0.3s ease;
}

.lang-switch .el-button.is-active {
  background: var(--cyan);
  color: #000;
  box-shadow: 0 0 10px var(--cyan);
}

.modular-matrix {
  display: flex;
  flex-direction: column;
  gap: 48px;
  padding: 32px 0;
  max-width: 1400px; /* 限制中间内容最大宽度，约 3 个卡片宽 */
  margin: 0 auto; /* 水平居中实现两侧留白 */
  width: 90%; /* 保证在小屏幕下也有一定间距 */
}

/* 移除不再需要的 matrix-left 和 matrix-right 样式 */

/* Custom Scrollbar for Body */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover { background: var(--cyan); }

@media (max-width: 1200px) {
  .modular-matrix {
    padding: 16px;
    gap: 32px;
  }
}
</style>
