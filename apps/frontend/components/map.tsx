'use client';

import 'leaflet/dist/leaflet.css';
import { useEffect, useRef, useState } from 'react';
import { fetchRestaurants } from '@/lib/restaurant';
import { locationIcon, restaurantIcon } from '@/lib/icons';
import { getCurrentLocation } from '@/lib/location';
import { useInfoCard } from '@/components/info-card-context';
import { PathResponse } from '@/lib/types';

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
  const pathLayerRef = useRef<any>(null);
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [userLocation, setUserLocation] = useState<{ latitude: number; longitude: number } | null>(null);
  const { setSelectedId, focusRestaurant } = useInfoCard();

  const createBlueDotIcon = (L: any) => {
    return L.divIcon({
      className: 'custom-blue-dot',
      html: `<div style="
        width: 10px;
        height: 10px;
        background-color: #4285f4;
        border: 2px solid white;
        border-radius: 50%;
        box-shadow: 0 2px 4px rgba(0,0,0,0.3);
      "></div>`,
      iconSize: [14, 14],
      iconAnchor: [7, 7]
    });
  };

  const visualizePath = async (pathData: PathResponse) => {    
    
    if (!leafletMapRef.current) {
      console.error('❌ No map reference available');
      return;
    }

    if (!pathData || !pathData.path || !Array.isArray(pathData.path) || pathData.path.length === 0) {
      console.error('❌ Invalid path data:', {
        hasPathData: !!pathData,
        hasPath: !!pathData?.path,
        isArray: Array.isArray(pathData?.path),
        length: pathData?.path?.length
      });
      return;
    }
    

    const L = (await import('leaflet')).default;
    
    if (pathLayerRef.current) {      
      leafletMapRef.current.removeLayer(pathLayerRef.current);
      pathLayerRef.current = null;
    }

    const pathLayer = L.layerGroup();
    const allCoordinates: [number, number][] = pathData.path.map((node: any) => [node.latitude, node.longitude]);
    const pathCoordinates = pathData.path.slice(1).map((node: any) => [node.latitude, node.longitude]);
    

    if (pathCoordinates.length > 0) {
      // Create polyline for the path
      const pathLine = L.polyline(pathCoordinates, {
        color: '#4285f4',
        weight: 5,
        opacity: 0.8,
        smoothFactor: 1
      });
      pathLayer.addLayer(pathLine);      
      const blueDotIcon = createBlueDotIcon(L);
      let dotsAdded = 0;
      for (let i = 1; i < pathData.path.length - 1; i++) {
        if (i % 5 === 0 || i === 1) {
          const node = pathData.path[i];
          const marker = L.marker([node.latitude, node.longitude], {
            icon: blueDotIcon
          });
          pathLayer.addLayer(marker);
          dotsAdded++;
        }
      }     
      const destination = pathData.path[pathData.path.length - 1];
      if (destination) {
        const destMarker = L.marker([destination.latitude, destination.longitude], {
          icon: restaurantIcon,
          zIndexOffset: 1000
        }).bindPopup(`
          <div style="text-align: center;">
            <h3 style="margin: 0 0 8px 0; font-weight: bold; color: #333;">${destination.name}</h3>
            <p style="margin: 0; font-size: 12px; color: #666;">📍 Destination</p>
          </div>
        `);
        pathLayer.addLayer(destMarker);        
      }

      pathLayer.addTo(leafletMapRef.current);
      pathLayerRef.current = pathLayer;
      const bounds = L.latLngBounds(allCoordinates);
      leafletMapRef.current.fitBounds(bounds, { 
        padding: [30, 30],
        maxZoom: 16 
      });
      
    }
  };

  useEffect(() => {    
    (window as any).showPathOnMap = (pathData: PathResponse) => {      
      visualizePath(pathData);
    };
    (window as any).handleWebSocketPathData = (pathData: PathResponse) => {      
      visualizePath(pathData);
    };
    (window as any).mapVisualizationReady = true;    
    return () => {      
      delete (window as any).showPathOnMap;
      delete (window as any).handleWebSocketPathData;
      delete (window as any).mapVisualizationReady;
    };
  }, []);

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
          .bindPopup('Konumunuz')
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
            setSelectedId(restaurant.id);
          });
        });
        leafletMapRef.current = map;        
        
      } catch (error) {
        console.error('❌ Failed to initialize map:', error);
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

  useEffect(() => {
    if (focusRestaurant && leafletMapRef.current) {
      leafletMapRef.current.setView(
        [focusRestaurant.latitude, focusRestaurant.longitude],
        17,
        { animate: true }
      );
      setSelectedId(focusRestaurant.id);
    }
  }, [focusRestaurant, setSelectedId]);

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