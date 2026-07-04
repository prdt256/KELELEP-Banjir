package com.kelelep.service;

import com.kelelep.model.DrainageInfrastructure;
import com.kelelep.model.ConcretePipe;
import com.kelelep.model.AbsorptionArea;
import com.kelelep.exception.KelelepOverflowException;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    private List<DrainageInfrastructure> urbanNetwork;

    public SimulationEngine() {
        this.urbanNetwork = new ArrayList<>();
    }

    public void loadDummyData() {
        urbanNetwork.add(new ConcretePipe("CH-01", 1500, 15.5));
        urbanNetwork.add(new ConcretePipe("CH-02", 800, 10.0));
        urbanNetwork.add(new AbsorptionArea("RES-01", 10000, 85.0));
        System.out.println("[SYS] Data dimuat.");
    }

    /**
     * Menjalankan satu siklus (tick) simulasi berdasarkan intensitas hujan saat ini.
     * Metode ini akan memeriksa dan memproses semua aset infrastruktur secara berurutan.
     * Jika ada aset yang meluap (overflow), peringatan (exception) akan ditangkap
     * dan dikumpulkan, tanpa menghentikan simulasi untuk aset yang lainnya.
     *
     * @param currentRainfallIntensity Intensitas curah hujan (mm/jam)
     * @return Daftar kejadian luapan (KelelepOverflowException). Jika list kosong, berarti sistem aman.
     */
    public List<KelelepOverflowException> runSimulationCycle(double currentRainfallIntensity) {
        List<KelelepOverflowException> overflows = new ArrayList<>();
        for (DrainageInfrastructure asset : urbanNetwork) {
            try {
                asset.processTick(currentRainfallIntensity);
            } catch (KelelepOverflowException e) {
                overflows.add(e);
            }
        }
        return overflows;
    }

    public List<DrainageInfrastructure> getUrbanNetwork() {
        return urbanNetwork;
    }
}
