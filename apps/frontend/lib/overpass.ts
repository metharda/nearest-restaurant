const OVERPASS_API_URL = 'https://overpass-api.de/api/interpreter';

export async function getRestaurantInfo(id: number) {
  const query = `
    [out:json][timeout:25];
    node(${id});
    out body;
  `;

  const response = await fetch(OVERPASS_API_URL, {
    method: 'POST',
    body: query,
  });

  if (!response.ok) {
    throw new Error('Overpass API isteği başarısız oldu.');
  }

  const data = await response.json();
  const element = data.elements[0];

  if (!element || !element.tags) {
    throw new Error('Restoran bilgisi bulunamadı.');
  }

  const tags = element.tags;

  return {
    id: element.id,
    name: tags.name || null,
    adress: {
      city: tags['addr:tags'] || null,
      district: tags['addr:district'] || null,
      neigberhood: tags['addr:neighborhood'] || null,
      street: tags['addr:street'] || null,
    },
    openingHours: tags.opening_hours || null,
    stars: tags.stars || null,
    description: tags.description || null,
    tags: {
      amenity: tags['amenity'] || null,
      cuisine: tags['cuisine'] || null,
       // return all tags as fallback
    },
  };
}