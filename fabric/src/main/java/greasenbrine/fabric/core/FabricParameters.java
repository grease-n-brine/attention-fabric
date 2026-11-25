package greasenbrine.fabric.core;

/**
 * Structural parameters governing a population of Silk units.
 *
 * <ul>
 *   <li>{@code capacity}: total number of units in the pool, including the root.</li>
 *   <li>{@code maxTeeth}: maximum number of outgoing connections per unit.</li>
 *   <li>{@code recruitmentEnabled}: whether a firing unit may attach a dormant unit.</li>
 *   <li>{@code pruningEnabled}: whether leaf units below baseline may be detached.</li>
 *   <li>{@code pruneMargin}: deviation below baseline required for pruning eligibility.</li>
 *   <li>{@code pruneProbability}: per-tick probability that an eligible leaf is detached.</li>
 *   <li>{@code migrationEnabled}: whether units above baseline move along random trajectories.</li>
 *   <li>{@code migrationStep}: displacement per unit of positive deviation.</li>
 *   <li>{@code positionBound}: half-width of the cube within which positions are confined.</li>
 * </ul>
 */
public record FabricParameters(
    int capacity,
    int maxTeeth,
    boolean recruitmentEnabled,
    boolean pruningEnabled,
    double pruneMargin,
    double pruneProbability,
    boolean migrationEnabled,
    double migrationStep,
    double positionBound
) {
    public FabricParameters {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        if (maxTeeth < 0) {
            throw new IllegalArgumentException("maxTeeth must be non-negative");
        }
        if (pruneMargin < 0.0) {
            throw new IllegalArgumentException("pruneMargin must be non-negative");
        }
        if (pruneProbability < 0.0 || pruneProbability > 1.0) {
            throw new IllegalArgumentException("pruneProbability must lie in [0, 1]");
        }
        if (migrationStep < 0.0) {
            throw new IllegalArgumentException("migrationStep must be non-negative");
        }
        if (positionBound <= 0.0) {
            throw new IllegalArgumentException("positionBound must be positive");
        }
    }

    public static FabricParameters defaults(int capacity) {
        return new FabricParameters(capacity, 3, true, true, 0.05, 0.05, true, 0.01, 2.0);
    }

    public FabricParameters withRecruitment(boolean enabled) {
        return new FabricParameters(capacity, maxTeeth, enabled, pruningEnabled, pruneMargin, pruneProbability, migrationEnabled, migrationStep, positionBound);
    }

    public FabricParameters withPruning(boolean enabled) {
        return new FabricParameters(capacity, maxTeeth, recruitmentEnabled, enabled, pruneMargin, pruneProbability, migrationEnabled, migrationStep, positionBound);
    }

    public FabricParameters withMigration(boolean enabled) {
        return new FabricParameters(capacity, maxTeeth, recruitmentEnabled, pruningEnabled, pruneMargin, pruneProbability, enabled, migrationStep, positionBound);
    }

    public FabricParameters withMaxTeeth(int value) {
        return new FabricParameters(capacity, value, recruitmentEnabled, pruningEnabled, pruneMargin, pruneProbability, migrationEnabled, migrationStep, positionBound);
    }
}
