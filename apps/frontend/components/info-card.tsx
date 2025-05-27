"use client"

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

  useEffect(() => {
    if (!isOpen) return

    getRestaurantInfo(restaurantId)
      .then((info) => {
        setData({
          name: info.name || "Unnamed",
          type: info.tags?.cuisine || "Restaurant",
          location: `${info.adress?.street || "Unknown Street"}, ${info.adress?.district || "Unknown District"}, ${info.adress?.city || "Unknown City"}`,
          rating: parseFloat(info.stars) || 0,
          reviews: Math.floor(Math.random() * 500 + 20), // Simulate reviews
          hours: info.openingHours || "Unknown hours",
          description: info.description || "No description available.",
          features: Object.keys(info.tags || {}),
        })
      })
      .catch((error) => {
        console.error("Failed to fetch restaurant info:", error)
      })
  }, [isOpen, restaurantId])

  if (!data) return null

  return (
    <div
      className={cn(
        "fixed bottom-4 right-4 z-30 w-80 transition-transform duration-300 ease-in-out",
        isOpen ? "translate-y-0" : "translate-y-full"
      )}
    >
      <Card className="shadow-lg">
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
                    onClick={() => {
                    
                    }}
                >
                    Yol Tarifi Al
                </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}