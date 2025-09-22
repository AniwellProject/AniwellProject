'use client'

import React from 'react'

type BaseModalProps = {
  title: string
  onClose: () => void
  children: React.ReactNode
}

export default function BaseModal({ title, onClose, children }: BaseModalProps) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-30 flex justify-center items-center z-50">
      <div className="relative bg-[#f8eaa7] w-[600px] h-[500px] rounded-2xl shadow-lg flex items-center justify-center">
        <div className="absolute left-[30px] top-0 w-[540px] h-[460px] bg-white rounded-2xl p-6 shadow-lg">
          {/* Header */}
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold">{title}</h2>
            <button onClick={onClose} className="text-gray-500 hover:text-black text-xl">×</button>
          </div>

          {/* Content */}
          <div className="overflow-auto max-h-[370px]">{children}</div>
        </div>
      </div>
    </div>
  )
}
