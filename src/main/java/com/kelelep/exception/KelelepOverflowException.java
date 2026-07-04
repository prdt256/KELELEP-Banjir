package com.kelelep.exception;

/**
 * Custom Checked Exception untuk menangani luapan air pada infrastruktur.
 * Exception ini nantinya akan ditangkap oleh UI untuk mengubah warna baris tabel jadi merah
 * dan memicu permintaan ke API Gemini AI.
 */
public class KelelepOverflowException extends Exception {
    private final String infrastructureId;
    private final double excessVolume;

    public KelelepOverflowException(String infrastructureId, double excessVolume) {
        super("BAHAYA! Infrastruktur " + infrastructureId + " meluap/Kelelep! Kelebihan air: " + excessVolume + " m3");
        this.infrastructureId = infrastructureId;
        this.excessVolume = excessVolume;
    }

    public String getInfrastructureId() {
        return infrastructureId;
    }

    public double getExcessVolume() {
        return excessVolume;
    }
}
