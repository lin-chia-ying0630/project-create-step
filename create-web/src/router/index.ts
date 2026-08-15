import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { resolvePortalUrl, shouldRedirectToPortal } from './portalUrl'

export const routeNames = {
  customer: 'customer-create',
  customerQuery: 'customer-query',
  applicationEntry: 'application-entry',
  policyQuery: 'policy-query',
  premium: 'initial-premium',
  batch: 'underwriting-batch',
  inquiry: 'underwriting-inquiry',
  underwritingReview: 'underwriting-review',
  reversal: 'policy-reversal',
  review: 'review-work-queue',
  codeDefinition: 'code-definition-lookup',
} as const

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: { name: routeNames.review } },
  {
    path: '/reviews',
    name: routeNames.review,
    component: () => import('../features/review/views/ReviewWorkQueueView.vue'),
    meta: { title: '新契約覆核工作台' },
  },
  {
    path: '/code-definitions',
    name: routeNames.codeDefinition,
    component: () => import('../features/code-definition/views/CodeDefinitionLookupView.vue'),
    meta: { title: 'Code Definitions 代碼定義' },
  },
  {
    path: '/customers',
    name: routeNames.customerQuery,
    component: () => import('../features/customer/views/CustomerCreateView.vue'),
    props: { mode: 'query' },
    meta: { title: '客戶資料查詢' },
  },
  {
    path: '/customers/new',
    name: routeNames.customer,
    component: () => import('../features/customer/views/CustomerCreateView.vue'),
    props: { mode: 'create' },
    meta: { title: '客戶資料建立' },
  },
  {
    path: '/new-contract/applications/new',
    name: routeNames.applicationEntry,
    component: () => import('../features/new-contract/views/ApplicationEntryView.vue'),
    meta: { title: '保單登打新增' },
  },
  {
    path: '/new-contract/applications',
    name: routeNames.policyQuery,
    component: () => import('../features/new-contract/views/PolicyQueryView.vue'),
    meta: { title: '保單資料查詢' },
  },
  {
    path: '/new-contract/premiums',
    name: routeNames.premium,
    component: () => import('../features/premium/views/InitialPremiumMatchView.vue'),
    meta: { title: '首期保費收款' },
  },
  {
    path: '/underwriting/batches',
    name: routeNames.batch,
    component: () => import('../features/underwriting/views/UnderwritingBatchView.vue'),
    meta: { title: '新契約批次承保作業' },
  },
  {
    path: '/underwriting/reviews',
    name: routeNames.underwritingReview,
    component: () => import('../features/underwriting/views/UnderwritingReviewView.vue'),
    meta: { title: '核保審查作業' },
  },
  {
    path: '/underwriting/inquiries',
    name: routeNames.inquiry,
    component: () => import('../features/underwriting/views/UnderwritingInquiryView.vue'),
    meta: { title: '核保照會單' },
  },
  {
    path: '/policies/reversals',
    name: routeNames.reversal,
    component: () => import('../features/policy-reversal/views/PolicyIssuanceReversalView.vue'),
    meta: { title: '承保撤回' },
  },
  { path: '/:pathMatch(.*)*', redirect: { name: routeNames.review } },
]

export const navigationItems = [
  { name: routeNames.review, label: '覆核工作台', icon: '✓' },
  { name: routeNames.codeDefinition, label: '代碼定義查詢', icon: '≡' },
  { name: routeNames.customerQuery, label: '客戶資料查詢', icon: '⌕' },
  { name: routeNames.customer, label: '客戶資料建立', icon: '♙' },
  { name: routeNames.applicationEntry, label: '保單登打新增', icon: '▤' },
  { name: routeNames.policyQuery, label: '保單資料查詢', icon: '⌕' },
  { name: routeNames.premium, label: '首期保費收款', icon: '＄' },
  { name: routeNames.batch, label: '新契約批次承保作業', icon: '⚙' },
  { name: routeNames.underwritingReview, label: '核保審查作業', icon: '◎' },
  { name: routeNames.inquiry, label: '核保照會單', icon: '!' },
  { name: routeNames.reversal, label: '承保撤回', icon: '↶' },
] as const

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

/** 所有新契約畫面進入前由後端驗證 HttpOnly SSO JWT；前端不讀取 token。 */
router.beforeEach(async () => {
  let response: Response
  try {
    response = await fetch('/api/auth/me', {
      credentials: 'same-origin',
      headers: { Accept: 'application/json' },
    })
  } catch {
    return true
  }
  if (response.ok || !shouldRedirectToPortal(response.status)) return true
  const portalUrl = resolvePortalUrl(import.meta.env.VITE_PORTAL_URL, window.location.origin)
  window.location.replace(portalUrl)
  return false
})

/** 依目前作業名稱更新瀏覽器標題，方便多分頁辨識。 */
router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : '新契約作業'
  document.title = `${title}｜保單新契約作業`
})
