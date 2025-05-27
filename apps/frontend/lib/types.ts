export interface PathLocation {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
}

export interface PathResponse {
  path: PathLocation[];
  longitude: string;
  latitude: string;
  restaurantId: string;
  requestId: string;
  status: string;
}
