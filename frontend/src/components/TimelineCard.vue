<template>
  <div class="timeline-card" :class="[`type-${type}`]">
    <div class="card-header">
      <div class="header-left">
        <div class="logo-wrapper">
          <slot name="logo">
            <svg class="industrial-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </slot>
        </div>
        <div class="title-section">
          <div class="main-title">
            {{ $t(titleKey) }} {{ boardIndex ? `CH-${boardIndex}` : '' }}
            <span class="tag" v-if="tagKey">{{ $t(tagKey) }}</span>
          </div>
          <div class="update-time">{{ updateTimeText }}</div>
        </div>
      </div>
      <div class="header-right">
        <div class="icon-btn"><svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"><path d="M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/></svg></div>
        <div class="icon-btn"><svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg></div>
        <div class="icon-btn"><svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/></svg></div>
      </div>
    </div>
    
    <div class="card-body">
      <div class="timeline-container">
        <TransitionGroup name="list">
          <div 
            v-for="item in data" 
            :key="item.id" 
            class="timeline-item" 
            :class="{ 
              'is-new': item.isNew,
              'up-item': type === 'stock' && item.isUp,
              'down-item': type === 'stock' && item.isUp === false
            }"
          >
            <div class="time-label">{{ formatTime(item.time) }}</div>
            <div class="content-text">{{ item.content[locale] }}</div>
            <span v-if="item.marketStatus" class="market-status" :class="item.marketStatus.toLowerCase()">
              {{ getStatusText(item.marketStatus) }}
            </span>
          </div>
        </TransitionGroup>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale, t } = useI18n()

const props = defineProps({
  titleKey: String,
  tagKey: String,
  boardIndex: Number,
  type: String,
  data: {
    type: Array,
    default: () => []
  }
})

const nowMs = ref(Date.now())
let nowTimer = null

onMounted(() => {
  nowTimer = setInterval(() => {
    nowMs.value = Date.now()
  }, 30000)
})

onUnmounted(() => {
  clearInterval(nowTimer)
})

const getStatusText = (status) => {
  const texts = {
    OPEN: t('marketStatus.open'),
    CLOSED: t('marketStatus.closed'),
    LUNCH_BREAK: t('marketStatus.lunchBreak')
  }
  return texts[status] || ''
}

const toMinutesAgo = (timeValue) => {
  if (typeof timeValue === 'number') {
    if (!Number.isFinite(timeValue)) return null
    return Math.max(0, Math.floor(timeValue))
  }

  if (typeof timeValue !== 'string') return null

  const trimmed = timeValue.trim()
  const match = trimmed.match(/^(\d{1,2}):(\d{2})(?::(\d{2}))?$/)
  if (!match) return null

  const hour = Number(match[1])
  const minute = Number(match[2])
  const second = match[3] ? Number(match[3]) : 0
  if (!Number.isFinite(hour) || !Number.isFinite(minute) || !Number.isFinite(second)) return null

  const now = new Date(nowMs.value)
  const candidate = new Date(now)
  candidate.setHours(hour, minute, second, 0)

  if (candidate.getTime() - now.getTime() > 60000) {
    candidate.setDate(candidate.getDate() - 1)
  }

  const diffMs = now.getTime() - candidate.getTime()
  return Math.max(0, Math.floor(diffMs / 60000))
}

const latestMinutesAgo = computed(() => {
  const items = Array.isArray(props.data) ? props.data : []
  if (items.length === 0) return null

  const minutesList = items
    .map((item) => toMinutesAgo(item?.time))
    .filter((v) => typeof v === 'number')

  if (minutesList.length === 0) return null
  return Math.min(...minutesList)
})

const updateTimeText = computed(() => {
  if (latestMinutesAgo.value == null) return t('common.refresh')
  if (latestMinutesAgo.value === 0) return t('common.justNow')
  return t('common.minutesAgo', { n: latestMinutesAgo.value })
})

const formatTime = (time) => {
  if (typeof time === 'string') return time // 如果是格式化好的字符串，直接返回
  if (time === 0) return t('common.justNow')
  return t('common.minutesAgo', { n: time })
}
</script>

<style scoped>
.timeline-card {
  --neon-color: var(--cyan);
  background-color: var(--panel-bg);
  backdrop-filter: blur(10px) saturate(180%);
  border: 1px solid var(--border-color);
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  box-sizing: border-box;
  min-height: 0;
  clip-path: polygon(
    0 0, 
    calc(100% - 15px) 0, 100% 15px, 
    100% 100%, 
    15px 100%, 0 calc(100% - 15px)
  );
  animation: border-breathe 4s infinite ease-in-out;
}

