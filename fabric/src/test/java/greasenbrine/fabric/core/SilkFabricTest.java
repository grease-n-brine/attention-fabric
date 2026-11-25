package greasenbrine.fabric.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SilkFabricTest {

    private static FabricParameters staticParameters(int capacity) {
        return FabricParameters.defaults(capacity)
            .withRecruitment(false)
            .withPruning(false)
            .withMigration(false);
    }

    private static SilkFabric fabric(int capacity, long seed) {
        return new SilkFabric(staticParameters(capacity), SilkParameters.defaults(), seed);
    }

    @Test
    void onlyRootIsAttachedInitially() {
        SilkFabric f = fabric(5, 1L);
        assertEquals(1, f.activeCount());
        assertTrue(f.isAttached(0));
        for (int i = 1; i < 5; i++) {
            assertFalse(f.isAttached(i));
        }
    }

    @Test
    void sameSeedProducesIdenticalPositions() {
        SilkFabric a = fabric(8, 42L);
        SilkFabric b = fabric(8, 42L);
        for (int i = 0; i < 8; i++) {
            assertEquals(a.getUnit(i).getPosition(), b.getUnit(i).getPosition());
        }
    }

    @Test
    void childReceivesParentOutput() {
        SilkFabric f = fabric(3, 7L);
        Silk root = f.getRoot();
        Silk child = f.getUnit(1);
        assertTrue(f.attach(root, child));

        f.step(0.4);

        double expectedChildInput = root.getLastOutput();
        assertEquals(1, child.getHistory().size());
        assertEquals(expectedChildInput, child.getHistory().get(0).input(), 1e-12);
    }

    @Test
    void dormantUnitsContributeZeroOutput() {
        SilkFabric f = fabric(4, 3L);
        f.attach(f.getRoot(), f.getUnit(2));
        f.step(0.9);
        double[] out = f.outputs();
        assertEquals(f.getRoot().getLastOutput(), out[0]);
        assertEquals(0.0, out[1]);
        assertEquals(f.getUnit(2).getLastOutput(), out[2]);
        assertEquals(0.0, out[3]);
    }

    @Test
    void attachRespectsMaxTeeth() {
        SilkFabric f = new SilkFabric(staticParameters(5).withMaxTeeth(2), SilkParameters.defaults(), 1L);
        Silk root = f.getRoot();
        assertTrue(f.attach(root, f.getUnit(1)));
        assertTrue(f.attach(root, f.getUnit(2)));
        assertFalse(f.attach(root, f.getUnit(3)));
        assertEquals(3, f.activeCount());
    }

    @Test
    void attachRejectsRootAndAlreadyAttachedUnits() {
        SilkFabric f = fabric(4, 1L);
        Silk root = f.getRoot();
        assertFalse(f.attach(root, root));
        assertTrue(f.attach(root, f.getUnit(1)));
        assertFalse(f.attach(root, f.getUnit(1)));
        assertFalse(f.attach(f.getUnit(2), f.getUnit(3)), "dormant parent cannot attach");
    }

    @Test
    void detachOnlyRemovesLeaves() {
        SilkFabric f = fabric(4, 1L);
        Silk root = f.getRoot();
        Silk mid = f.getUnit(1);
        Silk leaf = f.getUnit(2);
        f.attach(root, mid);
        f.attach(mid, leaf);

        assertFalse(f.detach(root));
        assertFalse(f.detach(mid));
        assertTrue(f.detach(leaf));
        assertFalse(f.isAttached(2));
        assertNull(f.getParent(2));
        assertTrue(f.detach(mid));
        assertEquals(1, f.activeCount());
    }

    @Test
    void propagationFollowsTreeDepth() {
        SilkFabric f = fabric(4, 1L);
        Silk root = f.getRoot();
        Silk a = f.getUnit(1);
        Silk b = f.getUnit(2);
        f.attach(root, a);
        f.attach(a, b);
        f.step(0.5);
        assertSame(root, f.getParent(1));
        assertSame(a, f.getParent(2));
        assertEquals(a.getLastOutput(), b.getHistory().get(0).input(), 1e-12);
    }

    @Test
    void firingRootRecruitsNearestDormantUnit() {
        FabricParameters p = staticParameters(6).withRecruitment(true);
        SilkFabric f = new SilkFabric(p, SilkParameters.defaults(), 11L);
        Silk root = f.getRoot();

        Silk nearest = null;
        double best = Double.POSITIVE_INFINITY;
        for (int i = 1; i < 6; i++) {
            double d = f.getUnit(i).getPosition().getDistanceTo(root.getPosition());
            if (d < best) {
                best = d;
                nearest = f.getUnit(i);
            }
        }

        f.step(5.0);
        f.step(5.0);
        assertTrue(f.getRoot().getHistory().get(1).fired());
        assertTrue(f.isAttached(nearest.getId()));
        assertSame(root, f.getParent(nearest.getId()));
    }

    @Test
    void recruitmentIsBoundedByCapacity() {
        FabricParameters p = staticParameters(5).withRecruitment(true).withMaxTeeth(10);
        SilkFabric f = new SilkFabric(p, SilkParameters.defaults(), 2L);
        for (int i = 0; i < 200; i++) {
            f.step(i % 2 == 0 ? 5.0 : -5.0);
        }
        assertEquals(5, f.activeCount());
    }

    @Test
    void noRecruitmentWhenDisabled() {
        SilkFabric f = fabric(5, 2L);
        for (int i = 0; i < 50; i++) {
            f.step(i % 2 == 0 ? 5.0 : -5.0);
        }
        assertEquals(1, f.activeCount());
    }

    @Test
    void quietLeavesArePrunedAfterBurst() {
        FabricParameters p = new FabricParameters(4, 3, false, true, 0.01, 1.0, false, 0.0, 2.0);
        SilkFabric f = new SilkFabric(p, SilkParameters.defaults(), 5L);
        Silk root = f.getRoot();
        Silk leaf = f.getUnit(1);
        f.attach(root, leaf);

        for (int i = 0; i < 5; i++) {
            f.step(i % 2 == 0 ? 5.0 : -5.0);
        }
        assertTrue(f.isAttached(1));

        for (int i = 0; i < 200 && f.isAttached(1); i++) {
            f.step(0.0);
        }
        assertFalse(f.isAttached(1));
        assertEquals(1, f.activeCount());
        assertEquals(0, leaf.getTickCount(), "detached unit is reset");
    }

    @Test
    void branchesCollapseFromLeavesInward() {
        FabricParameters p = new FabricParameters(4, 3, false, true, 0.0, 1.0, false, 0.0, 2.0);
        SilkFabric f = new SilkFabric(p, SilkParameters.defaults(), 5L);
        Silk root = f.getRoot();
        Silk mid = f.getUnit(1);
        f.attach(root, mid);
        f.attach(mid, f.getUnit(2));

        int steps = 0;
        while (f.isAttached(2) && steps < 500) {
            assertTrue(f.isAttached(1), "parent must remain attached while it has a child");
            f.step(0.0);
            steps++;
        }
        assertFalse(f.isAttached(2));
        assertTrue(f.isAttached(1), "parent is not detached in the same tick as its last child");

        for (int i = 0; i < 500 && f.isAttached(1); i++) {
            f.step(0.0);
        }
        assertFalse(f.isAttached(1));
        assertEquals(1, f.activeCount());
    }

    @Test
    void excitedUnitsMigrateAndStayWithinBounds() {
        FabricParameters p = new FabricParameters(2, 3, false, false, 0.05, 0.0, true, 0.5, 1.0);
        SilkFabric f = new SilkFabric(p, SilkParameters.defaults(), 9L);
        Silk root = f.getRoot();
        var start = root.getPosition();

        for (int i = 0; i < 50; i++) {
            f.step(i % 2 == 0 ? 5.0 : -5.0);
            var pos = root.getPosition();
            assertTrue(Math.abs(pos.getX()) <= 1.0 && Math.abs(pos.getY()) <= 1.0 && Math.abs(pos.getZ()) <= 1.0);
        }
        assertTrue(root.getPosition().getDistanceTo(start) > 0.0);
    }

    @Test
    void unitsAtOrBelowBaselineDoNotMigrate() {
        FabricParameters p = new FabricParameters(2, 3, false, false, 0.05, 0.0, true, 0.5, 1.0);
        SilkFabric f = new SilkFabric(p, SilkParameters.defaults(), 9L);
        Silk root = f.getRoot();
        var start = root.getPosition();
        for (int i = 0; i < 20; i++) {
            f.step(0.0);
        }
        assertEquals(start, root.getPosition());
    }

    @Test
    void dormantUnitsDoNotMigrate() {
        FabricParameters p = new FabricParameters(3, 3, false, false, 0.05, 0.0, true, 0.5, 1.0);
        SilkFabric f = new SilkFabric(p, SilkParameters.defaults(), 9L);
        var start = f.getUnit(1).getPosition();
        for (int i = 0; i < 20; i++) {
            f.step(i % 2 == 0 ? 5.0 : -5.0);
        }
        assertEquals(start, f.getUnit(1).getPosition());
    }
}
