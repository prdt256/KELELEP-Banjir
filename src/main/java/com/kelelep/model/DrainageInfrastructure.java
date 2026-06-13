package com.kelelep.model;

public abstract class DrainageInfrastructure {
    // Encapsulation: Atribut disembunyikan agar tidak bisa diubah sembarangan dari luar
    private String id;
    private double maxCapacity; // dalam satuan Liter
    private double currentVolume; // dalam satuan Liter

    // Constructor
    public DrainageInfrastructure(String id, double maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.currentVolume = 0.0; // Awalnya kosong
    }

    // Abstract method yang wajib diimplementasikan oleh class anak
    public abstract double calculateFlowRate();

    // Modifikasi: Menambahkan 'throws KelelepOverflowException'
    public void addWater(double volume) throws KelelepOverflowException {
        double potentialVolume = this.currentVolume + volume;

        // Jika volume air melebihi kapasitas, lemparkan exception ke sistem utama
        if (potentialVolume > this.maxCapacity) {
            double excess = potentialVolume - this.maxCapacity;
            this.currentVolume = this.maxCapacity; // Tetap set ke kapasitas maksimal
            throw new KelelepOverflowException(this.id, excess);
        }

        this.currentVolume = potentialVolume;
    }

    // Method untuk mengecek apakah saluran ini meluap (KELELEP)
    public boolean isOverflowing() {
        return this.currentVolume > this.maxCapacity;
    }

    // Getter dan Setter standar
    public String getId() { return id; }
    public double getMaxCapacity() { return maxCapacity; }
    public double getCurrentVolume() { return currentVolume; }

    public void setCurrentVolume(double currentVolume) {
        this.currentVolume = currentVolume;
    }


}