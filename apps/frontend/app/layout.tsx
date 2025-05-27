'use client';
import React from "react";
import { Geist, Geist_Mono } from "next/font/google";
import { SidebarApp } from '@/components/sidebar-app';
import { Inter } from "next/font/google";
import { AccountBubble } from "@/components/account-bubble"
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"
import { InfoCard } from "@/components/info-card";
import { InfoCardProvider, useInfoCard } from "@/components/info-card-context";
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

const inter = Inter({ subsets: ["latin"] });

function InfoCardWrapper() {
  const { selectedId, setSelectedId } = useInfoCard();
  return selectedId !== null ? (
    <InfoCard
      isOpen={true}
      restaurantId={selectedId}
      onClose={() => setSelectedId(null)}
    />
  ) : null;
}

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <SidebarProvider defaultOpen={false}>
          <InfoCardProvider>
            <SidebarApp />
            <SidebarInset>
              <header className="absolute top-0 left-0 z-10 flex h-16 items-center px-4 bg-transparent">
                <SidebarTrigger className="-ml-1" />
              </header>

              <div className="absolute top-4 right-4 z-20">
                <AccountBubble />
              </div>

              <div className="relative h-screen">{children}</div>
              <InfoCardWrapper />
            </SidebarInset>
          </InfoCardProvider>
        </SidebarProvider>
      </body>
    </html>
  );
}