'use client';

import { useEffect, useRef, useState } from 'react';
import 'leaflet/dist/leaflet.css';
import { fetchRestaurants } from '@/lib/restaurant';
import { locationIcon, restaurantIcon } from '@/lib/icons';

const radius = 2000;

interface Restaurant {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
}

export default function Map() {
  const mapRef = useRef<HTMLDivElement>(null);
  const leafletMapRef = useRef<any>(null);
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [userLocation, setUserLocation] = useState<{ latitude: number; longitude: number } | null>(null);

  useEffect(() => {
    const initializeMap = async () => {
      if (!mapRef.current) return;

      try {
        // Get user location
        const location = await getCurrentLocation();
        setUserLocation(location);

        // Fetch restaurants
        const response = await fetchRestaurants(radius, location.latitude, location.longitude);
        const restaurantData = response.restaurants || [];
        
        // Filter out generic "Restaurant" names
        const filteredRestaurants = restaurantData.filter(
          (restaurant: Restaurant) => restaurant.name !== "Restaurant"
        );
        setRestaurants(filteredRestaurants);

        // Initialize Leaflet map
        const L = (await import('leaflet')).default;
        
        // Create map centered on user location
        const map = L.map(mapRef.current).setView([location.latitude, location.longitude], 15);
        
        // Add tile layer
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        // Add user location marker
        L.marker([location.latitude, location.longitude], { icon: locationIcon })
          .addTo(map)
          .bindPopup('Your Location')
          .openPopup();

        // Add restaurant markers
        filteredRestaurants.forEach((restaurant: Restaurant) => {
          L.marker([restaurant.latitude, restaurant.longitude], { icon: restaurantIcon })
            .addTo(map)
            .bindPopup(`
              <div>
                <h3 style="margin: 0 0 8px 0; font-weight: bold;">${restaurant.name}</h3>
                <p style="margin: 0; font-size: 12px; color: #666;">
                  📍 ${restaurant.latitude.toFixed(6)}, ${restaurant.longitude.toFixed(6)}
                </p>
              </div>
            `);
        });

        // Store map reference for cleanup
        leafletMapRef.current = map;

      } catch (error) {
        console.error('Failed to initialize map:', error);
      }
    };

    initializeMap();

    // Cleanup function
    return () => {
      if (leafletMapRef.current) {
        leafletMapRef.current.remove();
        leafletMapRef.current = null;
      }
    };
  }, []);

  const getCurrentLocation = () => {
    return new Promise<{ latitude: number; longitude: number }>((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by this browser'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          resolve({ latitude, longitude });
        },
        (error) => {
          switch (error.code) {
            case error.PERMISSION_DENIED:
              reject(new Error('Konum erişimi reddedildi'));
              break;
            case error.POSITION_UNAVAILABLE:
              reject(new Error('Konum bilgisi kullanılamıyor'));
              break;
            case error.TIMEOUT:
              reject(new Error('Konum alma işlemi zaman aşımına uğradı'));
              break;
            default:
              reject(new Error('Bilinmeyen bir hata oluştu'));
              break;
          }
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 300000 // 5 minutes
        }
      );
    });
  };

  return (
    <div
      ref={mapRef}
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 0,
      }}
    />
  );
}