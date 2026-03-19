import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import BankingPage from './pages/BankingPage'
import InvestmentPage from './pages/InvestmentPage'
import PaymentPage from './pages/PaymentPage'
import InsurancePage from './pages/InsurancePage'
import SearchPage from './pages/SearchPage'
import NotificationPage from './pages/NotificationPage'
import AiPage from './pages/AiPage'
import ProtectedRoute from './components/ProtectedRoute'

function App() {
  return (
    <BrowserRouter>
      <Toaster
        position="top-center"
        toastOptions={{
          style: {
            background: '#1A1A1A',
            color: '#fff',
            borderRadius: '12px',
            fontSize: '14px',
            fontWeight: '500',
          },
          success: {
            iconTheme: { primary: '#00B37E', secondary: '#fff' },
          },
        }}
      />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/" element={<Navigate to="/home" replace />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route path="/home" element={<HomePage />} />
            <Route path="/banking" element={<BankingPage />} />
            <Route path="/investment" element={<InvestmentPage />} />
            <Route path="/payment" element={<PaymentPage />} />
            <Route path="/insurance" element={<InsurancePage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/notification" element={<NotificationPage />} />
            <Route path="/ai" element={<AiPage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
