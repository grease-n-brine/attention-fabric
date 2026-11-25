# Local Excitation Dynamics and Structural Adaptation in a Spatially Organized Network of Surprise-Driven Units

## Abstract

This study examines whether a population of locally adaptive units, each regulating its own excitation and embedded in a three-dimensional state space, can respond to anomalous input, habituate to repeated input, and support online approximation of continuous functions without global gradient optimization. Each unit, termed a Silk, maintains an expected input, computes surprise as the absolute residual between observed and expected input, accumulates excitation from surprise subject to leakage, and fires when excitation exceeds an adaptive baseline by a fixed threshold. Firing recruits dormant units into a directed tree, leaf units below baseline are pruned probabilistically, and excited units migrate along random trajectories. Three experiments were conducted with ten random seeds. A single unit reduced surprise under constant input from 1.000 to 0.014 within 20 ticks, produced a surprise of 2.012 at a step change in input, and returned to 10 percent of that value within 11 ticks. Under a frequent pulse schedule, unit firing probability at pulses declined from 0.614 to 0.471 with a slow baseline, whereas under a rare schedule every pulse elicited firing throughout the run. At the network level, the mean firing cascade at pulses declined from 3.85 to 1.90 units under the rare schedule. In online function approximation on four targets, the complete fabric attained a mean test error of 0.188 on a sine target compared with 0.0004 for fixed random features of the same capacity and 0.0062 for a backpropagation perceptron. Replacing the root transform with an identity reduced fabric error by a factor of three to ten, identifying the root nonlinearity as the dominant limitation, and per-seed analysis showed that pruning reduced active units to between 23 and 37 in half of the runs with a corresponding increase in error. The mechanism exhibits the predicted qualitative behaviors of surprise reduction, anomaly response, and partial habituation, and the function approximation results identify specific structural constraints that must be addressed before the architecture can be considered competitive.

## 1. Introduction

Most contemporary learning systems adjust a fixed set of parameters by minimizing a global objective, typically through backpropagation of error (Rumelhart et al., 1986). Biological nervous systems additionally exhibit homeostatic regulation of excitability (Turrigiano and Nelson, 2004), prediction-driven processing (Rao and Ballard, 1999; Friston, 2010), and structural changes in connectivity. Habituation, the decline of response to a repeated stimulus, is among the most widely documented of these behaviors (Thompson and Spencer, 1966; Rankin et al., 2009).

This work investigates a minimal computational model that combines these properties. The research question is stated as follows: can spatially organized units that regulate their own excitation and adapt their structure in response to local surprise detect anomalous patterns, habituate to repeated patterns, and provide features sufficient for continuous-function approximation?

The contribution of this study is threefold. First, a fully specified update rule for a surprise-driven, leaky, homeostatic unit with a coordinate-conditioned output transform is presented. Second, a population model in which firing recruits dormant units into a directed tree, inactive leaves are pruned, and excited units migrate is defined and implemented. Third, the model is evaluated against the stated research question with reproducible experiments and compared with three reference models. The implementation uses no numerical libraries and runs in approximately 30 seconds on commodity hardware.

## 2. Related Work

Predictive coding models (Rao and Ballard, 1999) and the free-energy formulation (Friston, 2010) treat prediction error as the quantity that drives both inference and learning. The present model shares the use of prediction residual as a control signal but does not propagate error between units; the residual affects only the excitation of the unit in which it arises.

Homeostatic plasticity (Turrigiano and Nelson, 2004) maintains neuronal activity within an operating range through slow adjustment of intrinsic excitability and synaptic strength. The adaptive baseline in the present model is a scalar instance of this principle.

Growing neural gas (Fritzke, 1995) and neuroevolution of augmenting topologies (Stanley and Miikkulainen, 2002) construct network structure incrementally. The recruitment rule described here is simpler than either and is driven by firing events alone.

Reservoir computing (Jaeger, 2001; Maass et al., 2002) and random feature methods (Rahimi and Recht, 2007) train only a linear readout on fixed nonlinear features. The readout used in this study follows that approach and is trained by the least-mean-squares rule (Widrow and Hoff, 1960). The fixed random feature baseline in Section 4 is therefore the most direct comparison for assessing whether structural dynamics add value beyond the feature family itself.

## 3. Model

### 3.1 Unit dynamics

