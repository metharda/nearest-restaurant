package com.project.utils;

import com.project.datastructures.Graph;
import com.project.datastructures.HashMap;
import com.project.datastructures.Linkedlist;
import com.project.datastructures.Node;
import com.project.entity.Location;

public class AStar {
    
    // A* algoritması implementasyonu
    public static Linkedlist<Location> findPath(Graph graph, Location start, Location goal) {
        if (start == null || goal == null) {
            return null;
        }
        
        // Açık ve kapalı setleri (ziyaret edilecek ve ziyaret edilmiş düğümler)
        Linkedlist<Location> openSet = new Linkedlist<>();
        Linkedlist<Location> closedSet = new Linkedlist<>();
        
        // Başlangıç noktasını açık listeye ekle
        openSet.add(start);
        
        // Her düğüm için g-skoru (başlangıçtan o düğüme olan maliyet)
        HashMap<Location, Double> gScore = new HashMap<>();
        
        // Her düğüm için f-skoru (g-skoru + heuristik)
        HashMap<Location, Double> fScore = new HashMap<>();
        
        // Her düğüm için hangi düğümden geldiğini sakla (geri izleme için)
        HashMap<Location, Location> cameFrom = new HashMap<>();
        
        // Tüm düğümler için başlangıç g-skoru sonsuz olarak ayarla
        // Start düğümü için g-skoru 0
        gScore.put(start, 0.0);
        
        // Start düğümünün f-skoru sadece heuristik değeridir
        fScore.put(start, heuristic(start, goal));
        
        // Açık liste boş olmadığı sürece devam et
        while (openSet.length() > 0) {
            // En düşük f-skora sahip düğümü bul
            Location current = getLowestFScore(openSet, fScore);
            
            // Eğer hedef düğüme ulaştıysak, yolu geri izleyerek döndür
            if (current.equals(goal)) {
                return reconstructPath(cameFrom, current);
            }
            
            // Mevcut düğümü açık listeden çıkar ve kapalı listeye ekle
            openSet.removebyvalue(current);
            closedSet.add(current);
            
            // Komşu düğümleri al
            Linkedlist<Location> neighbors = graph.getMap().get(current);
            if (neighbors == null) {
                continue;
            }
            
            Node<Location> neighborNode = neighbors.head;
            while (neighborNode != null) {
                Location neighbor = neighborNode.value;
                
                // Eğer komşu düğüm zaten ziyaret edildiyse atla
                if (closedSet.count(neighbor) > 0) {
                    neighborNode = neighborNode.next;
                    continue;
                }
                
                // Mevcut düğüm üzerinden komşuya olan mesafeyi hesapla
                double tentativeGScore = getOrDefault(gScore, current, Double.MAX_VALUE) +
                    calculateDistance(current, neighbor);
                
                // Eğer komşu daha önce ziyaret edilmemişse açık listeye ekle
                if (!(openSet.count(neighbor) > 0)) {
                    openSet.add(neighbor);
                }
                // Eğer yeni yol daha iyiyse g-skoru güncelle
                else if (tentativeGScore >= getOrDefault(gScore, neighbor, Double.MAX_VALUE)) {
                    neighborNode = neighborNode.next;
                    continue;
                }
                
                // Bu en iyi yolu kaydet
                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentativeGScore);
                fScore.put(neighbor, tentativeGScore + heuristic(neighbor, goal));
                
                neighborNode = neighborNode.next;
            }
        }
        
        // Eğer buraya kadar geldiyse, bir yol bulunamadı
        return null;
    }
    
    // Bir Linkedlist'te en düşük f-skora sahip düğümü bul
    private static Location getLowestFScore(Linkedlist<Location> openSet, HashMap<Location, Double> fScore) {
        Location lowest = null;
        double lowestScore = Double.MAX_VALUE;
        
        Node<Location> current = openSet.head;
        while (current != null) {
            double score = getOrDefault(fScore, current.value, Double.MAX_VALUE);
            if (score < lowestScore) {
                lowestScore = score;
                lowest = current.value;
            }
            current = current.next;
        }
        
        return lowest;
    }
    
    // HashMap'ten bir değer al, yoksa varsayılan değeri döndür
    private static double getOrDefault(HashMap<Location, Double> map, Location key, double defaultValue) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return defaultValue;
    }
    
    // Heuristik fonksiyonu (iki konum arasındaki basit mesafe)
    private static double heuristic(Location a, Location b) {
        return Graph.simpleDistance(
            a.getLatitude(), a.getLongitude(),
            b.getLatitude(), b.getLongitude()
        );
    }
    
    // İki konum arasındaki gerçek mesafeyi hesapla
    private static double calculateDistance(Location a, Location b) {
        return Graph.simpleDistance(
            a.getLatitude(), a.getLongitude(),
            b.getLatitude(), b.getLongitude()
        );
    }
    
    // Bir hedef düğüme ulaşıldığında yolu geri izleyerek oluştur
    private static Linkedlist<Location> reconstructPath(HashMap<Location, Location> cameFrom, Location current) {
        Linkedlist<Location> totalPath = new Linkedlist<>();
        totalPath.add(current);
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.addFirst(current); // Yolu başa doğru oluştur
        }
        
        return totalPath;
    }
}
