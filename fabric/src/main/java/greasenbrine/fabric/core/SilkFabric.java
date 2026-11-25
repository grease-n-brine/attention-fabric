package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * A population of Silk units organized as a directed tree rooted at unit 0.
 *
 * <p>On each tick the root receives the external signal, and every attached unit
 * receives the output of its parent in breadth-first order. Units that are not
 * attached to the tree are dormant and contribute no output.</p>
 */
public class SilkFabric {

    private final FabricParameters parameters;
    private final Random random;
    private final List<Silk> units;
    private final Silk[] parent;
    private final boolean[] attached;
    private final List<Silk> firedThisTick = new ArrayList<>();
    private long tickCount;

    public SilkFabric(FabricParameters parameters, SilkParameters unitParameters, long seed) {
        this.parameters = parameters;
        this.random = new Random(seed);
        this.units = new ArrayList<>(parameters.capacity());
        this.parent = new Silk[parameters.capacity()];
        this.attached = new boolean[parameters.capacity()];

        for (int i = 0; i < parameters.capacity(); i++) {
            units.add(new Silk(i, randomPosition(), unitParameters));
        }
        attached[0] = true;
        tickCount = 0;
    }

    /** Presents one external scalar input to the root and propagates it through the tree. */
    public void step(double input) {
        firedThisTick.clear();
        Deque<Silk> queue = new ArrayDeque<>();

        Silk root = units.get(0);
        if (root.step(input).fired()) {
            firedThisTick.add(root);
        }
        queue.add(root);

        while (!queue.isEmpty()) {
            Silk current = queue.poll();
            double signal = current.getLastOutput();
            for (Silk child : current.getTeeth()) {
                if (child.step(signal).fired()) {
                    firedThisTick.add(child);
                }
                queue.add(child);
            }
        }
        tickCount++;

        if (parameters.recruitmentEnabled()) {
            recruit();
        }
        if (parameters.pruningEnabled()) {
            prune();
        }
        if (parameters.migrationEnabled()) {
            migrate();
        }
    }

    /**
     * Attached units whose excitation exceeds their baseline move along a random
     * direction by a distance proportional to the deviation. Positions remain
     * within the configured bound.
     */
    private void migrate() {
        double bound = parameters.positionBound();
        for (int i = 0; i < units.size(); i++) {
            if (!attached[i]) {
                continue;
            }
            Silk unit = units.get(i);
            double deviation = unit.getDeviation();
            if (deviation <= 0.0) {
                continue;
            }
            Point displacement = randomDirection().scale(parameters.migrationStep() * deviation);
            Point moved = unit.getPosition().add(displacement);
            unit.moveTo(new Point(new double[]{
                clamp(moved.getX(), -bound, bound),
                clamp(moved.getY(), -bound, bound),
                clamp(moved.getZ(), -bound, bound)
            }));
        }
    }

    private Point randomDirection() {
        Point p;
        do {
            p = new Point(new double[]{random.nextGaussian(), random.nextGaussian(), random.nextGaussian()});
        } while (p.magnitude() == 0.0);
        return p.normalize();
    }

