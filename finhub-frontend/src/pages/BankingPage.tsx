import { useEffect, useState } from 'react'
import { bankingApi } from '../api/services'
import LoadingSpinner from '../components/LoadingSpinner'
import Modal from '../components/Modal'
import toast from 'react-hot-toast'

function formatAmount(amount: number) {
  return amount.toLocaleString('ko-KR') + '원'
}

export default function BankingPage() {
  const [accounts, setAccounts] = useState<any[]>([])
  const [transactions, setTransactions] = useState<any[]>([])
  const [selectedAccount, setSelectedAccount] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [txLoading, setTxLoading] = useState(false)

  const [transferModal, setTransferModal] = useState(false)
  const [transferForm, setTransferForm] = useState({ toAccountNumber: '', amount: '', description: '' })
  const [transferLoading, setTransferLoading] = useState(false)

  const [createModal, setCreateModal] = useState(false)
  const [accountName, setAccountName] = useState('')
  const [createLoading, setCreateLoading] = useState(false)

  const [depositModal, setDepositModal] = useState(false)
  const [depositAmount, setDepositAmount] = useState('')
  const [depositLoading, setDepositLoading] = useState(false)

  const loadAccounts = async () => {
    const res = await bankingApi.getAccounts()
    const data: any[] = res.data?.data ?? []
    setAccounts(data)
    return data
  }

  const loadTransactions = async (accountId: number) => {
    setTxLoading(true)
    try {
      const res = await bankingApi.getTransactions(accountId)
      setTransactions(res.data?.data?.content ?? res.data?.data ?? [])
    } finally {
      setTxLoading(false)
    }
  }

  useEffect(() => {
    loadAccounts()
      .then((data) => {
        if (data.length > 0) {
          setSelectedAccount(data[0])
          loadTransactions(data[0].id)
        }
      })
      .finally(() => setLoading(false))
  }, [])

  const handleSelectAccount = (acc: any) => {
    setSelectedAccount(acc)
    loadTransactions(acc.id)
  }

  const handleTransfer = async () => {
    if (!selectedAccount || !transferForm.toAccountNumber || !transferForm.amount) {
      toast.error('필수 항목을 입력해주세요.')
      return
    }
    setTransferLoading(true)
    try {
      await bankingApi.transfer({
        fromAccountId: selectedAccount.id,
        toAccountNumber: transferForm.toAccountNumber,
        amount: Number(transferForm.amount),
        description: transferForm.description,
      })
      toast.success('송금이 완료되었습니다.')
      setTransferModal(false)
      setTransferForm({ toAccountNumber: '', amount: '', description: '' })
      const data = await loadAccounts()
      const updated = data.find((a) => a.id === selectedAccount.id)
      if (updated) setSelectedAccount(updated)
      loadTransactions(selectedAccount.id)
    } finally {
      setTransferLoading(false)
    }
  }

  const handleDeposit = async () => {
    if (!selectedAccount || !depositAmount) {
      toast.error('금액을 입력해주세요.')
      return
    }
    setDepositLoading(true)
    try {
      await bankingApi.deposit(selectedAccount.id, Number(depositAmount))
      toast.success('충전이 완료되었습니다.')
      setDepositModal(false)
      setDepositAmount('')
      const data = await loadAccounts()
      const updated = data.find((a) => a.id === selectedAccount.id)
      if (updated) setSelectedAccount(updated)
      loadTransactions(selectedAccount.id)
    } finally {
      setDepositLoading(false)
    }
  }

  const handleCreateAccount = async () => {
    if (!accountName.trim()) {
      toast.error('계좌 이름을 입력해주세요.')
      return
    }
    setCreateLoading(true)
    try {
      await bankingApi.createAccount({ accountName: accountName.trim() })
      toast.success('계좌가 개설되었습니다.')
      setCreateModal(false)
      setAccountName('')
      const data = await loadAccounts()
      if (data.length > 0) {
        const newest = data[data.length - 1]
        setSelectedAccount(newest)
        loadTransactions(newest.id)
      }
    } finally {
      setCreateLoading(false)
    }
  }

  if (loading) return <LoadingSpinner className="py-32" />

  return (
    <div className="px-4 pt-4">
      {/* Accounts 가로 스크롤 */}
      {accounts.length > 0 && (
        <div className="flex gap-3 overflow-x-auto pb-2 mb-3" style={{ scrollbarWidth: 'none' }}>
          {accounts.map((acc: any) => (
            <button
              key={acc.id}
              onClick={() => handleSelectAccount(acc)}
              className={`flex-shrink-0 rounded-2xl p-4 min-w-[160px] text-left transition-all
                ${selectedAccount?.id === acc.id
                  ? 'bg-primary text-white shadow-lg'
                  : 'bg-white text-gray-900 shadow-card'}`}
            >
              <p className={`text-xs mb-2 ${selectedAccount?.id === acc.id ? 'text-primary-100' : 'text-gray-500'}`}>
                {acc.status ?? '입출금'}
              </p>
              <p className="font-bold text-sm mb-1">{acc.accountName}</p>
              <p className={`text-lg font-bold ${selectedAccount?.id === acc.id ? 'text-white' : 'text-gray-900'}`}>
                {formatAmount(Number(acc.balance ?? 0))}
              </p>
            </button>
          ))}
        </div>
      )}

      {/* 버튼 행: 계좌 개설 + 충전 + 송금 */}
      <div className="flex gap-2 mb-4">
        <button
          onClick={() => setCreateModal(true)}
          className="flex items-center justify-center gap-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-3 px-4 rounded-2xl transition-colors shrink-0"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          계좌 개설
        </button>
        {selectedAccount && (
          <>
            <button
              onClick={() => setDepositModal(true)}
              className="flex items-center justify-center gap-1.5 bg-primary-50 hover:bg-primary-100 text-primary font-semibold py-3 px-4 rounded-2xl transition-colors shrink-0"
            >
              💰 충전
            </button>
            <button
              onClick={() => setTransferModal(true)}
              className="flex-1 bg-primary text-white font-semibold py-3 px-4 rounded-2xl hover:bg-primary-600 transition-colors"
            >
              송금하기
            </button>
          </>
        )}
      </div>

      {/* Transactions */}
      <div className="card">
        <h2 className="font-bold text-gray-900 mb-4">거래내역</h2>
        {!selectedAccount ? (
          <p className="text-center text-gray-400 py-8 text-sm">계좌를 선택해주세요</p>
        ) : txLoading ? (
          <LoadingSpinner className="py-8" />
        ) : transactions.length === 0 ? (
          <p className="text-center text-gray-400 py-8 text-sm">거래내역이 없습니다</p>
        ) : (
          <div className="space-y-4">
            {transactions.map((tx: any) => {
              const isDeposit = tx.transactionType === 'DEPOSIT' || tx.transactionType === 'TRANSFER_IN'
              return (
                <div key={tx.id} className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${isDeposit ? 'bg-blue-50' : 'bg-red-50'}`}>
                      <span className="text-lg">{isDeposit ? '⬇️' : '⬆️'}</span>
                    </div>
                    <div>
                      <p className="font-medium text-sm text-gray-900">{tx.description || (isDeposit ? '입금' : '출금')}</p>
                      <p className="text-xs text-gray-400">{tx.createdAt ? new Date(tx.createdAt).toLocaleDateString('ko-KR') : ''}</p>
                    </div>
                  </div>
                  <p className={`font-semibold ${isDeposit ? 'text-blue-600' : 'text-red-500'}`}>
                    {isDeposit ? '+' : '-'}{formatAmount(Math.abs(Number(tx.amount ?? 0)))}
                  </p>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* 송금 모달 */}
      <Modal isOpen={transferModal} onClose={() => setTransferModal(false)} title="송금">
        <div className="space-y-3">
          <div className="bg-gray-50 rounded-2xl p-3">
            <p className="text-xs text-gray-500">보내는 계좌</p>
            <p className="font-semibold text-gray-900 mt-0.5">{selectedAccount?.accountName}</p>
            <p className="text-xs text-gray-400">{selectedAccount?.accountNumber}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">받는 계좌번호</label>
            <input type="text" className="input-field" placeholder="계좌번호 입력"
              value={transferForm.toAccountNumber}
              onChange={(e) => setTransferForm({ ...transferForm, toAccountNumber: e.target.value })} />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">금액</label>
            <input type="number" className="input-field" placeholder="0"
              value={transferForm.amount}
              onChange={(e) => setTransferForm({ ...transferForm, amount: e.target.value })} />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">메모 (선택)</label>
            <input type="text" className="input-field" placeholder="메모"
              value={transferForm.description}
              onChange={(e) => setTransferForm({ ...transferForm, description: e.target.value })} />
          </div>
          <button className="btn-primary mt-2" onClick={handleTransfer} disabled={transferLoading}>
            {transferLoading ? '송금 중...' : '송금하기'}
          </button>
        </div>
      </Modal>

      {/* 충전 모달 */}
      <Modal isOpen={depositModal} onClose={() => { setDepositModal(false); setDepositAmount('') }} title="충전">
        <div className="space-y-4">
          <div className="bg-gray-50 rounded-2xl p-3">
            <p className="text-xs text-gray-500">충전할 계좌</p>
            <p className="font-semibold text-gray-900 mt-0.5">{selectedAccount?.accountName}</p>
            <p className="text-xs text-gray-400">{selectedAccount?.accountNumber}</p>
          </div>
          <div className="grid grid-cols-3 gap-2">
            {[10000, 30000, 50000, 100000, 300000, 500000].map((preset) => (
              <button
                key={preset}
                onClick={() => setDepositAmount(String(Number(depositAmount || 0) + preset))}
                className="bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-medium py-2 rounded-xl transition-colors"
              >
                +{(preset / 10000)}만원
              </button>
            ))}
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">충전 금액</label>
            <input
              type="number"
              className="input-field"
              placeholder="0"
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
            />
            {depositAmount && (
              <p className="text-xs text-primary font-medium mt-1.5 ml-1">
                {Number(depositAmount).toLocaleString('ko-KR')}원
              </p>
            )}
          </div>
          <button className="btn-primary" onClick={handleDeposit} disabled={depositLoading}>
            {depositLoading ? '충전 중...' : '충전하기'}
          </button>
        </div>
      </Modal>

      {/* 계좌 개설 모달 */}
      <Modal isOpen={createModal} onClose={() => { setCreateModal(false); setAccountName('') }} title="계좌 개설">
        <div className="space-y-4">
          <div className="bg-primary-50 rounded-2xl p-4">
            <p className="text-sm text-primary font-medium">새 입출금 계좌를 개설합니다</p>
            <p className="text-xs text-primary/70 mt-1">계좌번호는 자동으로 발급됩니다</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 mb-1.5 block">계좌 이름</label>
            <input
              type="text"
              className="input-field"
              placeholder="예: 생활비 통장"
              value={accountName}
              onChange={(e) => setAccountName(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleCreateAccount()}
            />
          </div>
          <button className="btn-primary" onClick={handleCreateAccount} disabled={createLoading}>
            {createLoading ? '개설 중...' : '계좌 개설하기'}
          </button>
        </div>
      </Modal>
    </div>
  )
}
