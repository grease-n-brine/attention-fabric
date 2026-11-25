/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package greasenbrine.fabric;

import greasenbrine.fabric.core.Silk;
import greasenbrine.fabric.structures.Point;

/**
 *
 * @author nepsauce
 */
public class Fabric {

    public static void main(String[] args) {
        Silk silk = new Silk(
            1,
            new Point(new double[]{0, 0, 0}),
            1.0,
            0.1,
            0.5,
            0.7,
            1.0,
            1.2,
            0.8,
            0.1
        );
        silk.initialize();

        Point previousSurprise = new Point(new double[]{0, 0, 0});
        previousSurprise = silk.calculateSurprise(0);
        Point currentSurprise = silk.calculateSurprise(0);
    }
}
