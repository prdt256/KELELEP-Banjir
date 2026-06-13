package com.kelelep.model;

public class ConcretePipe extends DrainageInfrastructure {
    private double diameter; // Atribut spesifik pipa beton

    public ConcretePipe(String id, double maxCapacity, double diameter) {
        super(id, maxCapacity); // Memanggil constructor parent class
        this.diameter = diameter;
    }

    // Polymorphism: Implementasi cara hitung aliran air khusus pipa beton
    @Override
    public double calculateFlowRate() {
        // Simulasi rumus sederhana: makin besar diameter, laju air keluar makin cepat
        return this.diameter * 15.5;
    }
}