'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

const menus = [
  { href: '/my-page', label: '나의 정보' },
  { href: '/crew', label: '산책 크루' },
]

export default function Sidebar() {
  const pathname = usePathname()

  return (
    <div className="w-48 content-end flex flex-col gap-3">
      {menus.map((menu) => {
        const isActive = pathname === menu.href
        return (
          <Link key={menu.href} href={menu.href}>
            <div
              className={`content-center relative px-1 py-3 rounded-xl text-center font-semibold shadow-md cursor-pointer transition
                ${isActive ? 'bg-[#FEEEA4]' : 'bg-[#e4f0b9] hover:bg-[#FEEEA4]'}
              `}
            >
              {menu.label}

              {isActive && (
                <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-emerald-300" />
              )}
            </div>
          </Link>
        )
      })}
    </div>
  )
}
