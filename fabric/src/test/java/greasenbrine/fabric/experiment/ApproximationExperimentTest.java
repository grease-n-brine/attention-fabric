package greasenbrine.fabric.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import greasenbrine.fabric.core.SilkParameters;
import greasenbrine.fabric.experiment.ApproximationExperiment.Result;
import greasenbrine.fabric.experiment.ApproximationExperiment.Target;
import greasenbrine.fabric.learning.OnlineRegressor;
import java.util.Map;
import java.util.function.LongFunction;
import org.junit.jupiter.api.Test;

class ApproximationExperimentTest {

    @Test
    void modelCatalogueContainsBaselinesAndAblations() {
        Map<String, LongFunction<OnlineRegressor>> models = ApproximationExperiment.models(SilkParameters.defaults());
        assertTrue(models.containsKey("linear"));
        assertTrue(models.containsKey("random-features"));
        assertTrue(models.containsKey("mlp"));
        assertTrue(models.containsKey("fabric-static"));
        assertTrue(models.containsKey("fabric-full"));
        assertTrue(models.containsKey("fabric-full-passthrough"));
        assertEquals(9, models.size());
    }

    @Test
    void runIsDeterministicForSeed() {
        Target target = ApproximationExperiment.targets().get(0);
        LongFunction<OnlineRegressor> factory = ApproximationExperiment.models(SilkParameters.defaults()).get("fabric-full");
        Result a = ApproximationExperiment.run(target, "fabric-full", factory.apply(3L), 3L);
        Result b = ApproximationExperiment.run(target, "fabric-full", factory.apply(3L), 3L);
        assertEquals(a.testMse(), b.testMse());
        assertEquals(a.activeUnits(), b.activeUnits());
    }

    @Test
    void staticFabricAttachesWholePool() {
        Target target = ApproximationExperiment.targets().get(1);
        LongFunction<OnlineRegressor> factory = ApproximationExperiment.models(SilkParameters.defaults()).get("fabric-static");
        Result r = ApproximationExperiment.run(target, "fabric-static", factory.apply(0L), 0L);
        assertEquals(ApproximationExperiment.CAPACITY, r.activeUnits());
    }

    @Test
    void nonFabricModelsReportNoActiveUnits() {
        Target target = ApproximationExperiment.targets().get(0);
        LongFunction<OnlineRegressor> factory = ApproximationExperiment.models(SilkParameters.defaults()).get("mlp");
        Result r = ApproximationExperiment.run(target, "mlp", factory.apply(0L), 0L);
        assertEquals(-1, r.activeUnits());
        assertTrue(r.testMse() < 0.05, "mse=" + r.testMse());
    }
}
