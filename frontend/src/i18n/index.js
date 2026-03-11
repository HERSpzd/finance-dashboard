import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    header: {
      title: 'CYBER FINANCIAL TERMINAL',
      system: 'SYSTEM: ONLINE',
      network: 'NETWORK: SECURED'
    },
    sections: {
      news: 'Financial News',
      global: 'Global Affairs',
      ai: 'AI Trends',
      tech: 'Tech Trends',
      stocks: 'Stock Market Trends',
      crypto: 'Crypto Volatility'
    },
    marketStatus: {
      open: 'OPEN',
      closed: 'CLOSED',
      lunchBreak: 'LUNCH BREAK'
    },
    boards: {
      wallstreet: 'Wall Street News',
      nasdaq: 'Nasdaq News',
      cailian: 'Cailian Press',
      crypto: 'Crypto Volatility',
      stocks: 'S&P 500 Trends',
      usStocks: 'US Stocks',
      aitech: 'Hacker News',
      macro: 'Macro Economy',
      geopolitical: 'Singapore Zaobao'
    },
    tags: {
      flash: 'Flash',
      volatile: 'VOLATILE',
      index: 'INDEX',
      hot: 'HOT',
      active: 'ACTIVE',
      stable: 'STABLE'
    },
    common: {
      refresh: 'Just updated',
      justNow: 'Just now',
      minutesAgo: '{n}m ago',
      newsTag: 'Flash'
    }
  },
  zh: {
    header: {
      title: '赛博金融终端',
      system: '系统: 在线',
      network: '网络: 安全'
    },
    sections: {
      news: '财经新闻',
      global: '国际局势',
      ai: 'AI 动向',
      tech: '科技动向',
      stocks: '股市动向',
      crypto: '币圈动向'
    },
    marketStatus: {
      open: '开盘',
      closed: '休市',
      lunchBreak: '午间休市'
    },
    boards: {
      wallstreet: '华尔街见闻',
      nasdaq: '纳斯达克快讯',
      cailian: '财联社电报',
      crypto: '虚拟货币异动',
      stocks: '标普500动态',
      usStocks: '美股行情',
      aitech: 'Hacker News',
      macro: '宏观经济',
      geopolitical: '新加坡早报'
    },
    tags: {
      flash: '快讯',
      volatile: '剧烈波动',
      index: '指数',
      hot: '热门',
      active: '活跃',
      stable: '稳定'
    },
    common: {
      refresh: '刚刚更新',
      justNow: '刚刚',
      minutesAgo: '{n}分钟前',
      newsTag: '快讯'
    }
  }
}

const i18n = createI18n({
  legacy: false,
  locale: 'zh',
  fallbackLocale: 'en',
  messages
})

export default i18n
