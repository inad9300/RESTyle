package es.berry.restyle.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Json {

    private String json;

    public Json(String jsonFilename) throws IOException {
        this.json = new String(Files.readAllBytes(Paths.get(jsonFilename)), "UTF-8");
    }

    public boolean isValid() {
        return false;
    }

    public boolean isValidAgainstSchema(String schemaFilename) {
        return false;
    }

    // public String toYaml() {}

    // public String fromYaml() {}

}
