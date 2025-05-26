import React, { useState } from "react";

type Restaurant = {
  name: string;
  latitude: number;
  longitude: number;
  distance: number;
};

type SidebarProps = {
  restaurants: Restaurant[];
  onSelectRestaurant: (r: Restaurant) => void;
  selectedRestaurant?: Restaurant;
};

export default function Sidebar({
  restaurants,
  onSelectRestaurant,
  selectedRestaurant
}: SidebarProps) {
  const [open, setOpen] = useState(false);

  return (
    <>
      <button
        onClick={() => setOpen(!open)}
        style={{
          position: "absolute",
          top: "20px",
          right: open ? "310px" : "10px",
          zIndex: 1100,
          padding: "8px 12px",
          backgroundColor: "#333",
          color: "#fff",
          border: "none",
          borderRadius: "4px",
          cursor: "pointer",
          transition: "right 0.3s ease"
        }}
      >
        {open ? "Kapat" : "📍 Restoranlar"}
      </button>

      <div
        style={{
          position: "absolute",
          top: 0,
          right: open ? 0 : "-320px",
          width: "300px",
          height: "100vh",
          backgroundColor: "#fff",
          overflowY: "auto",
          padding: "1rem",
          boxShadow: "-2px 0 5px rgba(0,0,0,0.1)",
          zIndex: 1000,
          transition: "right 0.3s ease"
        }}
      >
        <h2>Yakındaki Restoranlar</h2>
        <ul style={{ listStyle: "none", padding: 0 }}>
          {restaurants.map((r, i) => (
            <li
              key={i}
              onClick={() => onSelectRestaurant(r)}
              style={{
                marginBottom: "10px",
                cursor: "pointer",
                backgroundColor:
                  selectedRestaurant?.name === r.name ? "#eee" : "transparent",
                padding: "5px",
                borderRadius: "5px"
              }}
            >
              <strong>{r.name}</strong>
              <br />
              {Math.round(r.distance)} metre
            </li>
          ))}
        </ul>
      </div>
    </>
  );
}
