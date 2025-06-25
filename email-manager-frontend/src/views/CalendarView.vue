<template>
  <div>
    <el-page-header content="日历提醒" class="page-header" @back="goBack" />

    <el-row :gutter="20">
      <!-- 左侧：日历和年月选择 -->
      <el-col :md="16" :sm="24">
        <el-card>
          <el-calendar ref="calendarRef" v-model="calendarDisplayDate" @input="handleCalendarDateChange">
            <template #header="{ date }"> 
              <div class="calendar-header-content">
                <span>{{ formatCalendarHeader(date) }}</span>
                <div>
                  <el-button-group>
                    <el-button size="small" @click="selectDate('prev-month')">上个月</el-button>
                    <el-button size="small" @click="selectDate('today')">今天</el-button>
                    <el-button size="small" @click="selectDate('next-month')">下个月</el-button>
                  </el-button-group>
                </div>
              </div>
            </template>
            <template #date-cell="{ data }">
              <div class="date-cell" :class="{ 
                  'is-today': isToday(data.day), 
                  'is-selected': isSelectedForHighlight(data.day),
                  'is-weekend': isWeekend(data.day)
                }" @click="handleDateClick(data.day)">
                <span class="day-number">{{ parseDay(data.day) }}</span>
                <div class="event-dots-container">
                  <div v-for="event in getEventsForDate(data.day)" 
                       :key="event.id || event.name" 
                       class="event-dot"
                       :class="`event-type-${event.type}`" 
                       :title="event.name">
                  </div>
                </div>
              </div>
            </template>
          </el-calendar>
        </el-card>
      </el-col>

      <!-- 右侧：节日和生日信息展示 -->
      <el-col :md="8" :sm="24">
        <el-card class="info-card" v-loading="loadingHolidays">
          <template #header><span>{{ displayYear }}年{{ displayMonth }}月 节假日/调休</span></template>
          <el-empty v-if="!holidays.length && !loadingHolidays" description="本月无节假日或调休安排" />
          <el-timeline v-else style="padding-left: 0;">
            <el-timeline-item
              v-for="holiday in holidays" 
              :key="holiday.id" 
              :timestamp="holiday.holidayDate"
              :type="holiday.isWorkday ? 'info' : 'primary'" 
              :hollow="holiday.isWorkday ? false : true"
              :icon="holiday.isWorkday ? Briefcase : CoffeeCup"
            >
              {{ holiday.holidayName }} 
              <el-tag v-if="holiday.isWorkday" type="warning" size="small" style="margin-left: 5px;">上班</el-tag>
              <el-tag v-else type="success" size="small" style="margin-left: 5px;">放假</el-tag>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card class="info-card" style="margin-top: 20px;" v-loading="loadingBirthdays">
          <template #header><span>{{ displayYear }}年{{ displayMonth }}月 寿星</span></template>
           <el-empty v-if="!birthdays.length && !loadingBirthdays" description="本月无联系人过生日" />
          <el-timeline v-else style="padding-left: 0;">
            <el-timeline-item
              v-for="contact in birthdays"
              :key="contact.id"
              :timestamp="contact.birthday ? `${displayYear}-${formatBirthdayForDisplay(contact.birthday)}` : '日期未设置'"
              type="success"
              hollow
              :icon="Present"
            >
              <el-link type="primary" @click="sendBirthdayGreeting(contact)" :underline="false">
                {{ contact.name }}
              </el-link>
              <span v-if="contact.birthday && formatBirthdayForDisplay(contact.birthday)"> ({{ formatBirthdayForDisplay(contact.birthday) }})</span>
              <span v-else-if="contact.birthday && !formatBirthdayForDisplay(contact.birthday)">(生日格式有误)</span>
              <span v-else>(生日未设置)</span>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getHolidaysByMonthApi, getBirthdaysByMonthApi } from '@/api/calendar.js';
import { ElMessage, ElCalendar, ElTimeline, ElTimelineItem, ElEmpty, ElButtonGroup, ElTag, ElLink } from 'element-plus';
import { Briefcase, CoffeeCup, Present } from '@element-plus/icons-vue';

const router = useRouter();
const calendarRef = ref(null);

const today = new Date();
const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

const calendarDisplayDate = ref(new Date()); 
const selectedDateForHighlight = ref(todayStr); 

const displayYear = computed(() => calendarDisplayDate.value.getFullYear());
const displayMonth = computed(() => calendarDisplayDate.value.getMonth() + 1);

function formatCalendarHeader(dateString) {
    const date = new Date(dateString);
    return `${date.getFullYear()} 年 ${date.getMonth() + 1} 月`;
}

function selectDate(val) {
  if (!calendarRef.value) return;
  calendarRef.value.selectDate(val);
}

