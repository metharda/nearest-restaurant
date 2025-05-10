'use client';

import { useEffect, useRef } from 'react';
import 'leaflet/dist/leaflet.css';
import { fetchNearbyRestaurants } from '@/lib/overpass';
import { locationIcon, restaurantIcon } from '@/components/icons';

export default function Map() {
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    async function loadMap() {
      const L = await import('leaflet');

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;

          const map = L.map(mapRef.current!).setView([lat, lon], 14);

          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors',
          }).addTo(map);

          L.marker([lat, lon],{ icon: locationIcon })
            .addTo(map)
            .bindPopup('Konumunuz')
            .openPopup();

          const restaurants = await fetchNearbyRestaurants(lat, lon);
          restaurants.forEach((place: any) => {
            if (place.lat && place.lon) {
              L.marker([place.lat, place.lon],{ icon: restaurantIcon })
                .addTo(map)
                .bindPopup(place.tags?.name || 'Restoran');
            }
          });
        },
        (error) => {
          alert('Konum alınamadı: ' + error.message);
        }
      );
    }

    loadMap();
  }, []);

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