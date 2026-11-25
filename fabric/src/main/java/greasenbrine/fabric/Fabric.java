package greasenbrine.fabric;

import greasenbrine.fabric.core.Silk;
import greasenbrine.fabric.core.SilkParameters;
import greasenbrine.fabric.core.SilkTick;
import greasenbrine.fabric.structures.Point;

public class Fabric {

    public static void main(String[] args) {
        Silk silk = new Silk(1, new Point(new double[]{1.0, 0.0, 0.0}), SilkParameters.defaults());

        double[] inputs = {1.0, 1.0, 1.0, 1.0, 3.0, 3.0, 3.0, 3.0};

        System.out.println("tick,input,expected,surprise,excitation,baseline,fired");
        for (double input : inputs) {
            SilkTick tick = silk.step(input);
            System.out.printf(
                "%d,%.1f,%.3f,%.3f,%.3f,%.3f,%s%n",
                tick.tick(),
                tick.input(),
                tick.expectedInput(),
                tick.rawSurprise(),
                tick.excitation(),
                tick.baseline(),
                tick.fired()
            );
        }
    }
}
