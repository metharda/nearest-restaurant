export const getCurrentLocation = () => {
  return new Promise<{ latitude: number; longitude: number }>((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation is not supported by this browser'));
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        resolve({ latitude, longitude });
      },
      (error) => {
        switch (error.code) {
          case error.PERMISSION_DENIED:
            reject(new Error('Konum erişimi reddedildi'));
            break;
          case error.POSITION_UNAVAILABLE:
            reject(new Error('Konum bilgisi kullanılamıyor'));
            break;
          case error.TIMEOUT:
            reject(new Error('Konum alma işlemi zaman aşımına uğradı'));
            break;
          default:
            reject(new Error('Bilinmeyen bir hata oluştu'));
            break;
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000
      }
    );
  });
};

export async function calcDistance(lat1: number, lon1: number): Promise<number> {
  const toRad = (x: number) => (x * Math.PI) / 180;
  const R = 6371e3;
  
  return getCurrentLocation().then(({ latitude: lat2, longitude: lon2 }) => {
    const φ1 = toRad(lat1);
    const φ2 = toRad(lat2);
    const Δφ = toRad(lat2 - lat1);
    const Δλ = toRad(lon2 - lon1);

    const a =
      Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
      Math.cos(φ1) * Math.cos(φ2) *
      Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const result = (R * c) / 1000;
    return result;
  }).catch((error) => {
    console.error('Error fetching current location:', error);
    throw error;
  });
}