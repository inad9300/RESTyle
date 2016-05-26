package es.berry.restyle.specification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.specification.generated.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SpecHelperTest {

    private Spec spec = null;
    private JsonNode specNode = null;
    private Resource resA = null;
    private Resource resB = null;
    private Relation relA = null;
    private Field fieldA = null;
    private Field fieldB = null;
    private Set<Resource> resources = null;
    private Set<Relation> relations = null;
    private ObjectMapper mapper = null;
    private Role roleA = null;
    private Role roleB = null;
    private Role roleC = null;

    @Before
    public void setUp() {
        spec = new Spec();
        resources = new HashSet<>();
        relations = new HashSet<>();

        final Set<Role> roles = new HashSet<>();
        roleA = new Role();
        roleB = new Role();
        roleC = new Role();
        roleA.setName("roleA");
        roleB.setName("roleB");
        roleC.setName("roleC");
        roles.addAll(Arrays.asList(roleA, roleB, roleC));
        spec.setRoles(roles);

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

        final Set<Field> fields = new HashSet<>();
        fieldA = new Field();
        fieldB = new Field();
        fieldA.set("name", "fA");
        fieldB.set("name", "fB");
        fields.addAll(Arrays.asList(fieldA, fieldB));
        resA.setFields(fields);

        // roleA can do everything in resA; roleB can't do anything.
        final List<String> rolesResA = new ArrayList<>();
        rolesResA.add("roleA");
        resA.setAcl(rolesResA);

        // rb can read and create in resB; roleA can't do anything.
        final List<String> rolesResB = new ArrayList<>();
        rolesResB.add("roleB");
        resB.setAcl(rolesResB);
        final Map<String, List> aclB = new HashMap<>();
        aclB.put("read", rolesResB);
        aclB.put("create", rolesResB);
        resB.setAcl(aclB);

        resA.setRelations(relations);
        spec.setResources(resources);

        SpecObjectMapper.configure("whatever.json");
        mapper = SpecObjectMapper.getInstance();
        specNode = mapper.valueToTree(spec);
    }

    @Test
    public void findResourceByName() {
        assertEquals(resA, SpecHelper.findResourceByName(spec, resA.getName()));
        assertNull(SpecHelper.findResourceByName(spec, "non-existent resource"));
    }

    @Test
    public void findResourceByNameInNode() {
        assertEquals(mapper.valueToTree(resA), SpecHelper.findResourceByName(specNode, resA.getName()));
        assertNull(SpecHelper.findResourceByName(specNode, "non-existent resource"));
    }

    @Test
    public void findRelationByName() {
        assertEquals(relA, SpecHelper.findRelationByName(resA, resB.getName()));
        assertNull(SpecHelper.findRelationByName(resA, "non-existent relationship"));
    }

    @Test
    public void findRelationByNameInNode() {
        final JsonNode resANode = mapper.valueToTree(resA);
        assertEquals(mapper.valueToTree(relA), SpecHelper.findRelationByName(resANode, resB.getName()));
        assertNull(SpecHelper.findRelationByName(resANode, "non-existent relationship"));
    }

    @Test
    public void findFieldByName() {
        assertEquals(fieldA, SpecHelper.findFieldByName(resA, fieldA.getName()));
        assertNull(SpecHelper.findFieldByName(resA, "non-existent field"));
    }

    @Test
    public void findFieldByNameInNode() {
        final JsonNode fieldANode = mapper.valueToTree(fieldA);
        final JsonNode resANode = mapper.valueToTree(resA);
        assertEquals(fieldANode, SpecHelper.findFieldByName(resANode, fieldA.getName()));
        assertNull(SpecHelper.findFieldByName(resANode, "non-existent field"));
    }

    @Test
    public void resourceContainsRelation() {
        assertTrue(SpecHelper.resourceContainsRelation(resA, resB.getName()));
        assertFalse(SpecHelper.resourceContainsRelation(resA, resA.getName()));

        assertTrue(SpecHelper.resourceContainsRelation(resA, resB.getName(), Relation.Type.HAS_MANY.toString()));
        assertFalse(SpecHelper.resourceContainsRelation(resA, resB.getName(), Relation.Type.HAS_ONE.toString()));
    }

    @Test
    public void findRoleByName() {
        assertEquals(roleB, SpecHelper.findRoleByName(spec, roleB.getName()));
        assertNull(SpecHelper.findRoleByName(spec, "non-existent role"));
    }

    @Test
    public void findGuestRole() {
        roleC.setIsGuest(true);
        assertEquals(roleC, SpecHelper.findGuestRole(spec));
    }

    @Test
    public void findAdminRole() {
        roleC.setIsAdmin(true);
        assertEquals(roleC, SpecHelper.findAdminRole(spec));
    }

    @Test(expected = SpecException.class)
    public void unsupportedReflexiveRelations() {
        relA.setWith(resA.getName());
        SpecHelper.unsupportedReflexiveRelations(spec);
    }

    @Test
    public void authorization() {
        assertTrue(SpecHelper.roleCanRead(resA, roleA.getName()));
        assertTrue(SpecHelper.roleCanCreate(resA, roleA.getName()));
        assertTrue(SpecHelper.roleCanUpdate(resA, roleA.getName()));
        assertTrue(SpecHelper.roleCanDelete(resA, roleA.getName()));

        assertFalse(SpecHelper.roleCanRead(resA, roleB.getName()));
        assertFalse(SpecHelper.roleCanCreate(resA, roleB.getName()));
        assertFalse(SpecHelper.roleCanUpdate(resA, roleB.getName()));
        assertFalse(SpecHelper.roleCanDelete(resA, roleB.getName()));
    }
}
