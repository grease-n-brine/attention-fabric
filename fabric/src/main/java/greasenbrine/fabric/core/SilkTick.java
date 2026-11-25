package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;

/**
 * Represents a single tick of the silk simulation.
 * 
 * @silkID The unique identifier of the silk.
 * @tick The current tick number of the simulation.
 * @position The 3D position of the silk at this tick.
 * @receiverInput The input received by the silk at this tick.
 * @expectedInput The input expected by the silk at this tick.
 * @excitation The excitation of the silk at this tick.
 * @targetExcitation The target excitation level of the silk.
 * @excitationThreshold The maximum excitation the silk can have.
 * @rawSurprise The raw surprise value of the silk.
 * @dampedSurprise The damped surprise value of the silk.
 * @dampeningFactor The dampening factor applied to the surprise.
 * @triggered True if excitation deviation reached @effectiveThreshold.
 * @surpriseBoosted True if damped surprise actively pushed excitation
 * @recruiting True if unit emits a signal through teeth downstream.
 */

public class SilkTick {
    private final int silkID;
    private final long tick;
    private final Point position;
    private Point receiverInput;
    private Point expectedInput;
    private final Point excitation;
    private final double targetExcitation;
    private final double excitationThreshold;
    private final double rawSurprise;
    private final double dampedSurprise;
    private final double dampeningFactor;
    private final boolean triggered;
    private final boolean surpriseBoosted;
    private final boolean recruiting;

    public SilkTick(
        int silkID,
        long tick,
        Point position,
        Point receiverInput,
        Point expectedInput,
        Point excitation,
        double targetExcitation,
        double excitationThreshold,
        double rawSurprise,
        double dampedSurprise,
        double dampeningFactor,
        boolean triggered,
        boolean surpriseBoosted,
        boolean recruiting
    ) {
        this.silkID = silkID;
        this.tick = tick;
        this.position = position;
        this.receiverInput = receiverInput;
        this.expectedInput = expectedInput;
        this.excitation = excitation;
        this.targetExcitation = targetExcitation;
        this.excitationThreshold = excitationThreshold;
        this.rawSurprise = rawSurprise;
        this.dampedSurprise = dampedSurprise;
        this.dampeningFactor = dampeningFactor;
        this.triggered = triggered;
        this.surpriseBoosted = surpriseBoosted;
        this.recruiting = recruiting;
    }

    public void setReceiverInput(Point input) {
        receiverInput = input;
    }

    public void setExpectedInput(Point input) {
        expectedInput = input;
    }

    public int getSilkID() { return silkID; }
    public long getTick() { return tick; }
    public Point getPosition() { return position; }
    public Point getReceiverInput() { return receiverInput; }
    public Point getExpectedInput() { return expectedInput; }
    public Point getExcitation() { return excitation; }
    public double getTargetExcitation() { return targetExcitation; }
    public double getExcitationThreshold() { return excitationThreshold; }
    public double getRawSurprise() { return rawSurprise; }
    public double getDampedSurprise() { return dampedSurprise; }
    public double getDampeningFactor() { return dampeningFactor; }
    public boolean isTriggered() { return triggered; }
    public boolean isSurpriseBoosted() { return surpriseBoosted; }
    public boolean isRecruiting() { return recruiting; }
}
