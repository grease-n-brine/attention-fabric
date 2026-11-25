# Spatially organized Silk networks can adapt locally to approximate continuous functions

Attention Fabric is a minimal-dependency Java research framework investigating whether local prediction error, excitation dynamics, and spatially organized directed connections can support continuous-function approximation. Its units, called Silk, may adapt their positions, form or prune connections, and generate coordinate-conditioned nonlinear outputs within a three-dimensional state space.

The central hypothesis is that locally predictive units can organize useful computational branches without global parameter optimization through backpropagation. The framework differs from a typical artificial neural network in that it investigates structural adaptation and residual-driven recruitment rather than updating a fixed network through a single global objective.

The hypothesis predicts that repeated exposure to a signal will cause each Silk to regulate its excitation toward a stable state. Excitation above or below the unit's adaptive baseline will influence firing, prediction error, or structural behavior.

Each Silk is a stateful unit in a three-dimensional state space. A unit receives an activity signal through receivers and emits a transformed signal through teeth. Directed connections join the teeth of one unit to the receivers of another, forming a loosely tree-structured network in which the signal is progressively transformed at each branch.

Each Silk has current excitation, baseline excitation, excitation gain, and a leakage rate. Incoming signals increase excitation according to gain, while leakage removes activity over time. As gain and leakage balance, excitation approaches a stable level, and the baseline adapts toward that level.

Prediction error, or surprise, is not the propagated signal. It is a residual control quantity that may alter excitation, firing, connection formation, and pruning. Unusually high excitation may indicate unfamiliar input and trigger additional Silk units to receive the signal. Excitation below the baseline may mark weak or unproductive branches, making them eligible for probabilistic pruning.

During network exploration, Silk units may migrate along randomized trajectories. Migration allows units to encounter new signal regions or connection partners. Subsequent stability, excitation response, and prediction accuracy determine whether the resulting branches remain or are pruned.

The gain, leakage, surprise, recruitment, pruning, and migration rules are implemented and tested incrementally. This README presents the proposed mechanism as a working hypothesis; experiments will determine which assumptions hold and how the mechanism performs relative to established approaches.




