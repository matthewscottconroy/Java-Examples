package com.patterns.builder;

/**
 * Builder — assembles a {@link Computer} component by component.
 *
 * <p>Each setter returns {@code this} so calls can be chained fluently.
 * The builder validates that at least a CPU and RAM are specified before
 * allowing {@link #build()} to proceed.
 *
 * <p>Subclasses ({@link GamingPCBuilder}, {@link OfficePCBuilder}) pre-populate
 * sensible defaults for their use case, so the caller only needs to override
 * what differs.
 */
public class ComputerBuilder {

    // Package-private so Computer's constructor can read them
    String  cpu          = "Generic CPU";
    int     ramGb        = 8;
    int     storageGb    = 256;
    String  gpu          = "Integrated";
    String  caseStyle    = "Standard mid-tower";
    boolean hasWifi      = false;
    boolean hasBluetooth = false;

    /**
     * Sets the CPU model.
     * @param cpu CPU model string
     * @return this builder
     */
    public ComputerBuilder cpu(String cpu) {
        this.cpu = cpu;
        return this;
    }

    /**
     * Sets the amount of RAM.
     * @param gb RAM in gigabytes
     * @return this builder
     */
    public ComputerBuilder ram(int gb) {
        if (gb <= 0) throw new IllegalArgumentException("RAM must be positive");
        this.ramGb = gb;
        return this;
    }

    /**
     * Sets the storage capacity.
     * @param gb storage in gigabytes
     * @return this builder
     */
    public ComputerBuilder storage(int gb) {
        this.storageGb = gb;
        return this;
    }

    /**
     * Sets the discrete GPU model.
     * @param gpu GPU model string
     * @return this builder
     */
    public ComputerBuilder gpu(String gpu) {
        this.gpu = gpu;
        return this;
    }

    /**
     * Sets the case style.
     * @param style case description
     * @return this builder
     */
    public ComputerBuilder caseStyle(String style) {
        this.caseStyle = style;
        return this;
    }

    /**
     * Adds a WiFi card.
     * @return this builder
     */
    public ComputerBuilder wifi() {
        this.hasWifi = true;
        return this;
    }

    /**
     * Adds Bluetooth.
     * @return this builder
     */
    public ComputerBuilder bluetooth() {
        this.hasBluetooth = true;
        return this;
    }

    /**
     * Constructs and returns the configured {@link Computer}.
     *
     * @return the finished computer
     * @throws IllegalStateException if no CPU has been specified
     */
    public Computer build() {
        if (cpu == null || cpu.isBlank()) {
            throw new IllegalStateException("A computer must have a CPU");
        }
        return new Computer(this);
    }
}
