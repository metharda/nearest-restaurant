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
export async function routeRestaurant(restaurantId: string) {
 const response = await MakeRequest(
   `/api/restaurant/${restaurantId}`,
   'GET',
   null,
   {}
 );
 
 if (response instanceof Error) {
   throw new Error(`Route restaurant failed: ${response.message}`);
 }
 return response;
}
