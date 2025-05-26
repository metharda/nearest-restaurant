import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { SidebarApp }from '@/components/sidebar-app';
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"
import "./globals.css";
import 'leaflet/dist/leaflet.css';

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Nearest Restaurants",
  description: "Find the nearest restaurants to your location",
};

export default function Layout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className={`${geistSans.variable} ${geistMono.variable}`}>
      <body className="bg-white text-default antialiased">
        <SidebarProvider>
          <SidebarTrigger />
          <SidebarApp />
          {children}
        </SidebarProvider>
      </body>
    </html>
  );
}
