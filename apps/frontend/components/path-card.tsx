"use client"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Route, Clock, MapPin, X } from "lucide-react"
import { cn } from "@/lib/utils"

interface PathInfo {
  distance: {
    meters: number;
    kilometers: number;
    formatted: string;
  };
  destination: {
    name: string;
    latitude: number;
    longitude: number;
  };
  nodeCount: number;
  estimatedTime: string;
}

interface PathInfoCardProps {
  isVisible: boolean;
  onClose: () => void;
  pathInfo: PathInfo | null;
}

export function PathInfoCard({ isVisible, onClose, pathInfo }: PathInfoCardProps) {
  if (!pathInfo) return null;
  const walkingTimeMinutes = Math.round((pathInfo.distance.kilometers / 5) * 60);
  const timeFormatted = walkingTimeMinutes < 60 
    ? `${walkingTimeMinutes} dk`
    : `${Math.floor(walkingTimeMinutes / 60)}s ${walkingTimeMinutes % 60}dk`;

  return (
    <div
      className={cn(
        "fixed bottom-4 z-30 w-80 transition-transform duration-300 ease-in-out",
        "right-[350px]",
        isVisible ? "translate-y-0" : "translate-y-full"
      )}
    >
      <Card className="shadow-lg">
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg">Yol Tarifi</CardTitle>
            <div className="flex items-center gap-2">
              <Badge variant="secondary">Yürüyüş</Badge>
              <Button
                variant="ghost"
                size="icon"
                className="h-6 w-6"
                onClick={onClose}
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
          </div>
          <CardDescription className="flex items-center gap-1">
            <Route className="h-4 w-4" />
            {pathInfo.destination.name} rotası
          </CardDescription>
        </CardHeader>
        
        <CardContent className="space-y-4">
          <div className="flex items-center gap-2">
            <MapPin className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium">Mesafe</span>
            <Badge variant="outline" className="ml-auto">
              {pathInfo.distance.formatted}
            </Badge>
          </div>
         
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4 text-green-600" />
            <span className="text-sm font-medium">Tahmini Süre</span>
            <Badge variant="outline" className="ml-auto">
              {timeFormatted}
            </Badge>
          </div>

          <div className="space-y-2">
            <h4 className="text-sm font-medium">Rota Detayları</h4>
            <div className="text-sm text-muted-foreground space-y-1">
              <div className="flex justify-between">
                <span>Nokta Sayısı:</span>
                <span className="font-medium">{pathInfo.nodeCount}</span>
              </div>
              <div className="flex justify-between">
                <span>Yürüyüş Hızı:</span>
                <span className="font-medium">5 km/saat</span>
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <h4 className="text-sm font-medium">Koordinatlar</h4>
            <div className="text-sm text-muted-foreground space-y-1">
              <div className="flex justify-between">
                <span>Enlem:</span>
                <span className="font-medium">{pathInfo.destination.latitude.toFixed(6)}</span>
              </div>
              <div className="flex justify-between">
                <span>Boylam:</span>
                <span className="font-medium">{pathInfo.destination.longitude.toFixed(6)}</span>
              </div>
            </div>
          </div>

          <div>
            <Button
              variant="outline"
              className="w-full mt-2"
              onClick={() => {
                console.log('Rotayı Kapat button clicked');
                onClose();
              }}
            >
              Rotayı Kapat
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}