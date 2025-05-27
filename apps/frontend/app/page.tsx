import type { Metadata } from "next";
import Map from '@/components/map';
export const metadata: Metadata = {
  title: "Nearest Restaurants",
  description: "Find the nearest restaurants to your location",
};
export default function Home() {
  return (
    <main className="relative h-screen w-full">
      <Map/>
    </main>
  );
}