A Silk $i$ has position $\mathbf{p}_i \in [-B, B]^3$, expected input $\hat{x}_i$, excitation $e_i \geq 0$, and adaptive baseline $b_i$. Given scalar input $x_t$ at tick $t$, the unit computes

$$
s_t = \lvert x_t - \hat{x}_{t-1} \rvert, \qquad
\tilde{s}_t = (1 - d)\, s_t,
$$

where $d \in [0, 1]$ is a dampening factor. Excitation is updated with gain and leakage and clamped to a maximum $e_{\max}$:

$$
e_t = \min\!\left(e_{\max},\; (1 - \lambda)\, e_{t-1} + g_x \lvert x_t \rvert + g_s\, \tilde{s}_t \right).
$$

The baseline tracks excitation with rate $\beta$:

$$
b_t = (1 - \beta)\, b_{t-1} + \beta\, e_t.
$$

The unit fires when the deviation $e_t - b_t$ reaches the threshold $\theta$. The expected input is then updated with rate $\alpha$:

$$
\hat{x}_t = (1 - \alpha)\, \hat{x}_{t-1} + \alpha\, x_t.
$$

Surprise is never transmitted. The quantity emitted through the unit's teeth is the coordinate-conditioned transform

$$
y_t = \tanh\!\left(p_x\, x_t + p_y + p_z\, x_t^2\right),
$$

so that the unit's position determines the nonlinearity it applies.

The parameter values used in all experiments were $\alpha = 0.2$, $\lambda = 0.1$, $g_x = 0$, $g_s = 0.5$, $d = 0.1$, $\beta = 0.05$, $\theta = 0.5$, and $e_{\max} = 10$. With $g_x = 0$, excitation is driven exclusively by surprise.

### 3.2 Fabric structure

A fabric is a pool of $N$ units with positions drawn uniformly from $[-B, B]^3$, $B = 2$. Unit 0 is the root and is permanently attached. All other units begin dormant. Attached units form a directed tree: each tooth of a unit connects to the receiver of exactly one child, and each unit has at most $k$ teeth, $k = 3$.

On each tick the root receives the external input and the tree is traversed in breadth-first order; every child receives the output of its parent computed on the same tick. Three structural phases follow.

**Recruitment.** Each unit that fired on the current tick and has fewer than $k$ teeth attaches the dormant unit nearest to it in the state space. A firing burst therefore adds computational branches around the excited region.

**Pruning.** Each attached leaf whose deviation satisfies $e - b < -m$, with margin $m = 0.05$, is detached with probability $q = 0.05$. Eligibility is evaluated on the structure at the start of the phase so that a parent cannot be detached in the same tick as its last child. Detached units are reset and return to the dormant pool.

**Migration.** Each attached unit with positive deviation moves by $\eta\, (e - b)$ along a direction drawn uniformly from the unit sphere, with step scale $\eta = 0.01$, and is confined to $[-B, B]^3$.

### 3.3 Readout

For function approximation, the outputs of attached units form a feature vector of length $N$ in which dormant units contribute zero. A linear readout with bias is trained by the least-mean-squares rule with rate $0.01$. The update for weight $w_i$ depends on the global prediction error and the local feature $y_i$ only; no error signal enters the fabric. Evaluation uses a side-effect-free forward pass that leaves unit state and structure unchanged.

## 4. Experimental Protocol

All experiments are deterministic given a seed. Ten seeds, 0 through 9, were used for every stochastic configuration. The implementation is written in Java with no external runtime dependencies. Results were produced with OpenJDK 27.0.1 on macOS (aarch64). Running the suite a second time produced byte-identical output files.

### 4.1 Experiment 1: single-unit adaptation

A single unit with the parameters in Section 3.1 received a piecewise-constant signal of 1.0 for 20 ticks, 3.0 for 20 ticks, and 1.0 for 20 ticks. The complete internal trajectory was recorded. Reported quantities are the surprise on the final tick of the first segment, the surprise on the first tick of the second segment, the number of ticks required for surprise to fall to 10 percent of that value, and the firing pattern.

### 4.2 Experiment 2: anomaly response and habituation

