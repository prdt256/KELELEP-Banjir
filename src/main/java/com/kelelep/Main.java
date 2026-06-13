package com.kelelep;

import com.kelelep.controller.SimulationEngine;
import com.kelelep.model.ConcretePipe;
import com.kelelep.model.AbsorptionArea;

public class Main {
    public static void main(String[] args) {
        SimulationEngine engine = new SimulationEngine();

        // Menginisialisasi objek-objek infrastruktur kota (Polymorphism & Inheritance)
        engine.addInfrastructure(new ConcretePipe("PIPA-GORONG-01", 100.0, 2.5)); // Kapasitas 100 L
        engine.addInfrastructure(new AbsorptionArea("TAMAN-KOTA-A", 150.0, 1.2)); // Kapasitas 150 L

        // Siklus 1: Hujan normal (20 mm/jam) -> Semua infrastruktur harusnya aman
        engine.runSimulationCycle(20.0);

        // Siklus 2: Terjadi hujan ekstrem (120 mm/jam) -> Memaksa terjadinya KelelepOverflowException
        engine.runSimulationCycle(120.0);
    }
}