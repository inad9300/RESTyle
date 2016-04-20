package es.berry.restyle.generators.interfaces;

import es.berry.restyle.specification.generated.Resource;

/**
 * Interface to be implemented by plugins in the database layer, to be used later by plugins implementing a server.
 */
public interface SqlCarrier {
    String getPrimaryKey(Resource res);
    String getForeignKey(Resource res);
    String getTableName(Resource res);
    String getManyToManyTableName(Resource resA, Resource resB);
    String getHasOneStr();
    String getHasManyStr();
}