    private static double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }

    /**
     * Leaf units whose excitation has fallen below their adaptive baseline by at
     * least the prune margin are detached with the configured probability.
     * Eligibility is evaluated against the structure at the start of the phase,
     * so a parent cannot be pruned in the same tick as its last child.
     */
    private void prune() {
        List<Silk> toDetach = new ArrayList<>();
        for (int i = 1; i < units.size(); i++) {
            if (!attached[i]) {
                continue;
            }
            Silk unit = units.get(i);
            if (!unit.getTeeth().isEmpty()) {
                continue;
            }
            if (unit.getDeviation() < -parameters.pruneMargin()
                && random.nextDouble() < parameters.pruneProbability()) {
                toDetach.add(unit);
            }
        }
        for (Silk unit : toDetach) {
            detach(unit);
        }
    }

    /**
     * Each unit that fired this tick and has spare teeth attaches the nearest
     * dormant unit, so that a firing burst recruits additional computational
     * branches around the excited region of the state space.
     */
    private void recruit() {
        for (Silk excited : firedThisTick) {
            if (excited.getTeeth().size() >= parameters.maxTeeth()) {
                continue;
            }
            Silk candidate = nearestDormant(excited.getPosition());
            if (candidate != null) {
                attach(excited, candidate);
            }
        }
    }

    private Silk nearestDormant(Point origin) {
        Silk best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 1; i < units.size(); i++) {
            if (attached[i]) {
                continue;
            }
            double d = units.get(i).getPosition().getDistanceTo(origin);
            if (d < bestDistance) {
                bestDistance = d;
                best = units.get(i);
            }
        }
        return best;
    }

    /** Output vector of length {@code capacity}; dormant units contribute zero. */
    public double[] outputs() {
        double[] out = new double[units.size()];
        for (int i = 0; i < units.size(); i++) {
            out[i] = attached[i] ? units.get(i).getLastOutput() : 0.0;
        }
        return out;
    }

    /**
     * Computes the output vector the tree would emit for {@code input} without
     * altering any unit state or structure. Used for evaluation.
     */
    public double[] probe(double input) {
        double[] out = new double[units.size()];
        Deque<Silk> queue = new ArrayDeque<>();
        Silk root = units.get(0);
        out[0] = root.transform(input);
        queue.add(root);
        while (!queue.isEmpty()) {
            Silk current = queue.poll();
            double signal = out[current.getId()];
            for (Silk child : current.getTeeth()) {
                out[child.getId()] = child.transform(signal);
                queue.add(child);
            }
        }
        return out;
    }

    public int activeCount() {
        int count = 0;
        for (boolean a : attached) {
            if (a) {
                count++;
            }
        }
        return count;
    }

    public boolean isAttached(int id) {
        return attached[id];
    }

    public Silk getParent(int id) {
        return parent[id];
    }

    public Silk getRoot() {
        return units.get(0);
    }

    public Silk getUnit(int id) {
        return units.get(id);
    }

    public List<Silk> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public List<Silk> getFiredThisTick() {
        return Collections.unmodifiableList(firedThisTick);
    }

    public FabricParameters getParameters() {
        return parameters;
    }

    public long getTickCount() {
        return tickCount;
    }

    /** Attaches {@code child} beneath {@code parentUnit}; returns false if the structure forbids it. */
    public boolean attach(Silk parentUnit, Silk child) {
        int childId = child.getId();
        if (childId == 0 || attached[childId] || !attached[parentUnit.getId()]) {
            return false;
        }
        if (parentUnit.getTeeth().size() >= parameters.maxTeeth()) {
            return false;
        }
        if (!parentUnit.attachTooth(child)) {
            return false;
        }
        attached[childId] = true;
        parent[childId] = parentUnit;
        return true;
    }

    /** Detaches a leaf unit from the tree, returning it to the dormant pool. */
    public boolean detach(Silk unit) {
        int id = unit.getId();
        if (id == 0 || !attached[id] || !unit.getTeeth().isEmpty()) {
            return false;
        }
        parent[id].detachTooth(unit);
        parent[id] = null;
        attached[id] = false;
        unit.reset();
        return true;
    }

    /**
     * Attaches every dormant unit to a uniformly chosen attached unit with spare
     * teeth, producing a fixed random tree over the whole pool. Used as a
     * static-topology control.
     */
    public void attachAllAsRandomTree() {
        for (int i = 1; i < units.size(); i++) {
            if (attached[i]) {
                continue;
            }
            List<Silk> candidates = new ArrayList<>();
            for (int j = 0; j < units.size(); j++) {
                if (attached[j] && units.get(j).getTeeth().size() < parameters.maxTeeth()) {
                    candidates.add(units.get(j));
                }
            }
            if (candidates.isEmpty()) {
                return;
            }
            Silk chosen = candidates.get(random.nextInt(candidates.size()));
            attach(chosen, units.get(i));
        }
    }

    Random random() {
        return random;
    }

    Point randomPosition() {
        double b = parameters.positionBound();
        return new Point(new double[]{
            uniform(-b, b),
            uniform(-b, b),
            uniform(-b, b)
        });
    }

    private double uniform(double low, double high) {
        return low + (high - low) * random.nextDouble();
    }
}
