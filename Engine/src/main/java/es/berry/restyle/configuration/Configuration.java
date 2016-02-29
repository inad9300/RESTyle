package es.berry.restyle.configuration;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private static Configuration instance = new Configuration();

    // TODO: Include global configuration

    private List<Resource> resources = new ArrayList<Resource>();

    public Configuration() {}

    public Configuration getInstance() {
        return this.instance;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

}
