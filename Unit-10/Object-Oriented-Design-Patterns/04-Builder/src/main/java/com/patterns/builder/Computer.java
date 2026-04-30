package com.patterns.builder;

/**
 * The product — a custom-built computer.
 *
 * <p>A {@code Computer} has many optional components. Constructing it via
 * a telescoping constructor (one overload per combination of parameters) would
 * require dozens of constructors and would be impossible to read at the call
 * site. The Builder solves this by assembling the object step by step.
 */
public class Computer {

    private final String cpu;
    private final int    ramGb;
    private final int    storageGb;
    private final String gpu;
    private final String caseStyle;
    private final boolean hasWifi;
    private final boolean hasBluetooth;

    /** Private — only {@link ComputerBuilder} may call this. */
    Computer(ComputerBuilder b) {
        this.cpu          = b.cpu;
        this.ramGb        = b.ramGb;
        this.storageGb    = b.storageGb;
        this.gpu          = b.gpu;
        this.caseStyle    = b.caseStyle;
        this.hasWifi      = b.hasWifi;
        this.hasBluetooth = b.hasBluetooth;
    }

    /** @return the CPU model */
    public String getCpu()       { return cpu; }

    /** @return RAM in gigabytes */
    public int getRamGb()        { return ramGb; }

    /** @return storage in gigabytes */
    public int getStorageGb()    { return storageGb; }

    /** @return GPU model, or "Integrated" */
    public String getGpu()       { return gpu; }

    /** @return the case style description */
    public String getCaseStyle() { return caseStyle; }

    /** @return true if a WiFi card is installed */
    public boolean hasWifi()      { return hasWifi; }

    /** @return true if Bluetooth is enabled */
    public boolean hasBluetooth() { return hasBluetooth; }

    @Override
    public String toString() {
        return String.format(
                "Computer{cpu='%s', ram=%dGB, storage=%dGB, gpu='%s', case='%s', wifi=%b, bt=%b}",
                cpu, ramGb, storageGb, gpu, caseStyle, hasWifi, hasBluetooth);
    }
}
