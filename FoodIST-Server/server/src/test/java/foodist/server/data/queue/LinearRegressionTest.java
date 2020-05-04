package foodist.server.data.queue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinearRegressionTest {
    private static final double DELTA = 0.00001;

    @Test
    public void identityFunctionTest() {
        LinearRegression regression = new LinearRegression(new double[]{1, 2, 3}, new double[]{1, 2, 3});
        assertEquals(regression.slope(), 1, DELTA);
        assertEquals(regression.intercept(), 0, DELTA);
        assertEquals(regression.predict(5), 5, DELTA);
    }

    @Test
    public void XPlus1FunctionTest() {
        LinearRegression regression = new LinearRegression(new double[]{1, 2, 3}, new double[]{2, 3, 4});
        assertEquals(regression.slope(), 1, DELTA);
        assertEquals(regression.intercept(), 1, DELTA);
        assertEquals(regression.predict(5), 6, DELTA);
    }

    @Test
    public void twoTimesXFunction() {
        LinearRegression regression = new LinearRegression(new double[]{1, 2, 3}, new double[]{2, 4, 6});
        assertEquals(regression.slope(), 2, DELTA);
        assertEquals(regression.intercept(), 0, DELTA);
        assertEquals(regression.predict(5), 10, DELTA);
    }

    @Test
    public void twoTimesXPlus1Function() {
        LinearRegression regression = new LinearRegression(new double[]{1, 2, 3}, new double[]{3, 5, 7});
        assertEquals(regression.slope(), 2, DELTA);
        assertEquals(regression.intercept(), 1, DELTA);
        assertEquals(regression.predict(5), 11, DELTA);
    }
}
