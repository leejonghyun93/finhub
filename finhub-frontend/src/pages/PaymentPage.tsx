import { useEffect, useState } from 'react'
import { paymentApi } from '../api/services'
import LoadingSpinner from '../components/LoadingSpinner'

function formatAmount(amount: number) {
  return amount.toLocaleString('ko-KR') + '원'
}

export default function PaymentPage() {
  const [methods, setMethods] = useState<any[]>([])
  const [history, setHistory] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.allSettled([
      paymentApi.getPaymentMethods(),
      paymentApi.getPaymentHistory(),
    ]).then(([mRes, hRes]) => {
      setMethods(mRes.status === 'fulfilled' ? (mRes.value.data?.data ?? []) : [])
      // Page 응답
      setHistory(hRes.status === 'fulfilled' ? (hRes.value.data?.data?.content ?? []) : [])
    }).finally(() => setLoading(false))
  }, [])

  if (loading) return <LoadingSpinner className="py-32" />

  return (
    <div className="px-4 pt-6">
      <div className="card mb-4">
        <h2 className="font-bold text-gray-900 mb-4">결제 수단</h2>
        {methods.length === 0 ? (
          <p className="text-center text-gray-400 py-4 text-sm">등록된 결제 수단이 없습니다</p>
        ) : (
          <div className="space-y-3">
            {methods.map((m: any) => (
              <div key={m.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-2xl">
                <div className="w-12 h-8 bg-gradient-to-r from-primary to-primary-600 rounded-lg flex items-center justify-center">
                  <span className="text-white text-xs font-bold">CARD</span>
                </div>
                <div>
                  <p className="font-medium text-sm text-gray-900">{m.cardName ?? m.methodName}</p>
                  <p className="text-xs text-gray-400">**** **** **** {String(m.cardNumber ?? m.lastFourDigits ?? '').slice(-4)}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="card">
        <h2 className="font-bold text-gray-900 mb-4">결제 내역</h2>
        {history.length === 0 ? (
          <p className="text-center text-gray-400 py-8 text-sm">결제 내역이 없습니다</p>
        ) : (
          <div className="space-y-4">
            {history.map((h: any) => (
              <div key={h.id} className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-purple-50 rounded-xl flex items-center justify-center">
                    <span className="text-lg">💳</span>
                  </div>
                  <div>
                    <p className="font-medium text-sm text-gray-900">{h.merchantName ?? h.description ?? '결제'}</p>
                    <p className="text-xs text-gray-400">{h.createdAt ? new Date(h.createdAt).toLocaleDateString('ko-KR') : ''}</p>
                  </div>
                </div>
                <p className="font-semibold text-gray-900">{formatAmount(Number(h.amount ?? 0))}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
