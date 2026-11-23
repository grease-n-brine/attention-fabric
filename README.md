# Attention Fabric

A minimal-dependency Java research framework investigating spatially organized, surprise-damped directed networks composed of hypothetical units called Silk. Each unit may adapt its position, prune or form connections, and generate coordinate-conditioned nonlinear outputs within a three-dimensional state space. The project explores whether local prediction error and spatial organization can support continuous-function approximation without global backpropagation.

The framework differs from a conventional artificial neural network in that it explores structural change rather than global parameter optimization through backpropagation. The experimental objective is to determine whether spatial position and local prediction error can form useful representations for continuous-function approximation.

Each Silk is a stateful unit in a three-dimensional state space. A unit receives an activity signal through receivers and emits a transformed signal through teeth. Directed connections join the teeth of one unit to the receivers of another, forming a loosely tree-structured network in which the signal is progressively transformed at each branch.

Each Silk has current excitation, baseline excitation, excitation gain, and a leakage rate. Incoming signals increase excitation according to gain, while leakage removes activity over time. As gain and leakage balance, excitation approaches a stable level, and the baseline slowly adapts toward that level.

Prediction error, or surprise, is not itself the propagated signal. It is a residual control quantity that can alter excitation, firing, connection formation, and pruning. Unusually high excitation may indicate unfamiliar input and trigger additional Silk units to receive the signal. Excitation below the baseline may mark weak or unproductive branches, making them eligible for probabilistic pruning.

During network exploration, Silk units may migrate along randomized trajectories. Migration allows units to encounter new signal regions or connection partners. Subsequent stability, excitation response, and prediction accuracy determine whether the resulting branches remain or are pruned. Exact gain, leakage, surprise, recruitment, pruning, and migration rules remain to be specified. These mechanisms are proposed research behaviors until their implementation and evaluation are complete.




