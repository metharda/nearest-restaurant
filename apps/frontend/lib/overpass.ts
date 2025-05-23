export async function fetchNearbyRestaurants(lat: number, lon: number) {
  const response = await fetch("http://localhost:8081/api/path/nearest", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ latitude: lat, longitude: lon }),
  });

  if (!response.ok) {
    throw new Error("Backend API isteği başarısız oldu.");
  }

  const data = await response.json();
  return data;
}
