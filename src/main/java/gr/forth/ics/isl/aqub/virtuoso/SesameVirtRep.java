package gr.forth.ics.isl.aqub.virtuoso;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import virtuoso.sesame2.driver.VirtuosoRepository;

import org.openrdf.rio.RDFFormat;

public class SesameVirtRep implements VirtuosoOps {

    private Repository repository;
    private RepositoryConnection con;
    private Map<String, String> namespaces;

    public SesameVirtRep(String virt_instance, int port, String usr, String pwd) throws RepositoryException {
        repository = new VirtuosoRepository("jdbc:virtuoso://" + virt_instance + ":" + port + "/charset=UTF-8/log_enable=2", usr, pwd);
        con = repository.getConnection();
        con.setAutoCommit(false);
        initNamespaces();
    }

    public SesameVirtRep(Properties prop, boolean openConnection) throws RepositoryException {
        String virt_instance = prop.getProperty("Repository_IP");
        String usr = prop.getProperty("Repository_Username");
        String pwd = prop.getProperty("Repository_Password");
        int port = Integer.parseInt(prop.getProperty("Repository_Port"));
        repository = new VirtuosoRepository("jdbc:virtuoso://" + virt_instance + ":" + port + "/charset=UTF-8/log_enable=2", usr, pwd);
        if (openConnection) {
            con = repository.getConnection();
            con.setAutoCommit(false);
        }
        initNamespaces();
    }

    private void initNamespaces() {
        this.namespaces = new HashMap();
        this.namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        this.namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.namespaces.put("owl", "http://www.w3.org/2002/07/owl#");
        this.namespaces.put("xml", "http://www.w3.org/2001/XMLSchema");
    }

    public boolean belongsInRdfSchema(String uri) {
        if (uri == null) {
            return false;
        } else {
            if (uri.startsWith(namespaces.get("rdf"))
                    || uri.startsWith(namespaces.get("rdfs"))
                    || uri.startsWith(namespaces.get("owl"))
                    || uri.startsWith(namespaces.get("xml"))) {
                return true;
            }
            return false;
        }
    }

    public RepositoryConnection getCon() {
        return con;
    }

    public void terminate() {
        try {
            con.close();
            repository.shutDown();
        } catch (RepositoryException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
        }
    }

