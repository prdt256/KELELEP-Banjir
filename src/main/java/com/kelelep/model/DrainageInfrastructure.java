package com.kelelep.model;

import com.kelelep.exception.KelelepOverflowException;

/**
 * Kelas induk abstrak yang mewariskan sifat dasar infrastruktur drainase.
 */
public abstract class DrainageInfrastructure implements Simulatable {
    protected String id;
    protected String type;
    protected double maxCapacity;
    protected double currentVolume;

    public DrainageInfrastructure(String id, String type, double maxCapacity) {
        this.id = id;
        this.type = type;
        this.maxCapacity = maxCapacity;
        this.currentVolume = 0.0;
    }

    /**
     * Menambahkan volume air ke dalam infrastruktur.
     * Jika total air melebihi kapasitas maksimum, maka kelebihan air akan meluap (overflow),
     * dan metode ini akan melemparkan pengecualian KelelepOverflowException.
     *
     * @param volumeAdded Jumlah air (m³) yang masuk ke dalam infrastruktur pada siklus ini.
     * @throws KelelepOverflowException jika volume air melebihi batas kapasitas maksimal.
     */
    public void addWater(double volumeAdded) throws KelelepOverflowException {
        this.currentVolume += volumeAdded;
        if (this.currentVolume > this.maxCapacity) {
            double excess = this.currentVolume - this.maxCapacity;
            this.currentVolume = this.maxCapacity; // Membatasi volume agar tidak melebihi kapasitas
            throw new KelelepOverflowException(this.id, excess);
        }
    }

    /**
     * Mengembalikan volume air saat ini ke titik nol (0.0).
     * Biasanya dipanggil saat simulasi dihentikan dan sistem direset.
     */
    public void resetVolume() {
        this.currentVolume = 0.0;
    }

    /**
     * Menghitung tingkat aliran air atau daya serap (flow rate).
     * Metode abstrak ini wajib diimplementasikan oleh kelas turunan sesuai karakteristik infrastrukturnya.
     *
     * @return Nilai aliran/serapan air spesifik per infrastruktur.
     */
    public abstract double calculateFlowRate();

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public double getMaxCapacity() { return maxCapacity; }
    public double getCurrentVolume() { return currentVolume; }
}