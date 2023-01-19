package gr.forth.ics.isl.aqub.virtuoso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import javax.ws.rs.core.Response;
import org.openrdf.rio.RDFFormat;

public class Utils {

    public static String fetchDataImportMimeType(RDFFormat format) {
        String mimeType;
        if (format == RDFFormat.RDFXML) {
            mimeType = "application/rdf+xml";
        } else if (format == RDFFormat.N3) {
            mimeType = "text/rdf+n3";
        } else if (format == RDFFormat.NTRIPLES) {
            mimeType = "text/plain";
        } else if (format == RDFFormat.TURTLE) {
            mimeType = "application/x-turtle";
        } else if (format == RDFFormat.TRIG) {
            mimeType = "application/x-trig";
        } else if (format == RDFFormat.NQUADS) {
            mimeType = "text/x-nquads";
        } else {
            mimeType = null;
        }
        return mimeType;
    }

    public static String fetchQueryResultMimeType(QueryResultFormat format) {
        String mimetype = "";
        switch (format) {
            case CSV:
                mimetype = "text/csv";
                break;
            case JSON:
                mimetype = "application/json";
                break;
            case TSV:
                mimetype = "text/tab-separated-values";
                break;
            case XML:
                mimetype = "application/sparql-results+xml";
                break;
        }
        return mimetype;
    }

    public static QueryResultFormat QueryResultFormatfromString(String format) {
        if (format != null) {
            for (QueryResultFormat b : QueryResultFormat.values()) {
                if (format.equalsIgnoreCase(b.toString())) {
                    return b;
                }
            }
        }
        return null;
    }

    public static RDFFormat RDFFormatfromString(String format) {
        if (format != null) {
            switch (format) {
                case "application/x-trig":
                    return RDFFormat.TRIG;
                case "application/rdf+xml":
                    return RDFFormat.RDFXML;
                case "text/plain":
                    return RDFFormat.NTRIPLES;
                case "text/rdf+n3":
                    return RDFFormat.N3;
                case "application/x-turtle":
                    return RDFFormat.TURTLE;
                case "text/x-nquads":
                    return RDFFormat.NQUADS;
            }
        }
        return null;
    }

    public static boolean saveResponseToFile(String filename, Response resp) {
        InputStream input = resp.readEntity(InputStream.class);
        new File(filename).delete();
        try {
            Files.copy(input, new File(filename).toPath());
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public static String readFileData(String filename) {
        File f = new File(filename);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage() + " occured .");
            return null;
        }
        return sb.toString();
    }
}
