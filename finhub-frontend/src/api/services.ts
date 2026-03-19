import api from './axios'

// Auth
export const authApi = {
  login: (email: string, password: string) =>
    api.post('/users/login', { email, password }),
  signup: (data: { email: string; password: string; name: string; phone?: string }) =>
    api.post('/users/signup', data),
  getMe: (token?: string) =>
    api.get('/users/me', token ? { headers: { Authorization: `Bearer ${token}` } } : undefined),
}

// Banking
export const bankingApi = {
  getAccounts: () => api.get('/banking/accounts'),
  getTransactions: (accountId: number, page = 0) =>
    api.get(`/banking/accounts/${accountId}/transactions`, { params: { page, size: 20 } }),
  transfer: (data: { fromAccountId: number; toAccountNumber: string; amount: number; description?: string }) =>
    api.post('/banking/accounts/transfer', data),
  createAccount: (data: { accountName: string }) =>
    api.post('/banking/accounts', data),
  deposit: (accountId: number, amount: number) =>
    api.post(`/banking/accounts/${accountId}/deposit`, { amount }),
}

// Investment
export const investmentApi = {
  getStocks: () => api.get('/investment/stocks'),
  getPortfolios: () => api.get('/investment/portfolios'),
  getHoldings: (portfolioId: number) =>
    api.get(`/investment/portfolios/${portfolioId}/holdings`),
  trade: (data: { portfolioId: number; stockId: number; tradeType: 'BUY' | 'SELL'; quantity: number; price: number }) =>
    api.post('/investment/trade', data),
}

// Payment
export const paymentApi = {
  getPaymentMethods: () => api.get('/payment/methods'),
  getPaymentHistory: (page = 0) =>
    api.get('/payment/history', { params: { page, size: 20 } }),
  registerCard: (data: { cardNumber: string; cardName: string; expiryDate: string }) =>
    api.post('/payment/methods', data),
}

// Insurance
export const insuranceApi = {
  getProducts: () => api.get('/insurance/products'),
  getSubscriptions: () => api.get('/insurance/subscriptions'),
  subscribe: (productId: number) =>
    api.post('/insurance/subscribe', { productId }),
}

// Search
export const searchApi = {
  search: (keyword: string, category?: string, page = 0) =>
    api.get('/search', { params: { keyword, category, page, size: 20 } }),
}

// AI (Ollama 응답 시간이 길어 타임아웃 5분으로 설정)
export const aiApi = {
  chat: (message: string) =>
    api.post('/ai/chat', { message }, { timeout: 300000 }),
  recommend: (q: string) =>
    api.get('/ai/recommend', { params: { q }, timeout: 300000 }),
  analyzeSpending: () =>
    api.get('/ai/analysis/spending', { timeout: 300000 }),
}

// Notification
export const notificationApi = {
  getNotifications: (page = 0) =>
    api.get('/notification', { params: { page, size: 20 } }),
  markAsRead: (id: number) => api.patch(`/notification/${id}/read`),
  markAllAsRead: () => api.patch('/notification/read-all'),
}
