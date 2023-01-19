package gr.forth.ics.isl.aqub.virtuoso;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import virtuoso.jena.driver.*;

public class JenaVirtuosoRep {

    String connStr;
    String username, password;
    VirtGraph graph;

    public JenaVirtuosoRep(String virt_instance, String usr, String pwd) {
        connStr = "jdbc:virtuoso://" + virt_instance + ":1111";
        username = usr;
        password = pwd;
        this.graph = new VirtGraph(connStr, username, password);
    }

    public JenaVirtuosoRep(String graph, String virt_instance, String usr, String pwd) throws Exception {
        connStr = "jdbc:virtuoso://" + virt_instance + ":1111";
        username = usr;
        password = pwd;
        this.graph = new VirtGraph(graph, connStr, username, password);
    }

    public void setGraph(String graph) {
        this.graph = new VirtGraph(graph, connStr, username, password);
    }

    public VirtGraph getGraph() {
        return this.graph;
    }

    public void addTriple(Node s, Node p, Node o) {
        this.graph.add(new Triple(s, p, o));
    }

    public void addTriple(Node s, Node p, String o) {
        this.graph.add(new Triple(s, p, Node.createLiteral(o)));
    }

    public void executeSPARQL(String query) {
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, this.graph);
        ResultSet results = vqe.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.nextSolution();
            RDFNode s = result.get("s");
            RDFNode p = result.get("p");
            RDFNode o = result.get("o");
            System.out.println(" { " + s + " " + p + " " + o + " . }");
        }
    }

    public static void main(String[] args) {
        JenaVirtuosoRep jena = new JenaVirtuosoRep("139.91.183.40", "dba", "dba");
        String query = "...";
        jena.executeSPARQL(query);
    }
}
