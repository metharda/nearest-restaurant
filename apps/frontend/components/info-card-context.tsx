'use client';

import { createContext, useContext, useState, ReactNode } from 'react';

interface PathNode {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
}

interface PathData {
  path: PathNode[];
  status: string;
  restaurantId?: string;
  longitude?: string;
  latitude?: string;
  requestId?: string;
}

interface InfoCardContextType {
  selectedId: number | null;
  setSelectedId: (id: number | null) => void;
  focusRestaurant: { id: number; latitude: number; longitude: number } | null;
  setFocusRestaurant: (restaurant: { id: number; latitude: number; longitude: number } | null) => void;
  pathData: PathData | null;
  setPathData: (data: PathData | null) => void;
}

const InfoCardContext = createContext<InfoCardContextType | undefined>(undefined);

export function InfoCardProvider({ children }: { children: ReactNode }) {
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [focusRestaurant, setFocusRestaurant] = useState<{ id: number; latitude: number; longitude: number } | null>(null);
  const [pathData, setPathData] = useState<PathData | null>(null);

  return (
    <InfoCardContext.Provider 
      value={{ 
        selectedId, 
        setSelectedId, 
        focusRestaurant, 
        setFocusRestaurant,
        pathData,
        setPathData
      }}
    >
      {children}
    </InfoCardContext.Provider>
  );
}

export function useInfoCard() {
  const context = useContext(InfoCardContext);
  if (context === undefined) {
    throw new Error('useInfoCard must be used within an InfoCardProvider');
  }
  return context;
}