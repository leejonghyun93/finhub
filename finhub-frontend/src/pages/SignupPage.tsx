import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { authApi } from '../api/services'
import toast from 'react-hot-toast'

export default function SignupPage() {
  const [form, setForm] = useState({ email: '', password: '', name: '', phone: '' })
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.email || !form.password || !form.name) {
      toast.error('필수 항목을 입력해주세요.')
      return
    }
    setLoading(true)
    try {
      await authApi.signup(form)
      toast.success('회원가입 완료! 로그인해주세요.')
      navigate('/login')
    } catch {
      toast.error('회원가입에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-white flex flex-col px-6 pt-12 max-w-md mx-auto">
      <div className="mb-10">
        <button onClick={() => navigate(-1)} className="mb-4">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#374151" strokeWidth="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 className="text-2xl font-bold text-gray-900">회원가입</h1>
        <p className="text-gray-500 mt-1">FinHub 계정을 만들어보세요</p>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div>
          <label className="text-sm font-medium text-gray-700 mb-1.5 block">이름 *</label>
          <input
            type="text"
            className="input-field"
            placeholder="홍길동"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
        </div>
        <div>
          <label className="text-sm font-medium text-gray-700 mb-1.5 block">이메일 *</label>
          <input
            type="email"
            className="input-field"
            placeholder="email@example.com"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
        </div>
        <div>
          <label className="text-sm font-medium text-gray-700 mb-1.5 block">비밀번호 *</label>
          <input
            type="password"
            className="input-field"
            placeholder="8자 이상 입력"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
        </div>
        <div>
          <label className="text-sm font-medium text-gray-700 mb-1.5 block">전화번호</label>
          <input
            type="tel"
            className="input-field"
            placeholder="010-0000-0000"
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
          />
        </div>

        <button type="submit" className="btn-primary mt-4" disabled={loading}>
          {loading ? '가입 중...' : '가입하기'}
        </button>
      </form>

      <div className="mt-6 text-center">
        <span className="text-gray-500 text-sm">이미 계정이 있으신가요? </span>
        <Link to="/login" className="text-primary font-semibold text-sm">로그인</Link>
      </div>
    </div>
  )
}
