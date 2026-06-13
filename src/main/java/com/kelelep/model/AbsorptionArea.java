package com.kelelep.model;

public class AbsorptionArea extends DrainageInfrastructure {
    private double absorptionRate; // Atribut spesifik kemampuan serap tanah

    public AbsorptionArea(String id, double maxCapacity, double absorptionRate) {
        super(id, maxCapacity);
        this.absorptionRate = absorptionRate;
    }

    // Polymorphism: Cara kerja serapan air yang berbeda dengan objek pipa
    @Override
    public double calculateFlowRate() {
        // Laju air berkurang berdasarkan kemampuan resapan tanahnya
        return this.absorptionRate * 20.0;
    }
}