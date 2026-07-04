package com.kelelep.model;

import com.kelelep.exception.KelelepOverflowException;

/**
 * Sub-class yang merepresentasikan Area Resapan / Taman Kota.
 */
public class AbsorptionArea extends DrainageInfrastructure {
    private double absorptionRate;

    public AbsorptionArea(String id, double maxCapacity, double absorptionRate) {
        super(id, "Absorption Area", maxCapacity);
        this.absorptionRate = absorptionRate;
    }

    /**
     * Menghitung tingkat penyerapan air ke dalam tanah (flow rate).
     * Penerapan polimorfisme: daya resap ditentukan oleh tingkat serapan tanah alami.
     *
     * @return Tingkat air yang berhasil diserap tanah.
     */
    @Override
    public double calculateFlowRate() {
        return absorptionRate * 1.2;
    }

    /**
     * Menyimulasikan perubahan volume genangan air di area resapan untuk setiap siklus waktu.
     * 
     * @param rainfallIntensity Intensitas curah hujan (mempengaruhi seberapa banyak air yang terkumpul).
     * @throws KelelepOverflowException jika volume genangan melebihi kapasitas maksimal area resapan.
     */
    @Override
    public void processTick(double rainfallIntensity) throws KelelepOverflowException {
        // Debit air yang masuk diasumsikan lebih besar karena area resapan menampung air dari berbagai arah
        double waterInflow = rainfallIntensity * 3.0; 
        
        // Debit air yang berhasil diserap oleh tanah
        double waterAbsorbed = calculateFlowRate();

        double netVolumeChange = waterInflow - waterAbsorbed;
        
        if (netVolumeChange > 0) {
            // Jika air yang masuk lebih banyak dari yang diserap, genangan bertambah (berpotensi banjir)
            addWater(netVolumeChange);
        } else {
            // Jika tanah berhasil menyerap lebih cepat, genangan air menyusut
            this.currentVolume = Math.max(0, this.currentVolume + netVolumeChange);
        }
    }
}