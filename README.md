# Attention Fabric

A minimal-dependency Java research framework investigating spatially organized, surprise-damped directed networks composed of hypothetical units called Silk. Each unit may adapt its position, prune or form connections, and generate coordinate-conditioned nonlinear outputs within a three-dimensional state space. The project explores whether local prediction error and spatial organization can support continuous-function approximation without global backpropagation.

The framework differs from a conventional artificial neural network in that it explores structural change rather than global parameter optimization through backpropagation. The experimental objective is to determine whether spatial position and local prediction error can form useful representations for continuous-function approximation.

Each Silk is a stateful unit in a three-dimensional state space. A unit receives activity through receivers and emits transformed activity through teeth. Directed connections may join the teeth of one unit to the receivers of another, forming a network that is loosely tree-structured.

A Silk maintains local state and parameters, including baseline excitation, firing threshold, and surprise damping. On each update, it combines incoming activity, compares observed activity with a local prediction, and computes a surprise measure. Surprise may increase or decrease activity propagation, influence connection formation or pruning, and guide movement within the state space. Exact update equations and control rules remain to be specified and will determine whether “spacetime,” trajectory formation, and migration are implemented mechanisms or merely conceptual descriptions.


