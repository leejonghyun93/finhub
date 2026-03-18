import { useState, useCallback } from 'react'
import { searchApi } from '../api/services'
import LoadingSpinner from '../components/LoadingSpinner'

export default function SearchPage() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)

  const handleSearch = useCallback(async () => {
    if (!query.trim()) return
    setLoading(true)
    setSearched(true)
    try {
      const res = await searchApi.search(query)
      // Page 응답
      setResults(res.data?.data?.content ?? res.data?.data ?? [])
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }, [query])

  const categoryLabel: Record<string, string> = {
    STOCK: '주식', INSURANCE: '보험', ACCOUNT: '계좌',
  }

  return (
    <div className="px-4 pt-6">
      <div className="flex gap-2 mb-4">
        <div className="flex-1 relative">
          <svg className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input type="text" className="input-field pl-10" placeholder="금융 상품 검색..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
        </div>
        <button onClick={handleSearch} className="bg-primary text-white font-semibold px-4 rounded-2xl">검색</button>
      </div>

      {loading ? (
        <LoadingSpinner className="py-20" />
      ) : searched ? (
        results.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-4xl mb-3">🔍</p>
            <p className="text-gray-500">검색 결과가 없습니다</p>
            <p className="text-gray-400 text-sm mt-1">다른 검색어를 입력해보세요</p>
          </div>
        ) : (
          <div className="space-y-3">
            {results.map((r: any, i: number) => (
              <div key={r.id ?? i} className="card flex items-start gap-3">
                <div className="w-10 h-10 bg-primary-50 rounded-xl flex items-center justify-center shrink-0">
                  <span className="text-primary font-bold text-sm">
                    {(categoryLabel[r.category] ?? r.category ?? 'F')[0]}
                  </span>
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-0.5">
                    <p className="font-semibold text-gray-900">{r.name ?? r.title}</p>
                    {r.category && (
                      <span className="bg-gray-100 text-gray-500 text-xs px-2 py-0.5 rounded-full">
                        {categoryLabel[r.category] ?? r.category}
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-gray-500">{r.description ?? r.content ?? ''}</p>
                </div>
              </div>
            ))}
          </div>
        )
      ) : (
        <div className="text-center py-16">
          <p className="text-4xl mb-3">💡</p>
          <p className="text-gray-500">원하는 금융 상품을 검색해보세요</p>
          <p className="text-gray-400 text-sm mt-1">주식, 보험, 계좌 등 통합 검색</p>
        </div>
      )}
    </div>
  )
}
