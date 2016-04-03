package es.berry.restyle.generators.interfaces;

public interface SqlStatements {

    String doGetMany(/* filter */); // -> needs to be broken down to smaller pieces, e.g. doSelectLimit (Oracle? vs. MySQL)

    String doGetOne(String resourceName, String[] fields, int id);

    String doCreate(int n);

    String doReplaceMany(int[] ids);

    String doReplaceOne(int id);

    String doUpdateMany(int[] ids);

    String doUpdateOne(int id);

    String doDeleteMany(int[] ids); // Or all of them

    String doDeleteOne(int id);

    String doGetRelated(/* filter */);

    String doCreateRelated(int n);

    String doDeleteRelated(int[] id); // Or all of them

    String doDeleteRelationship(int idResource, int idRelated);

}
