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
    console.log('🗺️ Starting path visualization with data:', pathData);
    
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

    console.log('✅ Valid path data confirmed. Nodes:', pathData.path.length);

    const L = (await import('leaflet')).default;
    
    if (pathLayerRef.current) {
      console.log('🧹 Removing existing path layer');
      leafletMapRef.current.removeLayer(pathLayerRef.current);
      pathLayerRef.current = null;
    }

    const pathLayer = L.layerGroup();

    const allCoordinates: [number, number][] = pathData.path.map((node: any) => [node.latitude, node.longitude]);
    
    // Create coordinates for the path line (skip user location for the line)
    const pathCoordinates = pathData.path.slice(1).map((node: any) => [node.latitude, node.longitude]);

    console.log('📍 Path coordinates prepared:', pathCoordinates.length);

    if (pathCoordinates.length > 0) {
      // Create polyline for the path
      const pathLine = L.polyline(pathCoordinates, {
        color: '#4285f4',
        weight: 5,
        opacity: 0.8,
        smoothFactor: 1
      });
      pathLayer.addLayer(pathLine);
      console.log('➰ Path line added');

      // Add blue dots along the path
      const blueDotIcon = createBlueDotIcon(L);
      
      // Add dots for intermediate nodes (skip first and last)
      let dotsAdded = 0;
      for (let i = 1; i < pathData.path.length - 1; i++) {
        // Show every 5th node to avoid cluttering
        if (i % 5 === 0 || i === 1) {
          const node = pathData.path[i];
          const marker = L.marker([node.latitude, node.longitude], {
            icon: blueDotIcon
          });
          pathLayer.addLayer(marker);
          dotsAdded++;
        }
      }
      console.log(`🔵 Added ${dotsAdded} blue dots`);

      // Add destination marker
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
        console.log('🎯 Destination marker added for:', destination.name);
      }

      // Add the path layer to the map
      pathLayer.addTo(leafletMapRef.current);
      pathLayerRef.current = pathLayer;

      // Fit the map to show the entire path
      const bounds = L.latLngBounds(allCoordinates);
      leafletMapRef.current.fitBounds(bounds, { 
        padding: [30, 30],
        maxZoom: 16 
      });

      console.log('🎉 Path visualization completed successfully!');
    }
  };

  // Set up global functions for WebSocket to call
  useEffect(() => {
    console.log('🚀 Setting up map visualization functions');
    
    // Primary function for WebSocket to call
    (window as any).showPathOnMap = (pathData: PathResponse) => {
      console.log('📞 showPathOnMap called from WebSocket with:', pathData);
      visualizePath(pathData);
    };

    // Backup function
    (window as any).handleWebSocketPathData = (pathData: PathResponse) => {
      console.log('📞 handleWebSocketPathData called with:', pathData);
      visualizePath(pathData);
    };

    // Indicate functions are ready
    (window as any).mapVisualizationReady = true;
    console.log('✅ Map visualization functions are ready');

    return () => {
      console.log('🧹 Cleaning up map visualization functions');
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
        console.log('🗺️ Map initialized successfully');
        
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