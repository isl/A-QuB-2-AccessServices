package gr.forth.ics.isl.aqub.accessServices.clients.examples;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;

public class UpdateTest {

    public static void main(String[] args) throws UnsupportedEncodingException, ParseException {
        String baseURI = "http://localhost:8084//A-QuB-2-AccessServices";

        Client client = ClientBuilder.newClient();

        String update = "insert data {graph <http://test1111> {<http://a99> <http://p3> <http://b3>.} }";

        System.out.println();
        System.out.println("Executing the update query: " + update);

        JSONObject json = new JSONObject();
        json.put("query", update);
        WebTarget webTarget = client.target(baseURI + "/update/virtuoso");
        Response updateResponse = webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", null).post(Entity.json(json.toJSONString()));

        JSONParser parser = new JSONParser();
        JSONObject message = (JSONObject) parser.parse(updateResponse.readEntity(String.class));
        System.out.println("Update executed, return message is: " + message.get("message"));
        client.close();
    }

}
