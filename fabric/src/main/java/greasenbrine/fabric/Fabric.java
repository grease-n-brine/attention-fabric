/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package greasenbrine.fabric;

import greasenbrine.fabric.core.Silk;
import greasenbrine.fabric.core.SilkTick;
import greasenbrine.fabric.structures.Point;

/**
 *
 * @author nepsauce
 */
public class Fabric {

    public static void main(String[] args) {
        Silk silk = new Silk(
            1,
            new Point(new double[]{0.0, 0.0, 0.0}),
            0.5,
            0.1,
            0.5,
            0.7,
            0.20,
            0.15,
            0.8,
            0.1
        );
        silk.initialize();

        double[] inputs = {10.0, 10.0, 10.0, 10.0, 30.0, 30.0, 30.0, 30.0};

        System.out.println("tick input expected surprise excitation triggered");
        for (double input : inputs) {
            SilkTick tick = silk.step(new Point(new double[]{input, input, input}));
            System.out.printf(
                "%d %.1f %.3f %.3f %.3f %s%n",
                tick.getTick(),
                tick.getReceiverInput().getX(),
                tick.getExpectedInput().getX(),
                tick.getRawSurprise(),
                tick.getExcitation().magnitude(),
                tick.isTriggered()
            );
        }
    }
}
