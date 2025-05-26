'use client'
import { Calendar, Home, Inbox, Search, Settings, ChefHat, MapPin, Star, MapPinIcon } from "lucide-react"
import { fetchRestaurants } from "@/lib/restaurant"
import { useEffect, useState } from "react"
import { cn } from "@/lib/utils"
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
import { Skeleton } from "@/components/ui/skeleton"

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
        // Extract restaurants array from the response
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

const getCurrentLocation = () => {
    return new Promise<{ latitude: number; longitude: number }>((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(new Error('Geolocation is not supported by this browser'))
            return
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords
                resolve({ latitude, longitude })
            },
            (error) => {
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        reject(new Error('Konum erişimi reddedildi'))
                        break
                    case error.POSITION_UNAVAILABLE:
                        reject(new Error('Konum bilgisi kullanılamıyor'))
                        break
                    case error.TIMEOUT:
                        reject(new Error('Konum alma işlemi zaman aşımına uğradı'))
                        break
                    default:
                        reject(new Error('Bilinmeyen bir hata oluştu'))
                        break
                }
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 300000 // 5 minutes
            }
        )
    })
}

  // Filter out generic "Restaurant" names and create menu items
  const restaurantItems = restaurants
    .filter((restaurant) => restaurant.name !== "Restaurant")
    .map((restaurant) => ({
      title: restaurant.name,
      icon: ChefHat,
      id: restaurant.id,
      coordinates: `${restaurant.latitude.toFixed(6)}, ${restaurant.longitude.toFixed(6)}`,
      restaurant: restaurant // Store full restaurant object for click handler
    }))

  const handleRestaurantClick = (restaurant: Restaurant) => {
    if (onRestaurantClick) {
      onRestaurantClick(restaurant)
    }
  }

  return (
    <Sidebar>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel className="flex items-center gap-2">
            <ChefHat className="h-4 w-4" />
            Restorantlar ({restaurantItems.length})
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu className="space-y-2">
              {loading ? (
                <>
                  {[...Array(3)].map((_, index) => (
                    <SidebarMenuItem key={index}>
                      <div className="p-3 rounded-lg border border-black bg-white">
                        <div className="flex items-start gap-3">
                          <Skeleton className="h-5 w-5 mt-0.5" />
                          <div className="min-w-0 flex-1">
                            <Skeleton className="h-5 w-3/4 mb-2" />
                            <Skeleton className="h-4 w-full" />
                          </div>
                        </div>
                      </div>
                    </SidebarMenuItem>
                  ))}
                </>
              ) : locationError ? (
                <SidebarMenuItem>
                  <div className="flex items-center gap-2 px-2 py-1.5 text-sm text-red-600">
                    <MapPinIcon className="h-4 w-4" />
                    <div>
                      <div className="font-medium">Konum Hatası</div>
                      <div className="text-xs">{locationError}</div>
                    </div>
                  </div>
                </SidebarMenuItem>
              ) : restaurantItems.length > 0 ? (
                restaurantItems.map((item) => (
                  <SidebarMenuItem key={item.id}>
                    <div 
                      className="p-3 rounded-lg border border-black bg-white hover:bg-gray-50 transition-colors cursor-pointer"
                      onClick={() => handleRestaurantClick(item.restaurant)}
                    >
                      <div className="flex items-start gap-3">
                        <item.icon className="h-5 w-5 shrink-0 mt-0.5" />
                        <div className="min-w-0 flex-1">
                          <div className="font-semibold text-base text-gray-900">{item.title}</div>
                          <div className="text-sm text-gray-600 mt-1">
                            📍 {item.coordinates}
                          </div>
                        </div>
                      </div>
                    </div>
                  </SidebarMenuItem>
                ))
              ) : (
                <SidebarMenuItem>
                  <div className="flex items-center gap-2 px-2 py-1.5 text-sm text-muted-foreground">
                    <MapPin className="h-4 w-4" />
                    {radius/1000}km çevresinde restoran bulunamadı
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