The background signal was $\sin(2\pi t / 50)$ with additive Gaussian noise of standard deviation 0.05, for 4000 ticks. After a warm-up of 200 ticks, pulses of amplitude 3.0 lasting one tick were added at fixed intervals: every 200 ticks (rare schedule, 19 pulses) or every 10 ticks (frequent schedule, 380 pulses). Seeds determined the noise realization and, for fabrics, the unit positions and stochastic structural decisions.

At the unit level, the firing probability and mean deviation at pulse ticks were computed over the first and last quarter of pulses. The false-alarm rate is the firing probability at non-pulse ticks after warm-up. The baseline rate $\beta$ was varied over $\{0.01, 0.05, 0.2\}$ to test its role in habituation.

At the network level, a fabric of $N = 32$ units received the same signals. The cascade size is the number of units firing on a pulse tick; recruitment is the change in attached count on that tick. Both were averaged over the first and last quarter of pulses. The attached count was also sampled every 50 ticks.

### 4.3 Experiment 3: online function approximation

Four targets on $[-1, 1]$ were used: $\sin(\pi x)$, $x^2$, $\exp(-8x^2)$, and $\lvert x \rvert$. Each model received 20 000 training samples with inputs drawn uniformly and was then evaluated on a grid of 201 equally spaced points. The reported metric is mean squared error on the grid.

Reference models were an affine model trained by LMS with rate 0.05; fixed random features of the form $\tanh(a x + b + c x^2)$ with $a, b, c$ drawn uniformly from $[-2, 2]$, 64 features, and an LMS readout with rate 0.01; and a perceptron with 16 hyperbolic tangent hidden units trained by stochastic gradient descent with rate 0.01. Fabric variants used $N = 64$ and the parameters of Section 3. The static variant attaches all units as a random tree and disables every structural rule. The recruit variant enables recruitment only. Two variants disable one of migration or pruning. The full variant enables all three. A diagnostic variant of the full fabric replaces the root transform with the identity so that children receive the raw input.

## 5. Results

### 5.1 Single-unit adaptation

Under the first constant segment, surprise fell monotonically from 1.000 at tick 1 to 0.014 at tick 20. Excitation rose to 1.21 at tick 6 and then decayed as surprise diminished, reaching 0.50 at tick 20, while the baseline rose to 0.54. The unit fired on ticks 2 through 11 and was silent from tick 12 onward.

The step to 3.0 at tick 21 produced a surprise of 2.012. Excitation reached 2.70 at tick 26, and the unit fired on ticks 21 through 33. Surprise fell to 10 percent of its spike value within 11 ticks and reached 0.029 by tick 40. The baseline rose to 1.39 over the second segment.

The return to 1.0 at tick 41 produced a surprise of 1.977, close in magnitude to the first step. The deviation on that tick was 0.43, below the threshold of 0.5, because the baseline had risen during the preceding segment. The unit fired from tick 42 through tick 50, and excitation reached its maximum over the whole run, 2.96, at tick 45, because the residual excitation from the second segment had not fully leaked before the third change arrived. The elevated baseline therefore delayed the response to the second change by one tick and shortened the firing burst from 13 ticks to 9.

These observations confirm the three single-unit predictions stated in the hypothesis: surprise declines under a stationary input, a change in input produces a large transient, and the response to a later change is reduced by the adaptation that followed the earlier one.

### 5.2 Anomaly response and habituation

Table 1 reports unit-level results. Under the rare schedule, every pulse elicited firing in every seed for all three baseline rates, with mean deviation at pulses between 1.16 and 1.25 and no measurable difference between early and late pulses. The false-alarm rate on the background signal decreased with increasing $\beta$, from 0.017 at $\beta = 0.01$ to 0.005 at $\beta = 0.2$.

Under the frequent schedule, the response depended on $\beta$. With $\beta = 0.01$, the firing probability at pulses declined from 0.614 in the first quarter to 0.471 in the last quarter, and the mean deviation declined from 0.715 to 0.520. With $\beta = 0.05$, firing declined from 0.444 to 0.400 and deviation from 0.577 to 0.539. With $\beta = 0.2$, firing was 0.721 early and 0.722 late, with deviation unchanged at 0.59 and 0.58. The false-alarm rate under the frequent schedule was substantially higher than under the rare schedule, 0.231 at $\beta = 0.01$ and 0.068 at $\beta = 0.2$, because excitation elevated by one pulse had not fully decayed before the next.

