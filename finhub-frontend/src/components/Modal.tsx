import { useEffect } from 'react'

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title: string
  children: React.ReactNode
}

export default function Modal({ isOpen, onClose, title, children }: ModalProps) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
    return () => { document.body.style.overflow = '' }
  }, [isOpen])

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-t-3xl w-full max-w-md flex flex-col" style={{ maxHeight: '85dvh' }}>
        <div className="px-6 pt-6 pb-0 shrink-0">
          <div className="w-10 h-1 bg-gray-200 rounded-full mx-auto mb-6" />
          <h2 className="text-lg font-bold text-gray-900 mb-4">{title}</h2>
        </div>
        <div className="overflow-y-auto min-h-0 px-6 pb-24">
          {children}
        </div>
      </div>
    </div>
  )
}
