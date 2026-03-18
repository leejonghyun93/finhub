import { useEffect, useState } from 'react'
import { insuranceApi } from '../api/services'
import LoadingSpinner from '../components/LoadingSpinner'
import toast from 'react-hot-toast'

function formatAmount(amount: number) {
  return amount.toLocaleString('ko-KR') + '원'
}

export default function InsurancePage() {
  const [tab, setTab] = useState<'products' | 'my'>('products')
  const [products, setProducts] = useState<any[]>([])
  const [subscriptions, setSubscriptions] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [subscribing, setSubscribing] = useState<number | null>(null)

  const loadData = async () => {
    setLoading(true)
    await Promise.allSettled([
      insuranceApi.getProducts().then((r) => setProducts(r.data?.data ?? [])),
      insuranceApi.getSubscriptions().then((r) => setSubscriptions(r.data?.data ?? [])),
    ])
    setLoading(false)
  }

  useEffect(() => { loadData() }, [])

  const handleSubscribe = async (productId: number) => {
    setSubscribing(productId)
    try {
      await insuranceApi.subscribe(productId)
      toast.success('보험 가입이 완료되었습니다.')
      loadData()
    } finally {
      setSubscribing(null)
    }
  }

  const categoryEmoji: Record<string, string> = {
    LIFE: '❤️', HEALTH: '🏥', AUTO: '🚗', TRAVEL: '✈️', FIRE: '🏠',
  }

  return (
    <div className="px-4 pt-6">
      <div className="flex bg-gray-100 rounded-2xl p-1 mb-4">
        {(['products', 'my'] as const).map((t) => (
          <button key={t} onClick={() => setTab(t)}
            className={`flex-1 py-2 rounded-xl text-sm font-semibold transition-all ${tab === t ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500'}`}>
            {t === 'products' ? '보험 상품' : '내 보험'}
          </button>
        ))}
      </div>

      {loading ? (
        <LoadingSpinner className="py-20" />
      ) : tab === 'products' ? (
        <div className="space-y-3">
          {products.length === 0 ? (
            <div className="card text-center py-8"><p className="text-gray-400 text-sm">상품이 없습니다</p></div>
          ) : products.map((p: any) => (
            <div key={p.id} className="card">
              <div className="flex items-start justify-between">
                <div className="flex items-start gap-3">
                  <span className="text-2xl">{categoryEmoji[p.category] ?? '🛡️'}</span>
                  <div>
                    <p className="font-bold text-gray-900">{p.productName ?? p.name}</p>
                    <p className="text-xs text-gray-500 mt-0.5">{p.description ?? ''}</p>
                    <p className="text-primary font-semibold text-sm mt-2">
                      월 {formatAmount(Number(p.monthlyPremium ?? p.premium ?? 0))}
                    </p>
                  </div>
                </div>
                <button onClick={() => handleSubscribe(p.id)} disabled={subscribing === p.id}
                  className="bg-primary text-white text-xs font-semibold px-3 py-1.5 rounded-xl shrink-0">
                  {subscribing === p.id ? '가입 중' : '가입'}
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="space-y-3">
          {subscriptions.length === 0 ? (
            <div className="card text-center py-8"><p className="text-gray-400 text-sm">가입된 보험이 없습니다</p></div>
          ) : subscriptions.map((s: any) => (
            <div key={s.id} className="card">
              <div className="flex items-center gap-3">
                <span className="text-2xl">{categoryEmoji[s.category ?? s.insuranceProduct?.category] ?? '🛡️'}</span>
                <div className="flex-1">
                  <p className="font-bold text-gray-900">{s.productName ?? s.insuranceProduct?.productName ?? s.name}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="bg-primary-50 text-primary text-xs px-2 py-0.5 rounded-full font-medium">
                      {s.status ?? '유지중'}
                    </span>
                    <span className="text-xs text-gray-400">월 {formatAmount(Number(s.monthlyPremium ?? s.premium ?? 0))}</span>
                  </div>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {s.startDate ? new Date(s.startDate).toLocaleDateString('ko-KR') : ''} ~
                    {s.endDate ? new Date(s.endDate).toLocaleDateString('ko-KR') : ''}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