**Table 1.** Unit-level response at pulse ticks (mean over 10 seeds; standard deviations below 0.03 for all rates and below 0.02 for all deviations).

| Schedule | $\beta$ | Fire rate, early | Fire rate, late | Deviation, early | Deviation, late | False alarms |
|---|---|---|---|---|---|---|
| Rare | 0.01 | 1.000 | 1.000 | 1.254 | 1.194 | 0.017 |
| Rare | 0.05 | 1.000 | 1.000 | 1.195 | 1.203 | 0.011 |
| Rare | 0.20 | 1.000 | 1.000 | 1.157 | 1.162 | 0.005 |
| Frequent | 0.01 | 0.614 | 0.471 | 0.715 | 0.520 | 0.231 |
| Frequent | 0.05 | 0.444 | 0.400 | 0.577 | 0.539 | 0.171 |
| Frequent | 0.20 | 0.721 | 0.722 | 0.591 | 0.581 | 0.068 |

The dependence on $\beta$ admits a direct interpretation. A slow baseline integrates excitation over many pulses and rises progressively, which reduces deviation over the course of the run. A fast baseline tracks the excitation within a single inter-pulse interval and resets between pulses, so the deviation at each pulse is approximately the same throughout the run. Habituation in this model therefore requires a baseline time constant longer than the inter-stimulus interval.

Table 2 reports network-level results for $N = 32$ and $\beta = 0.05$. Under the rare schedule, the mean number of units firing on a pulse tick declined from 3.85 in the first quarter to 1.90 in the last quarter, and mean recruitment per pulse declined from 0.60 to 0.18 units. Under the frequent schedule, the cascade declined from 1.90 to 1.20 and recruitment from 0.077 to 0.071. Seed-to-seed variability was high; the standard deviation of early cascade size under the rare schedule was 5.50.

**Table 2.** Network-level response at pulse ticks (mean ± standard deviation over 10 seeds).

| Schedule | Cascade, early | Cascade, late | Recruits, early | Recruits, late | Attached, final |
|---|---|---|---|---|---|
| Rare | 3.85 ± 5.50 | 1.90 ± 2.05 | 0.60 ± 0.83 | 0.18 ± 0.21 | 23.6 ± 8.0 |
| Frequent | 1.90 ± 1.43 | 1.20 ± 0.90 | 0.08 ± 0.11 | 0.07 ± 0.10 | 25.5 ± 7.7 |

The attached count reached a mean of 21.4 units by tick 200 under both schedules, before any pulse occurred. The background sinusoid, whose derivative changes continuously, produces a persistent residual against the exponential moving average expectation, and this residual alone was sufficient to drive recruitment of two thirds of the pool. Pulses then recruited from the remaining dormant units, and the decline in recruitment over the run reflects both depletion of that reserve and the rise in baselines across attached units. The rare schedule produced a larger initial cascade than the frequent schedule because each rare pulse arrived at units whose excitation had fully decayed, maximizing the deviation it produced.

### 5.3 Online function approximation

Table 3 reports test error on the four targets. The fixed random feature model achieved the lowest error on every target, between $3.5 \times 10^{-5}$ for the quadratic and $3.0 \times 10^{-3}$ for the Gaussian. The perceptron achieved errors between $1.0 \times 10^{-3}$ and $7.3 \times 10^{-3}$. The affine model achieved errors between 0.087 and 0.210, which constitute the reference for a model that captures no curvature.

Every fabric variant performed between the affine model and the random feature model, and closer to the former. The full fabric achieved 0.188 on the sine target, 0.073 on the quadratic, 0.106 on the Gaussian, and 0.070 on the absolute value. Differences among the static, recruit-only, no-migration, no-pruning, and full variants were within one standard deviation on every target.

**Table 3.** Test mean squared error on 201 grid points after 20 000 online samples (mean ± standard deviation over 10 seeds). Parameters trained: affine 2, random features 65, perceptron 49, all fabric variants 65.

