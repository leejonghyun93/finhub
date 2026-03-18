import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { bankingApi, investmentApi, notificationApi } from '../api/services'
import LoadingSpinner from '../components/LoadingSpinner'

function formatAmount(amount: number) {
  return amount.toLocaleString('ko-KR') + '원'
}

export default function HomePage() {
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()
  const [accounts, setAccounts] = useState<any[]>([])
  const [holdings, setHoldings] = useState<any[]>([])
  const [notifications, setNotifications] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadData = async () => {
      const accRes = await bankingApi.getAccounts().catch(() => null)
      const accData: any[] = accRes?.data?.data ?? []
      setAccounts(accData)

      const portRes = await investmentApi.getPortfolios().catch(() => null)
      const portfolios: any[] = portRes?.data?.data ?? []
      if (portfolios.length > 0) {
        const holdRes = await investmentApi.getHoldings(portfolios[0].id).catch(() => null)
        setHoldings(holdRes?.data?.data ?? [])
      }

      const notifRes = await notificationApi.getNotifications().catch(() => null)
      const notifContent: any[] = notifRes?.data?.data?.content ?? []
      setNotifications(notifContent.slice(0, 3))

      setLoading(false)
    }
    loadData()
  }, [])

  const totalBalance = accounts.reduce((sum: number, a: any) => sum + Number(a.balance ?? 0), 0)
  const unreadCount = notifications.filter((n: any) => !n.isRead).length

  const quickMenus = [
    { label: '뱅킹', path: '/banking', color: 'bg-blue-50', icon: '🏦' },
    { label: '투자', path: '/investment', color: 'bg-green-50', icon: '📈' },
    { label: '결제', path: '/payment', color: 'bg-purple-50', icon: '💳' },
    { label: '보험', path: '/insurance', color: 'bg-orange-50', icon: '🛡️' },
  ]

  return (
    <div className="px-4 pt-6 pb-4">
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="text-gray-500 text-sm">안녕하세요 👋</p>
          <h1 className="text-xl font-bold text-gray-900">{user?.name || '사용자'}님</h1>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/notification')} className="relative w-10 h-10 flex items-center justify-center">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#374151" strokeWidth="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
              <path d="M13.73 21a2 2 0 0 1-3.46 0" />
            </svg>
            {unreadCount > 0 && (
              <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
                {unreadCount}
              </span>
            )}
          </button>
          <button onClick={logout} className="text-gray-400 hover:text-gray-600">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" />
            </svg>
          </button>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner className="py-20" />
      ) : (
        <>
          <div className="bg-gradient-to-br from-primary to-primary-600 rounded-3xl p-6 mb-4 text-white">
            <p className="text-primary-100 text-sm mb-1">총 자산</p>
            <p className="text-3xl font-bold">{formatAmount(totalBalance)}</p>
            <div className="mt-4 flex gap-3">
              <button onClick={() => navigate('/banking')} className="flex-1 bg-white/20 hover:bg-white/30 rounded-2xl py-2 text-sm font-semibold transition-colors">
                송금
              </button>
              <button onClick={() => navigate('/banking')} className="flex-1 bg-white/20 hover:bg-white/30 rounded-2xl py-2 text-sm font-semibold transition-colors">
                계좌
              </button>
            </div>
          </div>

          <div className="grid grid-cols-4 gap-3 mb-4">
            {quickMenus.map((menu) => (
              <button key={menu.path} onClick={() => navigate(menu.path)} className={`${menu.color} rounded-2xl p-3 flex flex-col items-center gap-1.5`}>
                <span className="text-xl">{menu.icon}</span>
                <span className="text-xs font-medium text-gray-700">{menu.label}</span>
              </button>
            ))}
          </div>

          <div className="card mb-4">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-bold text-gray-900">내 계좌</h2>
              <button onClick={() => navigate('/banking')} className="text-primary text-sm font-medium">전체보기</button>
            </div>
            {accounts.length === 0 ? (
              <p className="text-gray-400 text-sm text-center py-4">등록된 계좌가 없습니다</p>
            ) : (
              <div className="space-y-3">
                {accounts.slice(0, 3).map((acc: any) => (
                  <div key={acc.id} className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-primary-50 rounded-xl flex items-center justify-center">
                        <span className="text-primary text-sm font-bold">{(acc.accountName ?? '계')[0]}</span>
                      </div>
                      <div>
                        <p className="font-medium text-gray-900 text-sm">{acc.accountName}</p>
                        <p className="text-gray-400 text-xs">{acc.accountNumber}</p>
                      </div>
                    </div>
                    <p className="font-semibold text-gray-900">{formatAmount(Number(acc.balance ?? 0))}</p>
                  </div>
                ))}
              </div>
            )}
          </div>

          {holdings.length > 0 && (
            <div className="card mb-4">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-bold text-gray-900">보유 주식</h2>
                <button onClick={() => navigate('/investment')} className="text-primary text-sm font-medium">전체보기</button>
              </div>
              <div className="space-y-3">
                {holdings.slice(0, 3).map((h: any) => (
                  <div key={h.id} className="flex items-center justify-between">
                    <div>
                      <p className="font-medium text-gray-900 text-sm">{h.stockName}</p>
                      <p className="text-gray-400 text-xs">{h.quantity}주</p>
                    </div>
                    <div className="text-right">
                      <p className="font-semibold text-gray-900 text-sm">{formatAmount(Number(h.currentValue ?? 0))}</p>
                      <p className={`text-xs font-medium ${Number(h.profitLoss) >= 0 ? 'text-red-500' : 'text-blue-500'}`}>
                        {Number(h.profitLoss) >= 0 ? '+' : ''}{Number(h.profitLossRate ?? 0).toFixed(2)}%
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {notifications.length > 0 && (
            <div className="card">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-bold text-gray-900">최근 알림</h2>
                <button onClick={() => navigate('/notification')} className="text-primary text-sm font-medium">전체보기</button>
              </div>
              <div className="space-y-3">
                {notifications.map((n: any) => (
                  <div key={n.id} className="flex items-start gap-3">
                    <div className="w-2 h-2 rounded-full bg-primary mt-1.5 shrink-0" />
                    <div>
                      <p className="text-sm text-gray-700">{n.message}</p>
                      <p className="text-xs text-gray-400 mt-0.5">{n.createdAt ? new Date(n.createdAt).toLocaleDateString('ko-KR') : ''}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
