package foodist.server.data;

import org.junit.*;

import static org.junit.Assert.assertEquals;

public class ServiceTest {

    private static final String NAME = "NAME";

    @Test
    public void validTest() {
        var service = new Service(NAME);
        assertEquals(service.getContractMenus().size(), 0);
        //No Exception is thrown
    }


    @Test(expected = IllegalArgumentException.class)
    public void nullUsername() {
        new Service(null);
    }

}