| Model | Sine | Quadratic | Gaussian | Absolute |
|---|---|---|---|---|
| Affine | 0.210 ± 0.007 | 0.094 ± 0.002 | 0.126 ± 0.003 | 0.087 ± 0.002 |
| Random features | 0.0004 ± 0.0002 | 0.00004 ± 0.00003 | 0.0030 ± 0.0014 | 0.0007 ± 0.0001 |
| Perceptron | 0.0062 ± 0.0015 | 0.0023 ± 0.0007 | 0.0073 ± 0.0039 | 0.0010 ± 0.0002 |
| Fabric, static | 0.201 ± 0.186 | 0.073 ± 0.022 | 0.106 ± 0.035 | 0.069 ± 0.021 |
| Fabric, recruit only | 0.230 ± 0.177 | 0.077 ± 0.020 | 0.118 ± 0.017 | 0.076 ± 0.015 |
| Fabric, no migration | 0.204 ± 0.150 | 0.073 ± 0.014 | 0.116 ± 0.017 | 0.073 ± 0.009 |
| Fabric, no pruning | 0.247 ± 0.172 | 0.077 ± 0.032 | 0.116 ± 0.026 | 0.074 ± 0.023 |
| Fabric, full | 0.188 ± 0.164 | 0.073 ± 0.017 | 0.106 ± 0.027 | 0.070 ± 0.014 |
| Fabric, full, identity root | 0.061 ± 0.053 | 0.0072 ± 0.0149 | 0.029 ± 0.018 | 0.0073 ± 0.0119 |

Two further observations explain the gap between the fabric and the random feature model, which share the same transform family and the same readout.

First, the root unit is a bottleneck. Every unit other than the root receives, in place of the external input, the output of its parent, and the parent's output passes through a hyperbolic tangent. The root alone determines the first such compression. Replacing the root transform with the identity, while leaving every other unit and every structural rule unchanged, reduced error by a factor of 3.1 on the sine target, 10.0 on the quadratic, 3.6 on the Gaussian, and 9.5 on the absolute value. The resulting errors, between 0.007 and 0.061, remain above the random feature model by one to two orders of magnitude, which indicates that depth beyond the root compounds the effect.

Second, pruning interacts adversely with the readout. On the sine target, the full fabric finished with 64 attached units in three seeds and achieved errors between 0.016 and 0.027 in those runs. In the remaining seven seeds the final attached count was between 23 and 62, and the four runs with the fewest units (23, 26, 31, and 37) produced errors between 0.280 and 0.439, which exceed the affine model. When a leaf is pruned its feature becomes zero, but the readout weight associated with that unit index persists; when the index is later re-attached under a different parent the weight no longer corresponds to the feature it was trained on. The no-pruning variant did not resolve this because it retained the root bottleneck, and it exhibited a comparable standard deviation arising from the random tree structure itself.

## 6. Discussion

The single-unit results are consistent with the hypothesis in every respect that was tested. Surprise declines monotonically under a stationary input, the response to a change is large and transient, and adaptation following one change attenuates the response to the next. The shortened firing burst on the return to baseline in Experiment 1 and the dependence of habituation on the baseline rate in Experiment 2 both follow from the same mechanism: a baseline that integrates past excitation reduces future deviation.

The network results confirm that firing propagates into recruitment and that the cascade size declines with repetition. They also reveal a property of the model that was not anticipated in the hypothesis. The exponential moving average expectation cannot track a smoothly varying signal without lag, so the background sinusoid alone produced sufficient surprise to recruit most of the pool before any anomaly was presented. In this model, surprise measures deviation from a constant prediction, and any non-constant signal is surprising to some degree. A unit whose expectation included a velocity term, or whose expected input was itself predicted by its parent, would separate ordinary variation from anomaly more sharply.

The function approximation results are informative in the negative direction. The fabric performed between the affine and random feature models on every target, and the structural rules produced no measurable difference among variants. The identity-root diagnostic attributes most of the gap to the compounding of hyperbolic tangents along the path from root to leaf, and the per-seed analysis attributes the high variance to the interaction between pruning and a readout that indexes features by unit identity. Both are properties of the specific design choices made here and both suggest concrete modifications: a root that passes the input unchanged, a readout that is reset or re-initialized for the unit when it is re-attached, and a pruning criterion that considers the readout weight associated with the unit.

The comparison with fixed random features is the most important one for the research question. Both models train the same readout on the same transform family with the same number of features. The random feature model receives the raw input at every feature and never changes its features; the fabric composes features along a tree and changes them over time. The random feature model performed better by two to three orders of magnitude. Structural adaptation, as implemented here, therefore degraded the feature set available to the readout. The present results do not support the hypothesis that local structural adaptation yields features superior to a fixed random set for this class of targets.

