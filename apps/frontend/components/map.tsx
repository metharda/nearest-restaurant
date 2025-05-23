'use client';
import Sidebar from './sidebar';


import 'leaflet/dist/leaflet.css';
import { fetchNearbyRestaurants } from '@/lib/overpass';

import { useState, useEffect, useRef } from 'react';


export default function Map() {
  const mapRef = useRef<HTMLDivElement>(null);
  const [restaurants, setRestaurants] = useState<any[]>([]);

  useEffect(() => {
    async function loadMap() {
      const L = await import('leaflet');

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;

          const map = L.map(mapRef.current!).setView([lat, lon], 14);

          const locationIcon = L.icon({
  iconUrl: '/location.png',
  iconSize: [30, 30],
  iconAnchor: [15, 30],
});



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
              L.marker([place.lat, place.lon],{ icon: restaurants })
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
  <>
    <Sidebar restaurants={restaurants} />
    <div
      ref={mapRef}
      style={{
        position: "absolute",
        top: 0,
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: 0,
      }}
    />
  </>
);

}