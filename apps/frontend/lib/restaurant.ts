import { MakeRequest } from "@/lib/request";

export async function fetchRestaurants(radius: number, lat: number, lon: number) {
  const response = await MakeRequest(
    '/getRestaurants',
    'GET',
    null,
    {
      'radius': radius.toString(),
      'latitude': lat.toString(), 
      'longitude': lon.toString()
    }
  );
  
  if (response instanceof Error) {
    throw new Error(`Fetch restaurants failed: ${response.message}`);
  }
  return response;
}
export async function getPathtoRestaurant(restaurantId: string, lat: number, lon: number) {
  const response = await MakeRequest(
    '/getPath',
    'GET',
    null,
    {
      'restaurantId': restaurantId,
      'latitude': lat.toString(),
      'longitude': lon.toString()
    }
  );
  
  if (response instanceof Error) {
    throw new Error(`Get path to restaurant failed: ${response.message}`);
  }
  return response;
}
