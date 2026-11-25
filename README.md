# Can spatially organized Silk networks regulate excitation and adapt to anomalous patterns in continuous signals?

Attention Fabric is a minimal-dependency Java research framework investigating whether local prediction error, excitation dynamics, and spatially organized directed connections can detect anomalies and support continuous-function approximation. Its units, called Silk, adapt their positions, form or prune connections, and generate coordinate-conditioned nonlinear outputs within a three-dimensional state space.

The central hypothesis is that Silk units regulate excitation by converging toward locally stable states. Normal signals reduce excitation and disturbance, whereas unfamiliar or anomalous activity produces a residual that can trigger a chain reaction of firing, recruitment, migration, and structural adaptation. If an initially anomalous pattern occurs repeatedly, the network adapts and reduces its response, recognizing that the pattern has become normal behavior.

The framework differs from a typical artificial neural network by investigating this homeostatic and structural adaptation rather than updating a fixed network through global parameter optimization. The network does not process anomalies as a separate category: anomaly is inferred from persistent excitation and prediction residual relative to a unit's adapted baseline.

Each Silk is a stateful unit in a three-dimensional state space. A unit receives an activity signal through receivers and emits a transformed signal through teeth. Directed connections join the teeth of one unit to the receivers of another, forming a loosely tree-structured network in which the signal is progressively transformed at each branch.

Each Silk has current excitation, baseline excitation, excitation gain, and a leakage rate. Incoming signals increase excitation according to gain, while leakage removes activity over time. As gain and leakage balance, excitation approaches a stable level, and the baseline adapts toward that level.

Prediction error, or surprise, is not the propagated signal. It is a residual control quantity that may alter excitation, firing, connection formation, and pruning. An unusually large residual indicates insufficient local adaptation and can trigger additional Silk units to receive and process the signal. Frequent repetition allows the local equilibrium and baseline to absorb the pattern, gradually reducing excitation, firing, and recruitment for it.

During network exploration, Silk units may migrate along randomized trajectories. Migration allows units to encounter new signal regions or connection partners. Excitation above the adapted baseline marks active exploration or weak local representation, while excitation below the baseline may mark weak or unproductive branches eligible for probabilistic pruning.

The gain, leakage, surprise, recruitment, pruning, and migration rules are implemented and tested incrementally. This README presents the proposed mechanism as a working hypothesis; experiments will determine which assumptions hold and how the mechanism performs relative to established approaches.




