import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { authApi } from '../api/services'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!email || !password) {
      toast.error('이메일과 비밀번호를 입력해주세요.')
      return
    }
    setLoading(true)
    try {
      const res = await authApi.login(email, password)
      const { accessToken } = res.data.data
      // 토큰으로 유저 정보 조회
      const userRes = await authApi.getMe(accessToken)
      const user = userRes.data.data
      setAuth(accessToken, user)
      toast.success('로그인 성공!')
      navigate('/home')
    } catch {
      toast.error('이메일 또는 비밀번호가 올바르지 않습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-white flex flex-col px-6 pt-16 max-w-md mx-auto">
      {/* Logo */}
      <div className="mb-12">
        <div className="w-12 h-12 bg-primary rounded-2xl flex items-center justify-center mb-4">
          <span className="text-white font-black text-xl">F</span>
        </div>
        <h1 className="text-2xl font-bold text-gray-900">안녕하세요!</h1>
        <p className="text-gray-500 mt-1">FinHub에 로그인하세요</p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div>
          <label className="text-sm font-medium text-gray-700 mb-1.5 block">이메일</label>
          <input
            type="email"
            className="input-field"
            placeholder="email@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-gray-700 mb-1.5 block">비밀번호</label>
          <input
            type="password"
            className="input-field"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </div>

        <button
          type="submit"
          className="btn-primary mt-4"
          disabled={loading}
        >
          {loading ? '로그인 중...' : '로그인'}
        </button>
      </form>

      <div className="mt-6 text-center">
        <span className="text-gray-500 text-sm">계정이 없으신가요? </span>
        <Link to="/signup" className="text-primary font-semibold text-sm">
          회원가입
        </Link>
      </div>
    </div>
  )
}
