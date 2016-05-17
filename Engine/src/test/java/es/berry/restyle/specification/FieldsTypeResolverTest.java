package es.berry.restyle.specification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.specification.generated.Type;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FieldsTypeResolverTest {

    private Spec spec = null;
    private Set<Resource> resources = null;
    private Resource resource = null;
    private Set<Field> fields = null;
    private Field field = null;
    private Set<Type> types = null;

    private JsonNode specNode = null;
    private ObjectNode r = null;
    private ObjectNode f = null;

    private ObjectMapper mapper = null;

    @Before
    public void setUp() {
        SpecObjectMapper.configure("whatever.json");
        mapper = SpecObjectMapper.getInstance();

        spec = new Spec();
        resources = new HashSet<>();
        resource = new Resource();
        fields = new HashSet<>();
        field = new Field();
        types = new HashSet<>();

        Type t1 = new Type();
        Type t2 = new Type();

        t2.setName("t2");
        t2.setMin(2);
        t2.setType(Field.Type.STRING);

        t1.setName("t1");
        t1.setMin(1);
        t1.setType(Field.Type.STRING);
        t1.setRequired(true);

        types.add(t2);
        types.add(t1);

        field.setName("f");
        field.setMin(3);
        fields.add(field);

        resource.setName("r");
        resources.add(resource);

        spec.setTypes(types);
        resource.setFields(fields);
        spec.setResources(resources);

        specNode = mapper.valueToTree(spec);

        r = (ObjectNode) SpecHelper.findResourceByName(specNode, "r");
        assert r != null;

        f = (ObjectNode) SpecHelper.findFieldByName(r, "f");
        assert f != null;

        f.put("type", "t1");
    }

    @Test
    public void resolve() {
        new FieldsTypeResolver(specNode).resolve();

        assertEquals("f", f.get("name").asText());
        assertEquals("string", f.get("type").asText());
        assertEquals(3, f.get("min").asInt());
        assertEquals(true, f.get("required").asBoolean());
    }

    @Test(expected = NullPointerException.class)
    public void resolveEndlessType() {
        f.set("type", null);

        new FieldsTypeResolver(specNode).resolve();
    }
}