    @Override
    public void clearGraphContents(String graph, boolean logging) {
        long start = 0;
        if (logging) {
            System.out.println("Deleting contents of: " + graph);
            start = System.currentTimeMillis();
        }
        try {
            con.clear(new URIImpl(graph));
        } catch (RepositoryException ex) {
            Logger.getLogger(SesameVirtRep.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (logging) {
            System.out.println("Done in " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    @Override
    public boolean executeUpdateQuery(String sparul, boolean logging) {
        long start = 0;
        if (logging) {
            System.out.println("QUERY: " + sparul);
            start = System.currentTimeMillis();
        }
        try {
            con.prepareUpdate(QueryLanguage.SPARQL, sparul).execute();
            if (logging) {
                System.out.println("Done in " + (System.currentTimeMillis() - start) + "ms");
            }
            return true;
        } catch (RepositoryException | MalformedQueryException | UpdateExecutionException ex) {
            Logger.getLogger(SesameVirtRep.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public TupleQueryResult executeSparqlQuery(String sparql, boolean logging) {
        try {
            long start = 0;
            if (logging) {
                System.out.println("QUERY: " + sparql);
                start = System.currentTimeMillis();
            }
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
            TupleQueryResult result = tupleQuery.evaluate();
            if (logging) {
                System.out.println("Done in " + (System.currentTimeMillis() - start) + "ms");
            }
            return result;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            System.out.println("During the select query: " + sparql);
            return null;
        }
    }

    @Override
    public long triplesNum(String graph) {
        try {
            if (graph == null) {
                TupleQueryResult res = this.executeSparqlQuery(""
                        + "select count(*)  "
                        + "where { ?s ?p ?o }", false);
                return Long.parseLong(res.next().getValue("callret-0").stringValue());
            } else {
                TupleQueryResult res = this.executeSparqlQuery(""
                        + "select count(*)  "
                        + "from <" + graph.toString() + ">"
                        + "where { ?s ?p ?o }", false);
                return Long.parseLong(res.next().getValue("callret-0").stringValue());
            }
        } catch (QueryEvaluationException ex) {
            System.out.println("Exception: " + ex.getMessage());
            return -1;
        }
    }

    public void exportToFile(String filename, RDFFormat format, String graphSource) throws Exception {
        System.out.println("Exporting graph: " + graphSource.toString());
        RDFWriter writer = Rio.createWriter(format, new OutputStreamWriter(new FileOutputStream(new File(filename))));
        con.export(writer, new URIImpl(graphSource));
    }

    public void importFilePath(String filename, RDFFormat format, String graphDest) throws Exception {
        System.out.println("Importing file: " + filename + " into graph: " + graphDest);
        File f = new File(filename);
        InputStreamReader in = new InputStreamReader(new FileInputStream(f), "UTF-8");
        con.add(in, "", format, new URIImpl(graphDest));
//        con.add(new File(filename), graphDest, format, new URIImpl(graphDest));
        con.commit();
        in.close();
        System.out.println("--- Done ---");
    }

    public void importInputStream(InputStream is, RDFFormat format, String graphDest) throws Exception {
        System.out.println("Importing into graph: " + graphDest);
        InputStreamReader in = new InputStreamReader(is, "UTF-8");
        con.add(in, "", format, new URIImpl(graphDest));
//        con.add(new File(filename), graphDest, format, new URIImpl(graphDest));
        con.commit();
        in.close();
        System.out.println("--- Done ---");
    }

    public void importFromURL(String url, RDFFormat format, String graphDest) throws Exception {
        System.out.println("Importing from: " + url + " into graph: " + graphDest);
        BufferedInputStream input = new BufferedInputStream(new URL(url).openStream());
        InputStreamReader in = new InputStreamReader(input, "UTF-8");
        con.add(in, "", format, new URIImpl(graphDest));
//        con.add(new File(filename), graphDest, format, new URIImpl(graphDest));
        con.commit();
        in.close();
        System.out.println("--- Done ---");
    }

    public void importFolder(String folder, RDFFormat format, String graphDest) throws RepositoryException {
        for (File file : new File(folder).listFiles()) {
            System.out.print("Importing file: " + file.getName() + " into graph: " + graphDest);
            InputStreamReader in;
            long start = System.currentTimeMillis();
            try {
                in = new InputStreamReader(new FileInputStream(file), "UTF8");
                con.add(in, "", format, new URIImpl(graphDest));
                in.close();
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }
            System.out.println(" in " + (System.currentTimeMillis() - start) + " ms");
//            System.out.println("--- Done ---");
        }
        con.commit();
    }

    public void addTriple(String s, String p, String o, String graph) {
        URI sub = repository.getValueFactory().createURI(s);
        URI pred = repository.getValueFactory().createURI(p);
        URI obj = repository.getValueFactory().createURI(o);
        URI g = repository.getValueFactory().createURI(graph);
        try {
            con.add(sub, pred, obj, g);
        } catch (RepositoryException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
        }
    }

    public void addLitTriple(String s, String p, String o, String graph) {
        URI sub = repository.getValueFactory().createURI(s);
        URI pred = repository.getValueFactory().createURI(p);
        Literal obj = repository.getValueFactory().createLiteral(o);
        URI g = repository.getValueFactory().createURI(graph);
        try {
            con.add(sub, pred, obj, g);
        } catch (RepositoryException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
        }
    }

    public void addLitTriple(String s, String p, double o, String graph) {
        URI sub = repository.getValueFactory().createURI(s);
        URI pred = repository.getValueFactory().createURI(p);
        Literal obj = repository.getValueFactory().createLiteral(o);
        URI g = repository.getValueFactory().createURI(graph);
        try {
            con.add(sub, pred, obj, g);
        } catch (RepositoryException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
        }
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public long countSparqlResults(String query) {
        try {
            long result = 0;
            String queryTmp = query.toLowerCase();
            int end = queryTmp.indexOf("from");
            if (end == -1) {
                end = queryTmp.indexOf("where");
            }
            int start = queryTmp.indexOf(" ");
            StringBuilder sb = new StringBuilder();
            sb.append(query.substring(0, start)).append(" (count(*) as ?triples) ").append(query.substring(end));
            TupleQueryResult res = executeSparqlQuery(sb.toString(), false);
            while (res.hasNext()) {
                result = Long.parseLong(res.next().getValue("triples").stringValue());
            }
            res.close();
            return result;
        } catch (QueryEvaluationException ex) {
            System.out.println("Exception: " + ex.getMessage() + " occured .");
        }
        return -1;
    }

    public void importDatasetTest(String filename, RDFFormat format, String graph, int runs) throws Exception {
        long duration = 0;
        System.out.println("-- " + graph + " --");
        long min = Long.MAX_VALUE, max = 0;
        for (int i = 0; i < runs; i++) {
            clearGraphContents(graph, false);
            long curDur = 0;
            long start = System.currentTimeMillis();
            if (new File(filename).isDirectory()) {
                importFolder(filename, format, graph);
            } else {
                importFilePath(filename, format, graph);
            }
            curDur = System.currentTimeMillis() - start;
            if (min > curDur) {
                min = curDur;
            }
            if (max < curDur) {
                max = curDur;
            }
            System.out.println(curDur);
            duration += curDur;
        }
        duration = duration - min - max;
        runs -= 2;
        System.out.println(graph + ": " + triplesNum(graph) + "\t\t\tDuration: " + duration / runs);
        System.out.println("----");
    }

    public static String readData(String filename) {
        File f = new File(filename);
        String s = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                s += (line + "\n");
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage() + " occured .");
            return null;
        }
        return s;
    }

    public static void queryTest(String folder, String graph, int runs, SesameVirtRep sesame) throws SQLException, RepositoryException, QueryEvaluationException, MalformedQueryException {
        for (File file : new File(folder).listFiles()) {
            System.out.println("-- " + file.getName() + " --");
            if (file.isDirectory()) {
                continue;
            }
            long duration = 0;
            long min = Long.MAX_VALUE, max = 0;
            String query = readData(file.getAbsolutePath());
            query = query.replace("[namegraph]", "<" + graph + ">");
            int i;
            for (i = 0; i < runs; i++) {
                long start = System.currentTimeMillis();
                TupleQueryResult res = sesame.executeSparqlQuery(query, false);
                while (res.hasNext()) {
                    res.next();
                }
                res.close();
                long curDur = (System.currentTimeMillis() - start);
                if (min > curDur) {
                    min = curDur;
                }
                if (max < curDur) {
                    max = curDur;
                }
                System.out.println(curDur);
                duration += curDur;
            }
            duration = duration - min - max;
            i -= 2;
            System.out.println(file.getName() + "\t" + duration / i + "\t" + sesame.countSparqlResults(query));
        }
    }
}
