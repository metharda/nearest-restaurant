import L from 'leaflet';

const defaultSize: [number, number] = [30, 30];
const defaultAnchor: [number, number] = [15, 30];

export const locationIcon = new L.Icon({
  iconUrl: 'location.png',
  iconSize: defaultSize,
  iconAnchor: defaultAnchor,
  popupAnchor: [0, -30],
});

export const restaurantIcon = new L.Icon({
  iconUrl: 'pin.png',
  iconSize: defaultSize,
  iconAnchor: defaultAnchor,
  popupAnchor: [0, -30],
});