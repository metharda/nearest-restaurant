'use client'
import { ChefHat, MapPin, MapPinIcon } from "lucide-react"
import { fetchRestaurants } from "@/lib/restaurant"
import { getCurrentLocation } from "@/lib/location"
import { useEffect, useState } from "react"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuItem,
} from "@/components/ui/sidebar"

const radius = 2000

interface Restaurant {
  id: number
  name: string
  latitude: number
  longitude: number
}

interface SidebarAppProps {
  onRestaurantClick?: (restaurant: Restaurant) => void
}

export function SidebarApp({ onRestaurantClick }: SidebarAppProps) {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [loading, setLoading] = useState(true)
  const [locationError, setLocationError] = useState<string | null>(null)

  useEffect(() => {
    const loadRestaurants = async () => {
      try {
        const { latitude, longitude } = await getCurrentLocation()
        const response = await fetchRestaurants(
          radius,
          latitude,
          longitude
        )
        setRestaurants(response.restaurants || [])
        setLocationError(null)
      } catch (error) {
        console.error('Failed to fetch restaurants:', error)
        setLocationError(error instanceof Error ? error.message : 'Bilinmeyen bir hata oluştu')
      } finally {
        setLoading(false)
      }
    }
    loadRestaurants()
  }, [])

  // Filter out generic "Restaurant" names and create menu items
  const restaurantItems = restaurants
    .filter((restaurant) => restaurant.name !== "Restaurant")
    .map((restaurant) => ({
      title: restaurant.name,
      icon: ChefHat,
      id: restaurant.id,
      coordinates: `${restaurant.latitude.toFixed(6)}, ${restaurant.longitude.toFixed(6)}`,
      restaurant: restaurant
    }))

  const handleRestaurantClick = (restaurant: Restaurant) => {
    if (onRestaurantClick) {
      onRestaurantClick(restaurant)
    }
  }

  return (
    <Sidebar variant="floating" className="w-80">
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel className="flex items-center gap-2 text-lg font-semibold">
            Restorantlar ({restaurantItems.length})
          </SidebarGroupLabel>
          <div className="h-px bg-gray-200 my-2" />
          <SidebarGroupContent>
            <SidebarMenu className="space-y-2">
              {loading ? (
                <>
                  {[...Array(3)].map((_, index) => (
                    <SidebarMenuItem key={index}>
                      <div className="p-2 rounded-md border border-gray-200 bg-white shadow-sm">
                        <div className="flex items-start gap-2">
                          <Skeleton className="h-4 w-4 mt-0.5 rounded-full" />
                          <div className="min-w-0 flex-1">
                            <Skeleton className="h-4 w-3/4 mb-1 rounded" />
                            <Skeleton className="h-3 w-full rounded" />
                          </div>
                        </div>
                      </div>
                    </SidebarMenuItem>
                  ))}
                </>
              ) : locationError ? (
                <SidebarMenuItem>
                  <div className="flex items-center gap-2 p-2 rounded-md border border-red-200 bg-red-50 text-red-700">
                    <MapPinIcon className="h-4 w-4 shrink-0" />
                    <div>
                      <div className="font-medium text-sm">Konum Hatası</div>
                      <div className="text-xs text-red-600">{locationError}</div>
                    </div>
                  </div>
                </SidebarMenuItem>
              ) : restaurantItems.length > 0 ? (
                restaurantItems.map((item) => (
                  <SidebarMenuItem key={item.id}>
                    <div
                      className="p-2 rounded-md border border-gray-200 bg-white hover:bg-gray-50 hover:border-gray-300 hover:shadow-md transition-all duration-200 cursor-pointer group"
                      onClick={() => handleRestaurantClick(item.restaurant)}
                    >
                      <div className="flex items-start gap-2">
                        <div className="p-1 rounded-full bg-orange-100 group-hover:bg-orange-200 transition-colors duration-200">
                          <item.icon className="h-4 w-4 text-orange-600" />
                        </div>
                        <div className="min-w-0 flex-1">
                          <div className="font-medium text-sm text-gray-900 mb-0.5 group-hover:text-gray-800 transition-colors duration-200">
                            {item.title}
                          </div>
                            <div className="text-xs text-gray-500 flex items-center gap-1">
                            <span>📍</span>
                            {item.coordinates}
                            </div>
                        </div>
                      </div>
                    </div>
                  </SidebarMenuItem>
                ))
              ) : (
                <SidebarMenuItem>
                  <div className="flex items-center gap-2 p-2 rounded-md border border-gray-200 bg-gray-50 text-gray-600">
                    <MapPin className="h-4 w-4" />
                    <span className="text-sm">
                      {radius/1000}km çevresinde restoran bulunamadı
                    </span>
                  </div>
                </SidebarMenuItem>
              )}
            </SidebarMenu>
    </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
        </Sidebar>
      )
    }