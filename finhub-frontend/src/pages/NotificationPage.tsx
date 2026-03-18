import { useEffect, useState } from 'react'
import { notificationApi } from '../api/services'
import LoadingSpinner from '../components/LoadingSpinner'
import toast from 'react-hot-toast'

export default function NotificationPage() {
  const [notifications, setNotifications] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    notificationApi.getNotifications()
      .then((res) => {
        // Page 응답: res.data.data.content
        setNotifications(res.data?.data?.content ?? [])
      })
      .catch(() => setNotifications([]))
      .finally(() => setLoading(false))
  }, [])

  const handleMarkAsRead = async (id: number) => {
    await notificationApi.markAsRead(id).catch(() => null)
    setNotifications((prev) => prev.map((n) => n.id === id ? { ...n, isRead: true } : n))
  }

  const handleMarkAllAsRead = async () => {
    await notificationApi.markAllAsRead().catch(() => null)
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })))
    toast.success('모든 알림을 읽음 처리했습니다.')
  }

  const unreadCount = notifications.filter((n) => !n.isRead).length

  const typeIcon: Record<string, string> = {
    TRANSFER: '💸', PAYMENT: '💳', INVESTMENT: '📈', INSURANCE: '🛡️', SYSTEM: '🔔',
  }

  return (
    <div className="px-4 pt-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          {unreadCount > 0 && <p className="text-sm text-gray-500">읽지 않은 알림 {unreadCount}개</p>}
        </div>
        {unreadCount > 0 && (
          <button onClick={handleMarkAllAsRead} className="text-primary text-sm font-medium">모두 읽음</button>
        )}
      </div>

      {loading ? (
        <LoadingSpinner className="py-20" />
      ) : notifications.length === 0 ? (
        <div className="text-center py-20">
          <p className="text-4xl mb-3">🔔</p>
          <p className="text-gray-500">알림이 없습니다</p>
        </div>
      ) : (
        <div className="space-y-2">
          {notifications.map((n: any) => (
            <button key={n.id} onClick={() => !n.isRead && handleMarkAsRead(n.id)}
              className={`w-full text-left p-4 rounded-2xl flex items-start gap-3 transition-colors ${n.isRead ? 'bg-white' : 'bg-primary-50'}`}>
              <span className="text-xl shrink-0">{typeIcon[n.type] ?? '🔔'}</span>
              <div className="flex-1">
                <p className={`text-sm ${n.isRead ? 'text-gray-600' : 'text-gray-900 font-medium'}`}>{n.message}</p>
                <p className="text-xs text-gray-400 mt-1">{n.createdAt ? new Date(n.createdAt).toLocaleString('ko-KR') : ''}</p>
              </div>
              {!n.isRead && <div className="w-2 h-2 bg-primary rounded-full mt-1.5 shrink-0" />}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
