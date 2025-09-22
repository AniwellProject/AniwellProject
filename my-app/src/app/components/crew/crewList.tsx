'use client'

type Crew = {
  id: number
  name: string
  description: string
  memberCount: number
}

const dummyCrews: Crew[] = [
  { id: 1, name: '둔산 펫산책단', description: '매주 수요일 둔산동 산책', memberCount: 12 },
  { id: 2, name: '용문견친회', description: '강아지 산책 교류 모임', memberCount: 8 },
]

export default function CrewList() {
  return (
    <div className="space-y-4">
      {dummyCrews.map((crew) => (
        <div
          key={crew.id}
          className="bg-white rounded-xl shadow p-4 hover:shadow-lg transition duration-200 cursor-pointer"
        >
          <h3 className="text-lg font-bold text-gray-800">{crew.name}</h3>
          <p className="text-sm text-gray-600 mt-1">{crew.description}</p>
          <p className="text-xs text-gray-500 mt-2">👥 {crew.memberCount}명 참여 중</p>
        </div>
      ))}
    </div>
  )
}