@keyframes border-breathe {
  0%, 100% { border-color: rgba(0, 243, 255, 0.2); box-shadow: inset 0 0 10px rgba(0, 243, 255, 0.05); }
  50% { border-color: rgba(0, 243, 255, 0.5); box-shadow: inset 0 0 20px rgba(0, 243, 255, 0.15); }
}

.type-crypto { --neon-color: var(--neon-green); }
.type-geo { --neon-color: var(--magenta); }
.type-stock { --neon-color: var(--yellow); }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding-bottom: 10px;
}

.header-left {
  display: flex;
  gap: 12px;
}

.logo-wrapper {
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--neon-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--neon-color);
  box-shadow: 0 0 10px rgba(0, 243, 255, 0.2);
}

.industrial-icon {
  width: 20px;
  height: 20px;
}

.main-title {
  font-size: 1rem;
  font-weight: 900;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  letter-spacing: 1px;
}

.tag {
  background: transparent;
  padding: 1px 6px;
  font-size: 0.6rem;
  border: 1px solid var(--neon-color);
  color: var(--neon-color);
  text-shadow: 0 0 5px var(--neon-color);
}

.update-time {
  font-size: 0.7rem;
  color: var(--text-secondary);
  margin-top: 2px;
  text-transform: uppercase;
}

.header-right {
  display: flex;
  gap: 12px;
  color: var(--text-secondary);
}

.icon-btn {
  cursor: pointer;
  transition: all 0.3s;
  opacity: 0.6;
}

.icon-btn:hover {
  color: var(--cyan);
  opacity: 1;
  filter: drop-shadow(0 0 5px var(--cyan));
}

.card-body {
  flex: 1;
  overflow: hidden;
  position: relative;
  min-height: 0;
}

.timeline-container {
  height: 100%;
  overflow-y: auto;
  padding-left: 15px;
  border-left: 1px solid rgba(255, 255, 255, 0.05);
  scrollbar-width: none;
}

.timeline-container::-webkit-scrollbar {
  display: none;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
  transition: all 0.5s ease;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -19px;
  top: 6px;
  width: 6px;
  height: 6px;
  background: var(--neon-color);
  box-shadow: 0 0 10px var(--neon-color);
  border-radius: 0;
  z-index: 1;
}

/* 股票板块特有的涨跌颜色 */
.up-item .content-text {
  color: #ff4d4f !important;
  text-shadow: 0 0 8px rgba(255, 77, 79, 0.4);
}
.up-item::before {
  background: #ff4d4f !important;
  box-shadow: 0 0 10px #ff4d4f !important;
}

.down-item .content-text {
  color: #52c41a !important;
  text-shadow: 0 0 8px rgba(82, 196, 26, 0.4);
}

.down-item::before {
  background: #52c41a !important;
  box-shadow: 0 0 10px #52c41a !important;
}

.market-status {
  display: inline-block;
  vertical-align: middle;
  margin-left: 10px;
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
  line-height: 1.4;
}

.market-status.open {
  color: #52c41a;
  background-color: rgba(82, 196, 26, 0.1);
  border: 1px solid rgba(82, 196, 26, 0.3);
}

.market-status.closed {
  color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.1);
  border: 1px solid rgba(255, 77, 79, 0.3);
}

.market-status.lunch_break {
  color: #faad14;
  background-color: rgba(250, 173, 20, 0.1);
  border: 1px solid rgba(250, 173, 20, 0.3);
}

.is-new {
  animation: row-flash 2s ease-out;
}

/* 针对股票板块的闪烁动画 */
.up-item.is-new {
  animation: row-flash-up 2s ease-out;
}
.down-item.is-new {
  animation: row-flash-down 2s ease-out;
}

@keyframes row-flash {
  0% { background: rgba(0, 243, 255, 0.1); border-left: 2px solid var(--cyan); }
  100% { background: transparent; border-left: 2px solid transparent; }
}

@keyframes row-flash-up {
  0% { background: rgba(255, 77, 79, 0.2); border-left: 2px solid #ff4d4f; }
  100% { background: transparent; border-left: 2px solid transparent; }
}

@keyframes row-flash-down {
  0% { background: rgba(82, 196, 26, 0.2); border-left: 2px solid #52c41a; }
  100% { background: transparent; border-left: 2px solid transparent; }
}

.time-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-bottom: 6px;
  font-weight: bold;
}

.content-text {
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
