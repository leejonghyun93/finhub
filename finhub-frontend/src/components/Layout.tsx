import { Outlet } from 'react-router-dom'
import BottomNav from './BottomNav'
import TopHeader from './TopHeader'

export default function Layout() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <TopHeader />
      <main className="flex-1 pb-20 max-w-md mx-auto w-full">
        <Outlet />
      </main>
      <BottomNav />
    </div>
  )
}
