import { useState, useRef, useEffect } from 'react'
import LoadingSpinner from '../components/LoadingSpinner'
import { aiApi } from '../api/services'

type Tab = 'chat' | 'recommend' | 'spending'

interface ChatMessage {
  role: 'user' | 'ai'
  content: string
}

interface RecommendProduct {
  id: number
  name: string
  category: string
  description: string
}

interface RecommendResult {
  products: RecommendProduct[]
  recommendation: string
}

export default function AiPage() {
  const [activeTab, setActiveTab] = useState<Tab>('chat')

  // Chat state
  const [messages, setMessages] = useState<ChatMessage[]>([
    { role: 'ai', content: '안녕하세요! 금융 AI 어시스턴트입니다. 금융 상품, 투자, 절약 방법 등 무엇이든 물어보세요.' },
  ])
  const [input, setInput] = useState('')
  const [chatLoading, setChatLoading] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  // Recommend state
  const [keyword, setKeyword] = useState('')
  const [recommendResult, setRecommendResult] = useState<RecommendResult | null>(null)
  const [recommendLoading, setRecommendLoading] = useState(false)
  const [recommendSearched, setRecommendSearched] = useState(false)

  // Spending state
  const [spendingResult, setSpendingResult] = useState<string | null>(null)
  const [spendingLoading, setSpendingLoading] = useState(false)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  async function sendMessage() {
    const text = input.trim()
    if (!text || chatLoading) return
    setInput('')
    setMessages((prev) => [...prev, { role: 'user', content: text }])
    setChatLoading(true)
    try {
      const res = await aiApi.chat(text)
      const reply = res.data?.data?.reply ?? res.data?.response ?? '응답을 받지 못했습니다.'
      setMessages((prev) => [...prev, { role: 'ai', content: reply }])
    } catch {
      setMessages((prev) => [...prev, { role: 'ai', content: '오류가 발생했습니다. 다시 시도해 주세요.' }])
    } finally {
      setChatLoading(false)
    }
  }

  async function searchRecommend() {
    const q = keyword.trim()
    if (!q || recommendLoading) return
    setRecommendLoading(true)
    setRecommendSearched(true)
    setRecommendResult(null)
    try {
      const res = await aiApi.recommend(q)
      const data = res.data?.data
      setRecommendResult({
        products: data?.products ?? [],
        recommendation: data?.recommendation ?? '',
      })
    } catch {
      setRecommendResult(null)
    } finally {
      setRecommendLoading(false)
    }
  }

  async function analyzeSpending() {
    if (spendingLoading) return
    setSpendingLoading(true)
    setSpendingResult(null)
    try {
      const res = await aiApi.analyzeSpending()
      setSpendingResult(res.data?.data?.analysis ?? res.data?.data?.result ?? JSON.stringify(res.data?.data ?? res.data))
    } catch {
      setSpendingResult('분석 중 오류가 발생했습니다. 다시 시도해 주세요.')
    } finally {
      setSpendingLoading(false)
    }
  }

  const tabs: { key: Tab; label: string }[] = [
    { key: 'chat', label: 'AI 챗봇' },
    { key: 'recommend', label: '상품 추천' },
    { key: 'spending', label: '지출 분석' },
  ]

  return (
    <div className="flex flex-col h-full">
      {/* Tab bar */}
      <div className="flex border-b border-gray-100 bg-white">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 py-3 text-sm font-semibold transition-colors ${
              activeTab === tab.key
                ? 'text-primary border-b-2 border-primary'
                : 'text-gray-400'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Chat Tab */}
      {activeTab === 'chat' && (
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3">
            {messages.map((msg, i) => (
              <div
                key={i}
                className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                {msg.role === 'ai' && (
                  <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center mr-2 flex-shrink-0 mt-1">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="white">
                      <path d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2zM9 14a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zm6 0a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3z" />
                    </svg>
                  </div>
                )}
                <div
                  className={`max-w-[72%] px-4 py-3 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap ${
                    msg.role === 'user'
                      ? 'bg-primary text-white rounded-br-sm'
                      : 'bg-gray-100 text-gray-800 rounded-bl-sm'
                  }`}
                >
                  {msg.content}
                </div>
              </div>
            ))}
            {chatLoading && (
              <div className="flex justify-start">
                <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center mr-2 flex-shrink-0">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="white">
                    <path d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2zM9 14a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zm6 0a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3z" />
                  </svg>
                </div>
                <div className="bg-gray-100 rounded-2xl rounded-bl-sm px-4 py-3 flex gap-1 items-center">
                  <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                  <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                  <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input area */}
          <div className="border-t border-gray-100 bg-white px-4 py-3 flex gap-2 items-end">
            <textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault()
                  sendMessage()
                }
              }}
              placeholder="금융 관련 질문을 입력하세요..."
              rows={1}
              className="flex-1 resize-none rounded-2xl border border-gray-200 px-4 py-2.5 text-sm focus:outline-none focus:border-primary transition-colors"
              style={{ maxHeight: '120px' }}
            />
            <button
              onClick={sendMessage}
              disabled={!input.trim() || chatLoading}
              className="w-10 h-10 rounded-full bg-primary flex items-center justify-center disabled:opacity-40 transition-opacity flex-shrink-0"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5">
                <line x1="22" y1="2" x2="11" y2="13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
            </button>
          </div>
        </div>
      )}

      {/* Recommend Tab */}
      {activeTab === 'recommend' && (
        <div className="flex-1 overflow-y-auto px-4 py-4">
          <p className="text-sm text-gray-500 mb-3">관심 있는 금융 키워드를 입력하면 AI가 맞춤 상품을 추천해 드립니다.</p>
          <div className="flex gap-2 mb-4">
            <input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && searchRecommend()}
              placeholder="예: 적금, ETF, 대출..."
              className="input-field flex-1 min-w-0"
            />
            <button
              onClick={searchRecommend}
              disabled={!keyword.trim() || recommendLoading}
              className="btn-primary !w-auto px-6 flex-shrink-0 disabled:opacity-40"
            >
              검색
            </button>
          </div>

          {recommendLoading && (
            <div className="flex flex-col items-center py-12 gap-3">
              <LoadingSpinner />
              <p className="text-sm text-gray-400">AI가 맞춤 상품을 분석하고 있습니다...</p>
            </div>
          )}

          {!recommendLoading && recommendSearched && !recommendResult?.products.length && (
            <div className="text-center py-12 text-gray-400 text-sm">추천 결과가 없습니다.</div>
          )}

          {!recommendLoading && recommendResult && recommendResult.products.length > 0 && (
            <div className="space-y-4">
              {/* 상품 목록 */}
              <div className="space-y-2">
                {recommendResult.products.map((product) => (
                  <div key={product.id} className="card p-4">
                    <div className="flex items-center justify-between mb-1.5">
                      <span className="font-semibold text-gray-900 text-sm">{product.name}</span>
                      <span className="text-xs bg-primary-50 text-primary px-2 py-0.5 rounded-full font-medium flex-shrink-0 ml-2">
                        {product.category}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500 leading-relaxed">{product.description}</p>
                  </div>
                ))}
              </div>

              {/* AI 종합 추천 분석 */}
              {recommendResult.recommendation && (
                <div className="card p-4">
                  <div className="flex items-center gap-2 mb-3">
                    <div className="w-6 h-6 rounded-full bg-primary flex items-center justify-center flex-shrink-0">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="white">
                        <path d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2z" />
                      </svg>
                    </div>
                    <span className="text-sm font-semibold text-gray-900">AI 종합 추천 분석</span>
                  </div>
                  <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">
                    {recommendResult.recommendation}
                  </p>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Spending Tab */}
      {activeTab === 'spending' && (
        <div className="flex-1 overflow-y-auto px-4 py-4">
          <div className="card p-5 mb-4 text-center">
            <div className="w-16 h-16 rounded-full bg-primary-50 flex items-center justify-center mx-auto mb-3">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#00B37E" strokeWidth="1.8">
                <path d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm-8 2a2 2 0 1 0 0 4 2 2 0 0 0 0-4z" />
              </svg>
            </div>
            <h3 className="font-bold text-gray-900 mb-1">AI 지출 분석</h3>
            <p className="text-sm text-gray-500 mb-4">
              최근 거래 내역을 AI가 분석하여 소비 패턴과 절약 팁을 알려드립니다.
            </p>
            <button
              onClick={analyzeSpending}
              disabled={spendingLoading}
              className="btn-primary w-full disabled:opacity-40"
            >
              {spendingLoading ? '분석 중...' : '지출 분석 시작'}
            </button>
          </div>

          {spendingLoading && (
            <div className="flex flex-col items-center py-8 gap-3">
              <LoadingSpinner />
              <p className="text-sm text-gray-400">AI가 지출 패턴을 분석하고 있습니다...</p>
            </div>
          )}

          {spendingResult && !spendingLoading && (
            <div className="card p-4">
              <div className="flex items-center gap-2 mb-3">
                <div className="w-6 h-6 rounded-full bg-primary flex items-center justify-center">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="white">
                    <path d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2z" />
                  </svg>
                </div>
                <span className="text-sm font-semibold text-gray-900">AI 분석 결과</span>
              </div>
              <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{spendingResult}</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
