const scheduleApi = require('../../api/schedule')

const WEEKDAYS = ['日', '一', '二', '三', '四', '五', '六']

Page({
  data: {
    currentYear: 2026,
    currentMonth: 6,
    monthDisplay: '',
    calendarDays: [],
    schedules: [],
    selectedDate: '',
    selectedSchedule: null,
    loading: false
  },

  onLoad() {
    const now = new Date()
    this.setData({
      currentYear: now.getFullYear(),
      currentMonth: now.getMonth() + 1
    })
    this.updateMonthDisplay()
    this.loadSchedule()
  },

  updateMonthDisplay() {
    const { currentYear, currentMonth } = this.data
    this.setData({
      monthDisplay: `${currentYear}年${currentMonth}月`
    })
  },

  onPrevMonth() {
    let { currentYear, currentMonth } = this.data
    currentMonth--
    if (currentMonth < 1) {
      currentMonth = 12
      currentYear--
    }
    this.setData({ currentYear, currentMonth })
    this.updateMonthDisplay()
    this.loadSchedule()
  },

  onNextMonth() {
    let { currentYear, currentMonth } = this.data
    currentMonth++
    if (currentMonth > 12) {
      currentMonth = 1
      currentYear++
    }
    this.setData({ currentYear, currentMonth })
    this.updateMonthDisplay()
    this.loadSchedule()
  },

  loadSchedule() {
    const { currentYear, currentMonth } = this.data
    const month = `${currentYear}-${String(currentMonth).padStart(2, '0')}`
    this.setData({ loading: true })

    scheduleApi.getMySchedule(month).then(res => {
      const schedules = res.data || []
      this.setData({ schedules, loading: false })
      this.generateCalendarDays(schedules)
    }).catch(() => {
      this.setData({ loading: false, schedules: [] })
      this.generateCalendarDays([])
    })
  },

  generateCalendarDays(schedules) {
    const { currentYear, currentMonth } = this.data
    const firstDay = new Date(currentYear, currentMonth - 1, 1)
    const lastDay = new Date(currentYear, currentMonth, 0)
    const totalDays = lastDay.getDate()
    const startWeekday = firstDay.getDay()

    const scheduleMap = {}
    schedules.forEach(item => {
      scheduleMap[item.date] = item
    })

    const days = []

    // 填充前面的空白
    for (let i = 0; i < startWeekday; i++) {
      days.push({ day: '', empty: true })
    }

    // 填充日期
    for (let d = 1; d <= totalDays; d++) {
      const dateStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(d).padStart(2, '0')}`
      const schedule = scheduleMap[dateStr]
      days.push({
        day: d,
        date: dateStr,
        empty: false,
        status: schedule ? schedule.status : '',
        statusDisplay: schedule ? schedule.statusDisplay : '',
        schedule: schedule || null
      })
    }

    this.setData({ calendarDays: days })
  },

  onDayTap(e) {
    const dayItem = e.currentTarget.dataset.day
    if (dayItem.empty || !dayItem.date) return

    this.setData({
      selectedDate: dayItem.date,
      selectedSchedule: dayItem.schedule
    })
  }
})
