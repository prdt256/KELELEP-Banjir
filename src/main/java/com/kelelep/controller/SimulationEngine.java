package com.kelelep.controller;

import com.kelelep.model.DrainageInfrastructure;
import com.kelelep.model.KelelepOverflowException;
import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    // Menggunakan Java Collections (List) untuk menampung infrastruktur kota
    private List<DrainageInfrastructure> urbanNetwork;

    public SimulationEngine() {
        this.urbanNetwork = new ArrayList<>();
    }

    // Method untuk mendaftarkan infrastruktur (pipa, taman, dll) ke dalam sistem kota
    public void addInfrastructure(DrainageInfrastructure infra) {
        this.urbanNetwork.add(infra);
    }

    // Method simulasi utama untuk mengalirkan air hujan ke semua titik infrastruktur
    public void runSimulationCycle(double rainfallIntensity) {
        System.out.println("=== MEMULAI SIMULASI DENGAN INTENSITAS HUJAN: " + rainfallIntensity + " mm/jam ===");

        for (DrainageInfrastructure infra : urbanNetwork) {
            try {
                // Kurangi air terlebih dahulu berdasarkan laju aliran keluar (Polymorphism)
                double discharged = infra.calculateFlowRate();
                double current = infra.getCurrentVolume();
                infra.setCurrentVolume(Math.max(0, current - discharged));

                // Masukkan air hujan baru ke dalam objek infrastruktur
                infra.addWater(rainfallIntensity);

                System.out.println("ID [" + infra.getId() + "] - Kondisi: Aman | Volume Saat Ini: " + infra.getCurrentVolume() + " L");
            } catch (KelelepOverflowException e) {
                // Blok catch menangkap objek yang meluap
                System.out.println(e.getMessage());
                System.out.println("-> [SISTEM]: Meneruskan data luapan ke modul AI untuk analisis lebih lanjut...\n");
                // Catatan: Di sinilah porsi 10% AI milikmu nanti akan dipanggil!
            }
        }
        System.out.println("=== SIMULASI SIKLUS INI SELESAI ===\n");
    }
}