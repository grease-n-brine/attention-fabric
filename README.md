# Attention Fabric

A minimal-dependency Java research framework investigating spatially organized, surprise-damped directed networks composed of hypothetical units called Silk. Each unit may adapt its position, prune or form connections, and generate coordinate-conditioned nonlinear outputs within a three-dimensional state space. The project explores whether local prediction error and spatial organization can support continuous-function approximation without global backpropagation.

The framework differs from a conventional artificial neural network in that it explores structural change rather than global parameter optimization through backpropagation. The experimental objective is to determine whether spatial position and local prediction error can form useful representations for continuous-function approximation.

Each Silk is a stateful unit in a three-dimensional state space. A unit receives an activity signal through receivers and emits a transformed signal through teeth. Directed connections join the teeth of one unit to the receivers of another, forming a loosely tree-structured network in which the signal is progressively transformed at each branch.

A Silk maintains local state and parameters, including baseline excitation, firing threshold, and surprise damping. It combines incoming activity, compares the observed activity with a local prediction, and computes surprise as the residual between the two. Surprise is not itself the signal propagated through the network: it is a control quantity that can raise excitation or suppress activity.

When surprise increases excitation, the Silk may recruit additional units through new or existing teeth and transmit the corresponding signal onward. Recruitment propagates the transformed computational signal, while the residual regulates whether and how strongly that propagation occurs. Therefore, surprise can influence branching and the strength of transformation without becoming a separate network value. Exact update equations and recruitment rules remain to be specified. The spatial trajectories, branch formation, pruning, and migration behavior are proposed mechanisms until their implementation and evaluation are complete.




