'use client'
import { createContext, useContext, useState } from 'react'

interface Restaurant {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
}
interface InfoCardContextType {
  selectedId: number | null
  setSelectedId: (id: number | null) => void
  focusRestaurant: Restaurant | null;
  setFocusRestaurant: (r: Restaurant | null) => void;
}

const InfoCardContext = createContext<InfoCardContextType | undefined>(undefined)

export const InfoCardProvider = ({ children }: { children: React.ReactNode }) => {
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [focusRestaurant, setFocusRestaurant] = useState<Restaurant | null>(null);
  return (
    <InfoCardContext.Provider value={{ selectedId, setSelectedId, focusRestaurant, setFocusRestaurant }}>
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