package es.berry.restyle.generators.interfaces;

public interface SqlCarrier {
    String getForeignKey(String resourceName);
    String getManyToManyTableName(String nameResourceA, String nameResourceB);
}
