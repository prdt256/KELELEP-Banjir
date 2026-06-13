package com.kelelep.model;

// Mewarisi Exception (Checked Exception) untuk memisahkan error logika dengan error sistem
public class KelelepOverflowException extends Exception {
    private String infrastructureId;
    private double excessVolume;

    // Constructor untuk merekam data spesifik saat banjir terjadi
    public KelelepOverflowException(String infrastructureId, double excessVolume) {
        super("PERINGATAN: Infrastruktur dengan ID [" + infrastructureId + "] mengalami KELELEP sebesar " + excessVolume + " Liter!");
        this.infrastructureId = infrastructureId;
        this.excessVolume = excessVolume;
    }

    // Getter untuk kebutuhan parsing data ke AI nantinya
    public String getInfrastructureId() { return infrastructureId; }
    public double getExcessVolume() { return excessVolume; }
}