import { useEffect, useState } from 'react'
import { investmentApi } from '../api/services'
import LoadingSpinner from '../components/LoadingSpinner'
import Modal from '../components/Modal'
import toast from 'react-hot-toast'

function formatAmount(amount: number) {
  return amount.toLocaleString('ko-KR') + '원'
}

export default function InvestmentPage() {
  const [tab, setTab] = useState<'portfolio' | 'stocks'>('portfolio')
  const [stocks, setStocks] = useState<any[]>([])
  const [portfolios, setPortfolios] = useState<any[]>([])
  const [holdings, setHoldings] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState<{ type: 'buy' | 'sell'; stock: any } | null>(null)
  const [quantity, setQuantity] = useState('')
  const [tradeLoading, setTradeLoading] = useState(false)

  const loadData = async () => {
    setLoading(true)
    const [stockRes, portRes] = await Promise.allSettled([
      investmentApi.getStocks(),
      investmentApi.getPortfolios(),
    ])

    const stockList: any[] = stockRes.status === 'fulfilled' ? (stockRes.value.data?.data ?? []) : []
    setStocks(stockList)

    const portList: any[] = portRes.status === 'fulfilled' ? (portRes.value.data?.data ?? []) : []
    setPortfolios(portList)

    if (portList.length > 0) {
      const holdRes = await investmentApi.getHoldings(portList[0].id).catch(() => null)
      setHoldings(holdRes?.data?.data ?? [])
    } else {
      setHoldings([])
    }
    setLoading(false)
  }

  useEffect(() => { loadData() }, [])

  const handleTrade = async () => {
    if (!modal || !quantity || portfolios.length === 0) {
      toast.error(portfolios.length === 0 ? '포트폴리오가 없습니다.' : '수량을 입력해주세요.')
      return
    }
    setTradeLoading(true)
    try {
      await investmentApi.trade({
        portfolioId: portfolios[0].id,
        stockId: modal.stock.id,
        tradeType: modal.type === 'buy' ? 'BUY' : 'SELL',
        quantity: Number(quantity),
        price: Number(modal.stock.currentPrice ?? modal.stock.price ?? 0),
      })
      toast.success(modal.type === 'buy' ? '매수가 완료되었습니다.' : '매도가 완료되었습니다.')
      setModal(null)
      setQuantity('')
      loadData()
    } finally {
      setTradeLoading(false)
    }
  }

  const totalValue = holdings.reduce((s: number, h: any) => s + Number(h.currentValue ?? 0), 0)
  const totalProfit = holdings.reduce((s: number, h: any) => s + Number(h.profitLoss ?? 0), 0)

  return (
    <div className="px-4 pt-6">
      <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-3xl p-5 mb-4 text-white">
        <p className="text-green-100 text-sm">총 평가금액</p>
        <p className="text-2xl font-bold mt-1">{formatAmount(totalValue)}</p>
        <p className={`text-sm mt-1 font-medium ${totalProfit >= 0 ? 'text-yellow-200' : 'text-red-200'}`}>
          {totalProfit >= 0 ? '+' : ''}{formatAmount(totalProfit)}
        </p>
      </div>

      <div className="flex bg-gray-100 rounded-2xl p-1 mb-4">
        {(['portfolio', 'stocks'] as const).map((t) => (
          <button key={t} onClick={() => setTab(t)}
            className={`flex-1 py-2 rounded-xl text-sm font-semibold transition-all ${tab === t ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500'}`}>
            {t === 'portfolio' ? '내 포트폴리오' : '종목 탐색'}
          </button>
        ))}
      </div>

      {loading ? (
        <LoadingSpinner className="py-20" />
      ) : tab === 'portfolio' ? (
        <div className="card">
          {holdings.length === 0 ? (
            <p className="text-center text-gray-400 py-8 text-sm">보유 종목이 없습니다</p>
          ) : (
            <div className="space-y-4">
              {holdings.map((h: any) => (
                <div key={h.id} className="flex items-center justify-between">
                  <div>
                    <p className="font-semibold text-gray-900">{h.stockName}</p>
                    <p className="text-xs text-gray-400">{h.quantity}주 · 평균 {formatAmount(Number(h.averagePrice ?? 0))}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-gray-900">{formatAmount(Number(h.currentValue ?? 0))}</p>
                    <p className={`text-xs font-medium ${Number(h.profitLoss) >= 0 ? 'text-red-500' : 'text-blue-500'}`}>
                      {Number(h.profitLoss) >= 0 ? '+' : ''}{Number(h.profitLossRate ?? 0).toFixed(2)}%
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div className="card">
          {stocks.length === 0 ? (
            <p className="text-center text-gray-400 py-8 text-sm">종목을 불러올 수 없습니다</p>
          ) : (
            <div className="space-y-4">
              {stocks.map((s: any) => (
                <div key={s.id} className="flex items-center justify-between">
                  <div>
                    <p className="font-semibold text-gray-900">{s.name}</p>
                    <p className="text-xs text-gray-400">{s.ticker}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="text-right">
                      <p className="font-semibold text-gray-900">{formatAmount(Number(s.currentPrice ?? 0))}</p>
                      <p className={`text-xs font-medium ${Number(s.changeRate ?? 0) >= 0 ? 'text-red-500' : 'text-blue-500'}`}>
                        {Number(s.changeRate ?? 0) >= 0 ? '+' : ''}{Number(s.changeRate ?? 0).toFixed(2)}%
                      </p>
                    </div>
                    <button onClick={() => { setModal({ type: 'buy', stock: s }); setQuantity('') }}
                      className="bg-primary text-white text-xs font-semibold px-3 py-1.5 rounded-xl">
                      매수
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      <Modal isOpen={!!modal} onClose={() => { setModal(null); setQuantity('') }}
        title={modal?.type === 'buy' ? `${modal?.stock?.name ?? ''} 매수` : `${modal?.stock?.name ?? ''} 매도`}>
        <div className="space-y-3">
          <div className="bg-gray-50 rounded-2xl p-4">
            <p className="text-sm text-gray-500">현재가</p>
            <p className="text-xl font-bold text-gray-900">{formatAmount(Number(modal?.stock?.currentPrice ?? modal?.stock?.price ?? 0))}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">수량</label>
            <input type="number" className="input-field" placeholder="0" value={quantity}
              onChange={(e) => setQuantity(e.target.value)} />
          </div>
          {quantity && (
            <div className="bg-primary-50 rounded-2xl p-3">
              <p className="text-sm text-primary font-semibold">
                총 {modal?.type === 'buy' ? '매수' : '매도'}금액: {formatAmount(Number(modal?.stock?.currentPrice ?? modal?.stock?.price ?? 0) * Number(quantity))}
              </p>
            </div>
          )}
          <button
            className={`w-full py-3 rounded-2xl font-semibold text-white transition-colors ${modal?.type === 'buy' ? 'bg-red-500 hover:bg-red-600' : 'bg-blue-500 hover:bg-blue-600'}`}
            onClick={handleTrade} disabled={tradeLoading}>
            {tradeLoading ? '처리 중...' : (modal?.type === 'buy' ? '매수하기' : '매도하기')}
          </button>
        </div>
      </Modal>
    </div>
  )
}
