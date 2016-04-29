package es.berry.restyle.specification;

import es.berry.restyle.specification.generated.Relation;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.specification.generated.Spec;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class SpecHelperTest {

    private Spec spec = null;
    private Resource resA = null;
    private Resource resB = null;
    private Relation relA = null;
    private Set<Resource> resources = null;
    private Set<Relation> relations = null;

    @Before
    public void setUp() {
        spec = new Spec();
        resources = new HashSet<>();
        relations = new HashSet<>();

        resA = new Resource();
        resB = new Resource();
        relA = new Relation();

        // Setup: resA has many resB
        resA.setName("resA");
        resB.setName("resB");
        relA.setWith("resB");
        relA.setType(Relation.Type.HAS_MANY);

        relations.add(relA);
        resources.add(resA);
        resources.add(resB);

        resA.setRelations(relations);
        spec.setResources(resources);
    }

    @Test
    public void resourceContainsRelation() {
        assertTrue(SpecHelper.resourceContainsRelation(resA, resB.getName()));
        assertFalse(SpecHelper.resourceContainsRelation(resA, resA.getName()));

        assertTrue(SpecHelper.resourceContainsRelation(resA, resB.getName(), Relation.Type.HAS_MANY.toString()));
        assertFalse(SpecHelper.resourceContainsRelation(resA, resB.getName(), Relation.Type.HAS_ONE.toString()));
    }

    @Test
    public void findResourceByName() {
        assertEquals(resA, SpecHelper.findResourceByName(spec, resA.getName()));
        assertNull(SpecHelper.findResourceByName(spec, "non-existing resource"));
    }

    @Test
    public void findRelationByName() {
        assertEquals(relA, SpecHelper.findRelationByName(resA, resB.getName()));
        assertNull(SpecHelper.findRelationByName(resA, "non-existing relationship"));
    }

    @Test
    public void needsAuthentication() {
        // Put an empty set as ACL, meaning nobody has access
        resA.setAcl(new HashSet<String>());

        assertTrue(SpecHelper.needsAuthentication(resA));
        assertFalse(SpecHelper.needsAuthentication(resB));
    }
}
