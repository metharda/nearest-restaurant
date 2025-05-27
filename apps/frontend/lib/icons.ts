let locationIcon: L.Icon | undefined;
let restaurantIcon: L.Icon | undefined;
if (typeof window !== 'undefined') {
  const L = (await import('leaflet')).default;
  const defaultSize: [number, number] = [30, 30];
  const defaultAnchor: [number, number] = [15, 30];

  locationIcon = new L.Icon({
    iconUrl: 'location.png',
    iconSize: defaultSize,
    iconAnchor: defaultAnchor,
    popupAnchor: [0, -30],
  });

  restaurantIcon = new L.Icon({
    iconUrl: 'pin.png',
    iconSize: defaultSize,
    iconAnchor: defaultAnchor,
    popupAnchor: [0, -30],
  });
}
export { locationIcon, restaurantIcon };