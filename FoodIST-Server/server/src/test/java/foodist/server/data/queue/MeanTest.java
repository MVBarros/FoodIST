package foodist.server.data.queue;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Compute average of list of values in O(1)
* */
public class MeanTest {
    private static final double DELTA = 0.00001;

    private static final double[] VALUES = new double[] {2d, 4d, 2d, 7d, 12d, 3.4d, 7.22d, 34.19d, 155.8d};
    @Test
    public void emptyConstructorMeanTest() {
        Mean mean = new Mean();
        assertEquals(mean.getCurrValue(), 0d, DELTA);
    }

    @Test
    public void valueConstructorMeanTest() {
        Mean mean = new Mean(2);
        assertEquals(mean.getCurrValue(), 2d, DELTA);
    }

    @Test
    public void addToEmptyMeanTest() {
        Mean mean = new Mean();
        mean.add(2d);
        assertEquals(mean.getCurrValue(), 2d, DELTA);
    }

    @Test
    public void complexMeanCalculationFromEmptyConstructor() {
        Mean mean = new Mean();
        Arrays.stream(VALUES).forEach(mean::add);
        double avg = Arrays.stream(VALUES).average().orElseThrow();
        assertEquals(mean.getCurrValue(), avg, DELTA);
    }


    @Test
    public void complexMeanCalculationFromValueConstructor() {
        Mean mean = new Mean(VALUES[0]);
        double[] vals = Arrays.copyOfRange(VALUES, 1, VALUES.length);
        Arrays.stream(vals).forEach(mean::add);
        double avg = Arrays.stream(VALUES).average().orElseThrow();
        assertEquals(mean.getCurrValue(), avg, DELTA);
    }
}
