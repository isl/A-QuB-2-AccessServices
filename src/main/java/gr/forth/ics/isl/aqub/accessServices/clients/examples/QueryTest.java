package gr.forth.ics.isl.aqub.accessServices.clients.examples;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.json.simple.parser.ParseException;

public class QueryTest {

    public static void main(String[] args) throws UnsupportedEncodingException, ParseException {

        String baseURI = "http://localhost:8080//A-QuB-2-AccessServices";
        Client client = ClientBuilder.newClient();

        String query = "select * where {?s ?p ?o} limit 1";

        System.out.println();
        System.out.println("Executing the query: " + query);

        WebTarget webTarget = client.target(baseURI + "/query/virtuoso").
                queryParam("format", "application/json").//mimetype
                queryParam("query", URLEncoder.encode(query, "UTF-8").
                        replaceAll("\\+", "%20"));
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", null);
        Response queryResponse = invocationBuilder.get();

        System.out.println("Response code: " + queryResponse.getStatus());
        System.out.println("Query executed, return message is: " + queryResponse.readEntity(String.class));

        client.close();
    }

}
