// src/components/crew/Sidebar.tsx
'use client'

import { useEffect, useState } from 'react'

export default function Sidebar() {
  const [city, setCity] = useState<string | null>(null)
  const [district, setDistrict] = useState<string | null>(null)
  const [dongs, setDongs] = useState<string[]>([])

  useEffect(() => {
    const userCity = localStorage.getItem("userCity")
    const userDistrict = localStorage.getItem("userDistrict")

    if (userCity && userDistrict) {
      setCity(userCity)
      setDistrict(userDistrict)

      fetch(`/usr/walkCrew/getDongs?city=${userCity}&district=${userDistrict}`)
        .then(res => res.json())
        .then(data => {
          console.log("✅ 동 리스트:", data)
          setDongs(data)
        })
        .catch(err => console.error("❌ 동 목록 요청 실패:", err))
    }
  }, [])

  return (
    <aside className="bg-white p-6 shadow mx-auto h-[90%]">
      <h2 className="text-lg font-semibold mb-4">{city || '로딩중'}</h2>
      <div className="text-xl font-semibold mb-4 border-b border-gray-300 pb-2">
        {district || '로딩중'}
      </div>
      <ul className="grid grid-cols-2 gap-2 text-sm text-gray-700">
        {dongs.map((dong, idx) => (
          <li key={idx}>
            <button
              className="w-full px-1 py-2 text-left bg-gray-100 hover:bg-yellow-200 rounded-lg transition duration-200 ease-in-out shadow-sm hover:shadow-md"
            >
              {dong}
            </button>
          </li>
        ))}
      </ul>
    </aside>
  )
}
