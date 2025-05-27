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
          maximumAge: 300000 // 5 minutes
        }
      );
    });
  };