'use client';

import 'leaflet/dist/leaflet.css';
import { useEffect, useRef, useState } from 'react';
import { fetchRestaurants } from '@/lib/restaurant';
import { locationIcon, restaurantIcon } from '@/lib/icons';
import { getCurrentLocation } from '@/lib/location';
import { useInfoCard } from '@/components/info-card-context';

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
  const { setSelectedId } = useInfoCard(); // Use context to trigger InfoCard

  useEffect(() => {
    const initializeMap = async () => {
      if (!mapRef.current) return;

      try {
        const location = await getCurrentLocation();
        setUserLocation(location);

        const response = await fetchRestaurants(radius, location.latitude, location.longitude);
        const restaurantData = response.restaurants || [];

        const filteredRestaurants = restaurantData.filter(
          (restaurant: Restaurant) => restaurant.name !== 'Restaurant'
        );
        setRestaurants(filteredRestaurants);

        const L = (await import('leaflet')).default;

        const map = L.map(mapRef.current, { zoomControl: false }).setView(
          [location.latitude, location.longitude],
          15
        );

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '© OpenStreetMap contributors',
        }).addTo(map);

        L.control.zoom({ position: 'bottomright' }).addTo(map);

        L.marker([location.latitude, location.longitude], { icon: locationIcon })
          .addTo(map)
          .bindPopup('Your Location')
          .openPopup();

        filteredRestaurants.forEach((restaurant: Restaurant) => {
          const marker = L.marker([restaurant.latitude, restaurant.longitude], {
            icon: restaurantIcon,
          })
            .addTo(map)
            .bindPopup(`
              <div>
                <h3 style="margin: 0 0 8px 0; font-weight: bold;">${restaurant.name}</h3>
                <p style="margin: 0; font-size: 12px; color: #666;">
                  📍 ${restaurant.latitude.toFixed(6)}, ${restaurant.longitude.toFixed(6)}
                </p>
              </div>
            `);

          marker.on('click', () => {
            setSelectedId(restaurant.id); // Trigger InfoCard
          });
        });

        leafletMapRef.current = map;
      } catch (error) {
        console.error('Failed to initialize map:', error);
      }
    };

    initializeMap();

    return () => {
      if (leafletMapRef.current) {
        leafletMapRef.current.remove();
        leafletMapRef.current = null;
      }
    };
  }, [setSelectedId]);

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