package com.kelelep.model;

import com.kelelep.exception.KelelepOverflowException;

/**
 * Interface untuk memastikan setiap infrastruktur bisa merespon siklus waktu simulasi (tick).
 */
public interface Simulatable {
    void processTick(double rainfallIntensity) throws KelelepOverflowException;
}