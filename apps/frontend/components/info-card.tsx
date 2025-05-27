"use client"
import { PathResponse } from "@/lib/types"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { MapPin, Clock, Star, X } from "lucide-react"
import { cn } from "@/lib/utils"
import { getRestaurantInfo } from "@/lib/overpass"
import { useEffect, useState } from "react"
import { getPathtoRestaurant } from "@/lib/restaurant"
import { calcDistance, getCurrentLocation } from "@/lib/location"

interface InfoCardProps {
  isOpen: boolean
  onClose: () => void
  restaurantId: number
}

export function InfoCard({ isOpen, onClose, restaurantId }: InfoCardProps) {
  const [data, setData] = useState<null | {
    name: string
    type: string
    location: string
    rating: number
    reviews: number
    hours: string
    description: string
    features: string[]
  }>(null)
  const [pathData, setPathData] = useState<PathResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [pathLoading, setPathLoading] = useState(false)

  useEffect(() => {
    if (!isOpen) return
    
    const fetchRestaurantData = async () => {
      try {
        setLoading(true)
        const info = await getRestaurantInfo(restaurantId)
        const distance = await calcDistance(
          info.location.lat,
          info.location.lon
        )
        
        setData({
          name: info.name || "Unnamed",
          type: info.tags?.cuisine || "Restaurant",
          location: `${distance.toFixed(5)} km uzaklıkta`,
          rating: parseFloat((Math.random() * 2 + 3).toFixed(1)),
          reviews: Math.floor(Math.random() * 500 + 20),
          hours: info.openingHours || "Unknown hours",
          description: info.description || "No description available.",
          features: Object.keys(info.tags || {}),
        })
      } catch (error) {
        console.error("Failed to fetch restaurant info:", error)
      } finally {
        setLoading(false)
      }
    }
    
    fetchRestaurantData()
  }, [isOpen, restaurantId])

  if (!data) return null
  
  let onGetPathClick = async () => {
    try {
      setPathLoading(true)
      console.log('Starting path request...');
      const userLocation = await getCurrentLocation();
      console.log('User location:', userLocation);
            
      await getPathtoRestaurant(restaurantId.toString(), userLocation.latitude, userLocation.longitude)
      
      console.log("Path request initiated via WebSocket");
      
    } catch (error) {
      console.error("Error getting path:", error);
    } finally {
      setPathLoading(false)
    }
  }

  return (
    <div
      className={cn(
        "fixed bottom-4 right-4 z-30 w-80 transition-transform duration-300 ease-in-out",
        isOpen ? "translate-y-0" : "translate-y-full"
      )}
    >
      <Card className="shadow-lg">
        {loading ? (
          <CardContent className="p-6">
            <div className="space-y-3">
              <div className="h-4 bg-gray-200 rounded animate-pulse" />
              <div className="h-4 bg-gray-200 rounded animate-pulse w-2/3" />
              <div className="h-20 bg-gray-200 rounded animate-pulse" />
            </div>
          </CardContent>
        ) : data && (
          <>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg">{data.name}</CardTitle>
                <div className="flex items-center gap-2">
                  <Badge variant="secondary">{data.type}</Badge>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-6 w-6"
                    onClick={onClose}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              </div>
              <CardDescription className="flex items-center gap-1">
                <MapPin className="h-4 w-4" />
                {data.location}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-2">
                <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                <span className="text-sm font-medium">{data.rating}</span>
                <span className="text-sm text-muted-foreground">
                  ({data.reviews.toLocaleString()} reviews)
                </span>
              </div>

              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Clock className="h-4 w-4" />
                <span>{data.hours}</span>
              </div>

              <div className="space-y-2">
                <h4 className="text-sm font-medium">Description</h4>
                <p className="text-sm text-muted-foreground">{data.description}</p>
              </div>

              <div className="space-y-2">
                <h4 className="text-sm font-medium">Features</h4>
                <div className="flex flex-wrap gap-1">
                  {data.features.map((feature) => (
                    <Badge key={feature} variant="outline" className="text-xs">
                      {feature}
                    </Badge>
                  ))}
                </div>
                <div>
                  <Button
                    variant="default"
                    className="w-full mt-2"
                    onClick={onGetPathClick}
                    disabled={pathLoading}
                  >
                    {pathLoading ? "Yol Tarifi Hesaplanıyor..." : "Yol Tarifi Al"}
                  </Button>
                </div>
              </div>
            </CardContent>
          </>
        )}
      </Card>
    </div>
  )
}