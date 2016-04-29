package es.berry.restyle.core;

import es.berry.restyle.fakes.FakePlugin;
import es.berry.restyle.fakes.FakePluginDependencies;
import es.berry.restyle.fakes.FakePluginI;
import es.berry.restyle.fakes.FakePluginNoDependencies;
import es.berry.restyle.specification.generated.Spec;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GeneratorTest {
    private FakePlugin firstFakePlugin = null;
    private FakePlugin lastFakePlugin = null;
    private FakePluginDependencies fakePluginDependencies = null;


    @Before
    public void setUp() {
        firstFakePlugin = createMockBuilder(FakePlugin.class).addMockedMethod("doSomething").createMock();
        lastFakePlugin = new FakePlugin(createMock(Spec.class), createMock(File.class));
        fakePluginDependencies = new FakePluginDependencies(createMock(Spec.class), createMock(File.class));
    }

    @Test
    public void prevGenerator() {
        lastFakePlugin.setPrevGenerator(firstFakePlugin);
        lastFakePlugin.prevGeneratorMustImplement(FakePluginI.class);

        assertThat(firstFakePlugin, is(instanceOf(FakePluginI.class)));
    }

    @Test(expected = RuntimeException.class)
    public void prevGeneratorMissingDependency() {
        List<Class<? extends Generator>> fakePlugins = new ArrayList<>();
        fakePlugins.add(fakePluginDependencies.getClass());

        Generator.runAll(fakePlugins, createMock(Spec.class), createMock(File.class));
    }

    @Test(expected = RuntimeException.class)
    public void prevGeneratorNonMatchingDependency() {
        final FakePluginNoDependencies fakePluginNoDependencies =
                new FakePluginNoDependencies(createMock(Spec.class), createMock(File.class));

        List<Class<? extends Generator>> fakePlugins = new ArrayList<>();
        fakePlugins.add(fakePluginNoDependencies.getClass());
        fakePlugins.add(fakePluginDependencies.getClass());

        Generator.runAll(fakePlugins, createMock(Spec.class), createMock(File.class));
    }

    @Test
    public void invokePrevMethod() {
        firstFakePlugin.doSomething();
        expectLastCall().times(3);
        replay(firstFakePlugin);

        lastFakePlugin.setPrevGenerator(firstFakePlugin);
        lastFakePlugin.prevGeneratorMustImplement(FakePluginI.class);

        lastFakePlugin.invokePrevMethod("doSomething");
        lastFakePlugin.invokePrevMethod("doSomething");
        lastFakePlugin.invokePrevMethod("doSomething");

        verify(firstFakePlugin);
    }
}
