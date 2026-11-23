# Attention Fabric

A minimal-dependency Java research framework investigating spatially organized, surprise-damped directed networks composed of hypothetical units called Silk. Each unit may adapt its position, prune or form connections, and generate coordinate-conditioned nonlinear outputs within a three-dimensional state space. The project explores whether local prediction error and spatial organization can support continuous-function approximation without global backpropagation.

The framework differs from a conventional artificial neural network in that it explores structural change rather than global parameter optimization through backpropagation. The experimental objective is to determine whether spatial position and local prediction error can form useful representations for continuous-function approximation.

Each Silk is a stateful unit in a three-dimensional state space. A unit receives an activity signal through receivers and emits a transformed signal through teeth. Directed connections join the teeth of one unit to the receivers of another, forming a loosely tree-structured network in which the signal is progressively transformed at each branch.

A Silk maintains local state and parameters, including baseline excitation, firing threshold, desired excitation level, and surprise damping. Excitation can leak over time, allowing activity to decay toward the unit's baseline or desired level. When incoming activity raises excitation above a firing threshold, the Silk emits the corresponding transformed signal through its teeth.

Prediction error, or surprise, is not itself the propagated signal. It is a residual control quantity that can alter excitation, firing, connection formation, and pruning. Excess excitation above a desired level may indicate an anomalous or unrepresented pattern. Such excitation can implicitly recruit additional Silk units by raising their activation across firing thresholds, expanding the network's computational branches without replacing the original signal with surprise. Exact leak, excitation, surprise, and recruitment equations remain to be specified. The spatial trajectories, branch formation, pruning, and migration behavior are proposed mechanisms until their implementation and evaluation are complete.




