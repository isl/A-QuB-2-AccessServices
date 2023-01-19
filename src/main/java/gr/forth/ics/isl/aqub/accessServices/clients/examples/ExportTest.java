package gr.forth.ics.isl.aqub.accessServices.clients.examples;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.json.simple.parser.ParseException;

public class ExportTest {

    public static void main(String[] args) throws UnsupportedEncodingException, ParseException {
        String baseURI = "http://localhost:8084//A-QuB-2-AccessServices";

        Client client = ClientBuilder.newClient();

        String graph = "http://exampleGraph/";
        System.out.println();
        System.out.println("Exporting data from graph: " + graph);
        String format = "text/plain";
        WebTarget webTarget = client.target(baseURI).path("export");
        webTarget = webTarget.path("virtuoso");
        webTarget = webTarget.queryParam("format", format).//mimetype
                queryParam("graph", graph);
        Invocation.Builder invocationBuilder = webTarget.request().
                header("Authorization", null);//.request(mimetype);
        Response response = invocationBuilder.get();

        System.out.println("Export executed! Status code: " + response.getStatus());
        System.out.println("Result:\n" + response.readEntity(String.class));
        client.close();
    }

}
