'use client'

import { createContext, useContext, useState } from 'react'

interface InfoCardContextType {
  selectedId: number | null
  setSelectedId: (id: number | null) => void
}

const InfoCardContext = createContext<InfoCardContextType | undefined>(undefined)

export const InfoCardProvider = ({ children }: { children: React.ReactNode }) => {
  const [selectedId, setSelectedId] = useState<number | null>(null)

  return (
    <InfoCardContext.Provider value={{ selectedId, setSelectedId }}>
      {children}
    </InfoCardContext.Provider>
  )
}

export const useInfoCard = () => {
  const context = useContext(InfoCardContext)
  if (!context) {
    throw new Error('useInfoCard must be used within an InfoCardProvider')
  }
  return context
}