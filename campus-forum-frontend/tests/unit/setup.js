import { vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

// 每个测试前重置 Pinia
beforeEach(() => {
  setActivePinia(createPinia())
})

// Mock Element Plus 消息组件
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  },
  ElNotification: vi.fn()
}))

// Mock Vue Router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    go: vi.fn()
  }),
  useRoute: () => ({
    params: {},
    query: {},
    path: '/'
  })
}))
