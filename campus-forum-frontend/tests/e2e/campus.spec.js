import { test, expect } from '@playwright/test'

/**
 * E2E-001~010: 校园活动发布平台端到端测试
 */

const BASE_URL = 'http://localhost:5173'
const ADMIN_URL = 'http://localhost:5173/admin'

test.describe('E2E-001: 用户注册与登录流程', () => {
  test('应能成功注册新用户并登录', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`)
    await page.fill('input[placeholder*="用户名"]', `testuser_${Date.now()}`)
    await page.fill('input[placeholder*="昵称"]', '测试用户')
    await page.fill('input[type="password"]', 'password123')
    await page.click('button:has-text("注册")')
    await expect(page).toHaveURL(/login/)
  })
})

test.describe('E2E-002~003: 活动操作', () => {
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto(`${BASE_URL}/login`)
    await page.fill('input[placeholder*="用户名"]', 'test')
    await page.fill('input[type="password"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/')
  })

  test('E2E-002: 应能成功发布活动', async ({ page }) => {
    await page.goto(`${BASE_URL}/activity/create`)
    await page.fill('input[placeholder*="活动标题"]', '测试活动 E2E')
    await page.fill('input[placeholder*="活动地点"]', '图书馆大厅')
    await page.fill('textarea[placeholder*="详细描述"]', '这是一个E2E测试活动，内容详情见现场。')
    await page.click('button:has-text("发布活动")')
    await expect(page).toHaveURL(/activity/)
  })

  test('E2E-003: 应能发表评论', async ({ page }) => {
    await page.goto(`${BASE_URL}/activity`)
    await page.click('.card-hover >> nth=0')
    await page.fill('textarea[placeholder*="评论"]', '这是我的E2E测试评论')
    await page.click('button:has-text("发表评论")')
    await expect(page.locator('text=这是我的E2E测试评论')).toBeVisible()
  })
})

test.describe('E2E-004: 帖子发布', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`)
    await page.fill('input[placeholder*="用户名"]', 'test')
    await page.fill('input[type="password"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/')
  })

  test('E2E-004: 应能发布帖子并跳转到发现页', async ({ page }) => {
    await page.goto(`${BASE_URL}/post/publish`)
    await page.fill('input[placeholder*="标题"]', 'E2E测试帖子')
    // 选择版块
    await page.click('.el-select')
    await page.click('.el-select-dropdown__item >> nth=0')
    await page.fill('textarea[placeholder*="想法"]', 'E2E测试帖子内容，分享校园生活。')
    await page.click('button:has-text("发布")')
    await expect(page).toHaveURL(/discover/)
  })
})

test.describe('E2E-005: 收藏功能', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`)
    await page.fill('input[placeholder*="用户名"]', 'test')
    await page.fill('input[type="password"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/')
  })

  test('E2E-005: 应能收藏活动', async ({ page }) => {
    await page.goto(`${BASE_URL}/activity`)
    await page.click('.card-hover >> nth=0')
    await page.click('button:has-text("收藏")')
    await expect(page.locator('button:has-text("已收藏")')).toBeVisible()
  })
})

test.describe('E2E-006: AI助手对话', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`)
    await page.fill('input[placeholder*="用户名"]', 'test')
    await page.fill('input[type="password"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/')
  })

  test('E2E-006: 应能打开AI助手并发送消息', async ({ page }) => {
    await page.goto(`${BASE_URL}/ai-assistant`)
    await expect(page.locator('h2')).toContainText('AI活动助手')
    // 点击快捷问题
    await page.click('text=有什么近期活动？')
    // AI回复区域应该存在（即使在测试环境中AI可能不可用）
    await expect(page.locator('.messages')).toBeVisible()
  })
})

test.describe('E2E-007~010: 管理后台', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`)
    await page.fill('input[placeholder*="用户名"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/')
  })

  test('E2E-007: 管理员可访问后台仪表盘', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/dashboard`)
    await expect(page.locator('h2')).toContainText('数据概览')
    // ECharts图表容器应渲染
    await expect(page.locator('.el-card').first()).toBeVisible()
  })

  test('E2E-008: 管理员可查看用户列表', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/users`)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('E2E-009: 管理员可查看帖子列表', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/posts`)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('E2E-010: 管理员可发布公告', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/announcements`)
    await page.click('button:has-text("+ 发布公告")')
    await page.fill('input[placeholder*="标题"]', 'E2E测试公告')
    await page.fill('textarea', 'E2E测试公告内容')
    await page.click('button:has-text("发布")')
    await expect(page.locator('text=发布成功')).toBeVisible()
  })
})
