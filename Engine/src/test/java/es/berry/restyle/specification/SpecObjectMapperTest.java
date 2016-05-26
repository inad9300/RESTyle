package es.berry.restyle.specification;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SpecObjectMapperTest {

    // Does not work. The ObjectMapper gets configured somehow before this test is called (most likely due to the
    // execution of other tests, and the non-complete isolation of JUnit -- remember SpecObjectMapper is though to
    // behave as a Singleton)
//    @Test(expected = NullPointerException.class)
//    public void getNotConfiguredInstance() {
//        SpecObjectMapper.getInstance();
//    }

    @Test
    public void getInstance() {
        SpecObjectMapper.configure("whatever.json");
        assertNotNull(SpecObjectMapper.getInstance());
    }
}
