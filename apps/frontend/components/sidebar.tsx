import React from "react";

type Restaurant = {
  name: string;
  latitude: number;
  longitude: number;
  distance: number;
};

export default function Sidebar({ restaurants }: { restaurants: Restaurant[] }) {
  return (
    <div style={{
      position: 'absolute',
      top: 0,
      right: 0,
      width: '300px',
      height: '100vh',
      backgroundColor: '#fff',
      overflowY: 'auto',
      padding: '1rem',
      boxShadow: '-2px 0 5px rgba(0,0,0,0.1)',
      zIndex: 1000
    }}>
      <h2>Yakındaki Restoranlar</h2>
      <ul>
        {restaurants.map((r, i) => (
          <li key={i} style={{ marginBottom: "10px" }}>
            <strong>{r.name}</strong><br />
            {Math.round(r.distance)} metre
          </li>
        ))}
      </ul>
    </div>
  );
}
