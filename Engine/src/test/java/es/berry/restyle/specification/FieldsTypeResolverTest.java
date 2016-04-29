package es.berry.restyle.specification;

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

    @Before
    public void setUp() {
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
        t2.setType("string");

        t1.setName("t1");
        t1.setMin(1);
        t1.setType("t2");
        t1.setRequired(true);

        types.add(t2);
        types.add(t1);

        field.setName("f");
        field.setMin(3);
        field.setType("t1");
        fields.add(field);

        resource.setName("r");
        resources.add(resource);

        spec.setTypes(types);
        resource.setFields(fields);
        spec.setResources(resources);
    }

    @Test
    public void resolve() {
        new FieldsTypeResolver(spec).resolve();

        Field f = (Field) resource.getFields().toArray()[0];

        assertEquals("f", f.getName());
        assertEquals("string", f.getType());
        assertEquals(3, f.getMin());
        assertEquals(true, f.getRequired());
    }

    @Test(expected = NullPointerException.class)
    public void resolveEndlessType() {
        Set<Field> mistakenFields = new HashSet<>();
        Field fieldWithoutType = new Field();
        mistakenFields.add(fieldWithoutType);
        resource.setFields(mistakenFields);

        new FieldsTypeResolver(spec).resolve();
    }
}
