package gr.forth.ics.isl.aqub.virtuoso;

public interface VirtuosoOps {

    public void clearGraphContents(String graph, boolean logging);

    public long triplesNum(String graph);

    public void addTriple(String s, String p, String o, String graph);

    public void addLitTriple(String s, String p, String o, String graph);

    public void addLitTriple(String s, String p, double o, String graph);

    public long countSparqlResults(String query);

    public boolean executeUpdateQuery(String sparul, boolean logging);

    public Object executeSparqlQuery(String sparql, boolean logging);

}
