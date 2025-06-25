import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue';
import MainLayout from '../layouts/MainLayout.vue'
import HomeView from '../views/HomeView.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import UserManagementView from '../views/admin/UserManagementView.vue';
import AdminAnnouncementView from '../views/admin/AdminAnnouncementView.vue';
import AdminServiceTermsView from '../views/admin/AdminServiceTermsView.vue'; // 引入服务条款管理页面
import AdminGlobalSpamRuleView from '../views/admin/AdminGlobalSpamRuleView.vue'; // 引入新页面
import AdminHolidayView from '../views/admin/AdminHolidayView.vue'; // 引入新页面
import InboxView from '../views/InboxView.vue'
import EmailDetailView from '../views/EmailDetailView.vue'
import SentView from '../views/SentView.vue';
import DraftsView from '../views/DraftsView.vue';
import TrashView from '../views/TrashView.vue';
import ComposeView from '../views/ComposeView.vue';
import ContactView from '../views/ContactView.vue';
import ArchivedView from '../views/ArchivedView.vue';
import EmailSearchView from '../views/EmailSearchView.vue';
import UserProfileView from '../views/UserProfileView.vue';
import AnnouncementView from '../views/AnnouncementView.vue';
import CalendarView from '../views/CalendarView.vue';
import SpamRuleView from '../views/SpamRuleView.vue';
import { useUserStore } from '@/stores/userStore'

const routes = [
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', name: 'register', component: RegisterView },
  {
    path: '/',
    name: 'layout',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: 'home', name: 'home', component: HomeView },
      { path: 'admin/dashboard', name: 'admin-dashboard', component: AdminDashboard },
      { path: 'admin/users', name: 'admin-user-management', component: UserManagementView },
      { path: 'admin/announcements', name: 'admin-announcement-management', component: AdminAnnouncementView },
      { path: 'admin/service-terms', name: 'admin-service-terms-management', component: AdminServiceTermsView }, // 管理员服务条款管理
      { path: 'admin/global-spam-rules', name: 'admin-global-spam-rules', component: AdminGlobalSpamRuleView },
      {path: 'admin/holidays',name: 'admin-holiday-management', component: AdminHolidayView },
      { path: 'inbox', name: 'inbox', component: InboxView },
      { path: 'sent', name: 'sent', component: SentView },
      { path: 'drafts', name: 'drafts', component: DraftsView },
      { path: 'trash', name: 'trash', component: TrashView },
      { path: 'archived', name: 'archived', component: ArchivedView },
      { path: 'email/detail/:id', name: 'email-detail', component: EmailDetailView },
      { path: 'compose', name: 'compose', component: ComposeView },
      { path: 'contacts', name: 'contacts', component: ContactView },
      { path: 'search', name: 'email-search', component: EmailSearchView, props: route => ({ keyword: route.query.keyword }) },
      { path: 'profile', name: 'user-profile', component: UserProfileView },
      { path: 'announcements', name: 'announcements', component: AnnouncementView },
      { path: 'calendar', name: 'calendar', component: CalendarView },
      { path: 'spam-rules', name: 'spam-rules', component: SpamRuleView },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = !!userStore.token
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'login' })
  } else if ((to.name === 'login' || to.name === 'register') && isAuthenticated) {
    next({ name: userStore.userInfo.role === 'ADMIN' ? 'admin-dashboard' : 'home' })
  } else {
    next()
  }
})

export default router