import L from 'leaflet';

const defaultSize: [number, number] = [30, 30];
const defaultAnchor: [number, number] = [15, 30];

export const locationIcon = new L.Icon({
  iconUrl: 'icons/location.png',
  iconSize: defaultSize,
  iconAnchor: defaultAnchor,
  popupAnchor: [0, -30],
});

export const restaurantIcon = new L.Icon({
  iconUrl: 'icons/restaurant.png',
  iconSize: defaultSize,
  iconAnchor: defaultAnchor,
  popupAnchor: [0, -30],
});