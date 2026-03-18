import { useNavigate, useLocation } from 'react-router-dom'

const pageTitles: Record<string, string> = {
  '/home': '홈',
  '/banking': '뱅킹',
  '/investment': '투자',
  '/payment': '결제',
  '/insurance': '보험',
  '/search': '검색',
  '/notification': '알림',
}

export default function TopHeader() {
  const navigate = useNavigate()
  const location = useLocation()
  const isHome = location.pathname === '/home'
  const title = pageTitles[location.pathname] ?? ''

  if (isHome) return null

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-sm border-b border-gray-100">
      <div className="max-w-md mx-auto flex items-center h-14 px-2">
        <button
          onClick={() => navigate(-1)}
          className="w-10 h-10 flex items-center justify-center rounded-xl hover:bg-gray-100 active:bg-gray-200 transition-colors"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#374151" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
        <span className="ml-1 text-base font-bold text-gray-900">{title}</span>
      </div>
    </header>
  )
}
