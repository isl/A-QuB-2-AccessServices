package gr.forth.ics.isl.aqub.virtuoso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.json.JSONObject;


public class RestVirtRep {

    private String endpointURL;
    private int timeout;

    public RestVirtRep(String endpointURL) {
        this.endpointURL = endpointURL;
        this.timeout = 0;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public String importFileData(String fileContentStr, String mimetypeFormat, String namespace, String namedGraph)
            throws IOException {
        return null;
    }

    public Response executeSparqlQuery(String queryStr, QueryResultFormat format) throws UnsupportedEncodingException {
        String mimetype = Utils.fetchQueryResultMimeType(format);
        return executeSparqlQuery(queryStr, mimetype);

    }

    public Response executeSparqlQuery(String queryStr, String mimetypeFormat) throws UnsupportedEncodingException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(endpointURL + "/sparql")
                .queryParam("query", URLEncoder.encode(queryStr, "UTF-8").replaceAll("\\+", "%20"))
                .queryParam("timeout", "" + this.timeout);
        Invocation.Builder invocationBuilder = webTarget.request(mimetypeFormat);
        Response response = invocationBuilder.get();
        return response;
    }

    public long triplesNum(String graph) throws UnsupportedEncodingException {
        String query = "select (count(*) as ?count) from <" + graph + "> where {?s ?p ?o}";
        Response response = executeSparqlQuery(query, QueryResultFormat.JSON);
        JSONObject json = new JSONObject(response.readEntity(String.class));
        JSONObject count = (JSONObject) json.getJSONObject("results").getJSONArray("bindings").get(0);
        return count.getJSONObject("count").getLong("value");
    }

    public long countSparqlResults(String query, String namespace) throws Exception {
        String queryTmp = query.toLowerCase();
        int end = queryTmp.indexOf("from");
        if (end == -1) {
            end = queryTmp.indexOf("where");
        }
        int start = queryTmp.indexOf(" ");
        StringBuilder sb = new StringBuilder();
        sb.append(query.substring(0, start)).append(" (count(*) as ?count) ").append(query.substring(end));
        JSONObject json = new JSONObject(executeSparqlQuery(sb.toString(), QueryResultFormat.JSON).readEntity(String.class));
        JSONObject count = (JSONObject) json.getJSONObject("results").getJSONArray("bindings").get(0);
        return count.getJSONObject("count").getLong("value");
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        String url = "http://139.91.183.97:8890/sparql";
        RestVirtRep virt = new RestVirtRep(url);

        String query = "...";

        Response resp = virt.executeSparqlQuery(query, QueryResultFormat.JSON);
        System.out.println(resp.readEntity(String.class));
    }

}
