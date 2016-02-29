package es.berry.restyle.configuration;

import java.util.ArrayList;
import java.util.List;

public class Resource {

    private String name;
    private String description;

    private List<Field> fields = new ArrayList<Field>();
    private List<Relationship> relationships = new ArrayList<Relationship>();

    public Resource() {
        // TODO: construct from configuration file
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

}
