package com.kelelep.model;

import com.kelelep.exception.KelelepOverflowException;

/**
 * Sub-class yang merepresentasikan Gorong-gorong / Pipa Beton.
 */
public class ConcretePipe extends DrainageInfrastructure {
    private double pipeDiameter;

    public ConcretePipe(String id, double maxCapacity, double pipeDiameter) {
        super(id, "Concrete Pipe", maxCapacity);
        this.pipeDiameter = pipeDiameter;
    }

    /**
     * Menghitung tingkat aliran keluar (flow rate) air.
     * Penerapan polimorfisme: laju alir pipa beton bergantung secara proporsional terhadap diameternya.
     *
     * @return Tingkat aliran keluar air.
     */
    @Override
    public double calculateFlowRate() {
        return pipeDiameter * 2.5;
    }

    /**
     * Menyimulasikan perubahan volume air di dalam pipa untuk setiap siklus (tick) waktu.
     * 
     * @param rainfallIntensity Intensitas curah hujan (mempengaruhi debit masuk).
     * @throws KelelepOverflowException jika penambahan air menyebabkan pipa meluap.
     */
    @Override
    public void processTick(double rainfallIntensity) throws KelelepOverflowException {
        // Debit air yang masuk diasumsikan sebanding dengan intensitas hujan
        double waterInflow = rainfallIntensity * 1.5; 
        
        // Debit air yang keluar didasarkan pada laju alir pipa
        double waterOutflow = calculateFlowRate();

        double netVolumeChange = waterInflow - waterOutflow;
        
        if (netVolumeChange > 0) {
            // Jika debit masuk lebih besar, tambahkan volume air (berpotensi meluap)
            addWater(netVolumeChange);
        } else {
            // Jika debit keluar lebih besar, kurangi volume air (surut), batas minimum adalah 0
            this.currentVolume = Math.max(0, this.currentVolume + netVolumeChange);
        }
    }
}