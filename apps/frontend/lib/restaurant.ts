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
export async function getPathtoRestaurant(restaurantId: string, lat: number, lon: number): Promise<PathResponse | null> {
  return new Promise((resolve, reject) => {
    try {
      const stompClient = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
        debug: () => { },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      });

      // Track if we've already received a response for this restaurantId
      let responseReceived = false;
      
      // Set a timeout to prevent waiting forever
      const timeout = setTimeout(() => {
        if (!responseReceived) {
          console.error('Path request timed out after 30 seconds');
          stompClient.deactivate();
          reject(new Error('Path request timed out'));
        }
      }, 30000); // 30 seconds timeout
      
      stompClient.onConnect = () => {
        console.log('STOMP connection established');
        
        // Also subscribe to errors
        const errorSubscription = stompClient.subscribe('/topic/errors', (message) => {
          try {
            const errorData = JSON.parse(message.body);
            if (errorData.restaurantId === restaurantId) {
              console.error('Path calculation error:', errorData);
              if (!responseReceived) {
                responseReceived = true;
                clearTimeout(timeout);
                errorSubscription.unsubscribe();
                stompClient.deactivate();
                reject(new Error(`Path calculation error: ${errorData.message || 'Unknown error'}`));
              }
            }
          } catch (error) {
            console.error('Error parsing error message:', error);
          }
        });

        const subscription = stompClient.subscribe('/topic/paths', (message) => {
          try {
            const pathData = JSON.parse(message.body) as PathResponse;
            
            // Only process if it's for our requested restaurant and we haven't processed a response yet
            if (pathData.restaurantId === restaurantId && !responseReceived) {
              console.log('Received path data for restaurant:', pathData);
              responseReceived = true;
              clearTimeout(timeout);
              
              // Clean up duplicates from the path if needed
              if (pathData.path && Array.isArray(pathData.path)) {
                // Remove consecutive duplicates from path
                const cleanPath = pathData.path.filter((node, index, array) => {
                  if (index === 0) return true;
                  const prevNode = array[index - 1];
                  return !(node.id === prevNode.id && 
                          node.latitude === prevNode.latitude && 
                          node.longitude === prevNode.longitude);
                });
                
                pathData.path = cleanPath;
                console.log(`Path optimized: Reduced from ${pathData.path.length} to ${cleanPath.length} nodes`);
              }
              
              // Unsubscribe after receiving the relevant path data
              subscription.unsubscribe();
              errorSubscription.unsubscribe();
              stompClient.deactivate();
              console.log('WebSocket connection closed after receiving path');
              
              resolve(pathData);
            }
          } catch (jsonError) {
            console.error('Could not parse path data as JSON:', jsonError);
            console.log('Raw message body:', message.body);
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
            if (!responseReceived) {
              responseReceived = true;
              clearTimeout(timeout);
              stompClient.deactivate();
              reject(error);
            }
          });
      };

      stompClient.onStompError = (frame) => {
        console.error('STOMP protocol error:', frame.headers['message']);
        if (!responseReceived) {
          responseReceived = true;
          clearTimeout(timeout);
          stompClient.deactivate();
          reject(new Error(`STOMP protocol error: ${frame.headers['message']}`));
        }
      };

      stompClient.activate();

    } catch (error) {
      console.error('Error in path request:', error);
      reject(error);
    }
  });
}