function handleCalendarDateChange(newDateInput) {
  let newDate;
  if (newDateInput instanceof Date) {
    newDate = newDateInput;
  } else if (typeof newDateInput === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(newDateInput)) {
    newDate = new Date(newDateInput);
  } else {
    return; 
  }
  calendarDisplayDate.value = new Date(newDate); 
  if (newDate.getFullYear() === today.getFullYear() && newDate.getMonth() === today.getMonth()) {
    selectedDateForHighlight.value = todayStr;
  } else {
    const y = newDate.getFullYear();
    const m = String(newDate.getMonth() + 1).padStart(2, '0');
    selectedDateForHighlight.value = `${y}-${m}-01`;
  }
  fetchHolidays();
  fetchBirthdays();
}

const loadingHolidays = ref(false);
const holidays = ref([]);
async function fetchHolidays() {
  loadingHolidays.value = true;
  try {
    holidays.value = await getHolidaysByMonthApi(displayYear.value, displayMonth.value);
  } catch (error) {
    ElMessage.error('加载节假日信息失败');
  } finally { 
    loadingHolidays.value = false; 
  }
}

const loadingBirthdays = ref(false);
const birthdays = ref([]);
async function fetchBirthdays() {
  loadingBirthdays.value = true;
  try {
    birthdays.value = await getBirthdaysByMonthApi(displayMonth.value);
  } catch (error) {
    ElMessage.error('加载联系人生日失败');
  } finally { 
    loadingBirthdays.value = false; 
  }
}

const getEventsForDate = (dateStr) => {
  const events = [];
  holidays.value.forEach(h => {
    if (h.holidayDate === dateStr) {
      if (h.isWorkday === 1) {
        events.push({ type: 'workday_makeup', name: `${h.holidayName} (班)`, id: `h-${h.id}` });
      } else {
        events.push({ type: 'holiday', name: h.holidayName, id: `h-${h.id}` });
      }
    }
  });
  birthdays.value.forEach(b => {
    if (b.birthday && typeof b.birthday === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(b.birthday)) {
      const [_year, month, day] = b.birthday.split('-');
      const currentDayOfMonthInCell = dateStr.substring(8, 10);
      const currentMonthInCell = dateStr.substring(5, 7);
      
      if (month === currentMonthInCell && day === currentDayOfMonthInCell) {
         events.push({ type: 'birthday', name: `${b.name} 的生日`, id: `b-${b.id}` });
      }
    }
  });
  return events.slice(0, 2);
};

function handleDateClick(dateStr) {
    selectedDateForHighlight.value = dateStr;
}

const isToday = (dateStr) => dateStr === todayStr;
const isSelectedForHighlight = (dateStr) => dateStr === selectedDateForHighlight.value;
const parseDay = (dayStr) => dayStr.split('-')[2];

function formatBirthdayForDisplay(birthdayStr) {
  if (!birthdayStr || typeof birthdayStr !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(birthdayStr)) {
    return ''; 
  }
  return birthdayStr.substring(5);
}

const isWeekend = (dateStr) => {
    const dayOfWeek = new Date(dateStr).getDay();
    return dayOfWeek === 0 || dayOfWeek === 6;
}

// 新增：发送生日祝福邮件的函数
function sendBirthdayGreeting(contact) {
  if (contact && contact.email) {
    router.push({ 
      name: 'compose', 
      query: { 
        toEmail: contact.email,
        subject: `祝 ${contact.name} 生日快乐！`
      } 
    });
  } else {
    ElMessage.warning('该联系人没有邮箱地址，无法发送祝福。');
  }
}

onMounted(() => {
  fetchHolidays();
  fetchBirthdays();
});

function goBack() { router.back(); }
</script>

<style scoped>
.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.calendar-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.date-cell {
  height: 100%;
  min-height: 60px;
  padding: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  position: relative;
  cursor: pointer;
}
.day-number {
  margin-bottom: 4px;
  z-index: 1;
  padding: 2px 4px;
  border-radius: 50%;
  line-height: 20px;
  width: 24px;
  height: 24px;
  text-align: center;
}
.date-cell.is-weekend > .day-number {
  color: #a8abb2;
}
.date-cell.is-today > .day-number {
  color: var(--el-color-primary);
  font-weight: bold;
}
.date-cell.is-today.is-weekend > .day-number {
    color: var(--el-color-primary);
    font-weight: bold;
}
.date-cell.is-selected > .day-number {
  background-color: var(--el-color-primary);
  color: white !important;
  font-weight: normal !important;
}
.event-dots-container {
  position: absolute;
  bottom: 4px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 3px;
}
.event-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.event-dot.event-type-holiday {
  background-color: #E6A23C;
}
.event-dot.event-type-birthday {
  background-color: #67C23A;
}
.event-dot.event-type-workday_makeup {
  background-color: #409EFF;
}
.info-card {
  min-height: 300px;
}
:deep(.el-timeline) {
    padding-left: 0px !important;
}
:deep(.el-timeline-item__icon) {
    font-size: 14px;
}
</style>