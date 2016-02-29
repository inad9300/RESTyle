package es.berry.restyle.configuration;

import java.util.ArrayList;
import java.util.List;

public class Relationship {

    private List<Field> fields = new ArrayList<Field>();

    public Relationship() {
        // TODO: initialize
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

}
