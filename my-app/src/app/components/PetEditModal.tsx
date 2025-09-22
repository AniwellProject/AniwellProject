'use client'

import BaseModal from './BaseModal'

type Props = {
  onClose: () => void
}

export default function PetEditModal({ onClose }: Props) {
  return (
    <BaseModal title="반려동물 정보수정" onClose={onClose}>
      <div className="flex gap-6">
        <div className="w-32 h-32 bg-gray-300 rounded-md"></div>

        <div className="flex-1 grid grid-cols-2 gap-2 text-sm">
          <input className="border px-2 py-1 rounded" placeholder="이름" />
          <input className="border px-2 py-1 rounded" placeholder="품종" />
          <input className="border px-2 py-1 rounded" placeholder="생일" />
          <input className="border px-2 py-1 rounded" placeholder="성별" />
          <input className="border px-2 py-1 rounded" placeholder="중성화 여부" />
          <input className="border px-2 py-1 rounded" placeholder="체중" />
          <input className="border px-2 py-1 rounded col-span-2" placeholder="기타 정보" />
        </div>
      </div>

      <div className="mt-6 flex justify-end">
        <button className="bg-green-200 hover:bg-green-300 px-4 py-2 rounded">완료</button>
      </div>
    </BaseModal>
  )
}
