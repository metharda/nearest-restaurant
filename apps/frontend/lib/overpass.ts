const OVERPASS_API_URL = 'https://overpass-api.de/api/interpreter';

export async function fetchNearbyRestaurants(lat: number, lon: number, radius = 1000) {
  const query = `
    [out:json][timeout:25];
    node["amenity"="restaurant"](around:${radius},${lat},${lon});
    out;
  `;

  const response = await fetch(OVERPASS_API_URL, {
    method: 'POST',
    body: query,
  });

  if (!response.ok) {
    throw new Error('Overpass API isteği başarısız oldu.');
  }

  const data = await response.json();
  return data.elements;
}