## 7. Limitations

The input signal is scalar. The expectation is a first-order exponential moving average and cannot represent trend or periodicity. Recruitment selects the nearest dormant unit and does not consider whether that unit's transform is useful. Pruning considers excitation alone and ignores the readout. The readout indexes features by unit identity, which is unstable under pruning and re-attachment. The migration rule is an unbiased random walk and was not observed to influence any measured outcome. Only one setting of the unit parameters was evaluated in Experiments 1 and 3. Ten seeds provide limited power for distinguishing fabric variants whose standard deviations are comparable to their means. The habituation experiment used a fixed pulse amplitude and two schedules; the dependence on amplitude and on irregular schedules was not examined.

## 8. Conclusion

A surprise-driven, leaky, homeostatic unit with a coordinate-conditioned transform exhibits surprise reduction under stationary input, a transient response to change, and attenuation of subsequent responses through baseline adaptation. A population of such units connected as a directed tree recruits new branches when excited, and the size of the recruitment cascade declines with repetition. These behaviors were obtained with eight scalar parameters per unit and nine structural parameters per fabric, without gradient computation. The same population, used as a feature generator for online function approximation, performed worse than fixed random features of the same family and capacity, and diagnostic experiments identified the root nonlinearity and the pruning-readout interaction as the principal causes. The mechanism is adequate for the anomaly-response component of the hypothesis and inadequate, in its present form, for the function approximation component. The modifications suggested by the diagnostics are specific and testable.

## Reproducibility

All code, results, and this manuscript are contained in the repository. The experiment suite is executed with `./run-experiments.sh` from the repository root, which writes seven CSV files and an environment record to `results/`. The unit tests, 62 in total, are executed with `mvn -f fabric/pom.xml test`. Repeated execution of the suite produced byte-identical result files. Parameter values are recorded in `results/environment.txt`.

## References

Friston, K. (2010). The free-energy principle: a unified brain theory? *Nature Reviews Neuroscience*, 11(2), 127–138.

Fritzke, B. (1995). A growing neural gas network learns topologies. In *Advances in Neural Information Processing Systems 7* (pp. 625–632). MIT Press.

Jaeger, H. (2001). The "echo state" approach to analysing and training recurrent neural networks. GMD Report 148, German National Research Center for Information Technology.

Maass, W., Natschläger, T., and Markram, H. (2002). Real-time computing without stable states: a new framework for neural computation based on perturbations. *Neural Computation*, 14(11), 2531–2560.

Rahimi, A., and Recht, B. (2007). Random features for large-scale kernel machines. In *Advances in Neural Information Processing Systems 20* (pp. 1177–1184).

Rankin, C. H., Abrams, T., Barry, R. J., Bhatnagar, S., Clayton, D. F., Colombo, J., Coppola, G., Geyer, M. A., Glanzman, D. L., Marsland, S., McSweeney, F. K., Wilson, D. A., Wu, C.-F., and Thompson, R. F. (2009). Habituation revisited: an updated and revised description of the behavioral characteristics of habituation. *Neurobiology of Learning and Memory*, 92(2), 135–138.

Rao, R. P. N., and Ballard, D. H. (1999). Predictive coding in the visual cortex: a functional interpretation of some extra-classical receptive-field effects. *Nature Neuroscience*, 2(1), 79–87.

Rumelhart, D. E., Hinton, G. E., and Williams, R. J. (1986). Learning representations by back-propagating errors. *Nature*, 323(6088), 533–536.

Stanley, K. O., and Miikkulainen, R. (2002). Evolving neural networks through augmenting topologies. *Evolutionary Computation*, 10(2), 99–127.

Thompson, R. F., and Spencer, W. A. (1966). Habituation: a model phenomenon for the study of neuronal substrates of behavior. *Psychological Review*, 73(1), 16–43.

Turrigiano, G. G., and Nelson, S. B. (2004). Homeostatic plasticity in the developing nervous system. *Nature Reviews Neuroscience*, 5(2), 97–107.

Widrow, B., and Hoff, M. E. (1960). Adaptive switching circuits. In *IRE WESCON Convention Record*, Part 4 (pp. 96–104).
