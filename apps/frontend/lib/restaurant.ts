import { MakeRequest } from "@/lib/request";
import SockJS from "sockjs-client"
import { Client } from "@stomp/stompjs"
import { PathResponse } from "@/lib/types"

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
  try {
    const stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
      debug: () => { },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    stompClient.onConnect = () => {
      console.log('STOMP connection established');

      const subscription = stompClient.subscribe('/topic/paths', (message) => {
        try {
          try {
            const pathData = JSON.parse(message.body) as PathResponse;
            console.log('Received path data:', pathData);
            return pathData;
          } catch (jsonError) {
            console.error('Could not parse path data as JSON:', jsonError);
            console.log('Raw message body:', message.body);
          }

          subscription.unsubscribe();
          stompClient.deactivate();
          console.log('WebSocket connection closed');
        } catch (error: any) {
          console.error('Error handling WebSocket response:', error);
        }
      });

      MakeRequest(
        '/getPath',
        'GET',
        null,
        {
          'Restaurant-Id': restaurantId,
          'User-Latitude': lat.toString(),
          'User-Longitude': lon.toString(),
        }
      )
        .then(response => {
          if (response instanceof Error) {
            throw response;
          }
          console.log('Path request sent successfully:', response);
        })
        .catch((error: any) => {
          console.error('Error requesting path:', error);
          stompClient.deactivate();
        });
    };

    stompClient.onStompError = (frame) => {
      console.error('STOMP protocol error:', frame.headers['message']);
      stompClient.deactivate();
    };

    stompClient.activate();

  } catch (error) {
    console.error('Error in path request:', error);
  }
}