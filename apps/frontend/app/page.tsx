import Map from '@/components/map';
import RestaurantSideMenu from "@/components/sidebar"
export default function Home() {
  return (
    <main className="relative h-screen w-full">
      <Map />
      <RestaurantSideMenu />
    </main>
  );
}
