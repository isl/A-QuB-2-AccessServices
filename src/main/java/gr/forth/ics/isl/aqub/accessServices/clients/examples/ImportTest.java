package gr.forth.ics.isl.aqub.accessServices.clients.examples;

import gr.forth.ics.isl.aqub.virtuoso.Utils;
import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class ImportTest {

    public static void main(String[] args) throws IOException {

        String baseURI = "http://localhost:8080//A-QuB-2-AccessServices";
        Client client = ClientBuilder.newClient();

        String rdfFile = "C:\\Users\\Pavlos\\Desktop\\data\\Version-2020-11\\Crew List (Ruoli di Equipaggio), Adelaide, 1862-12-01, Carolina Gaggero\\Crew List _Ruoli di Equipaggio__ Adelaide_ 1862_12_01_ Carolina  Gaggero .trig";
        String restURL = baseURI + "/import/virtuoso";
        String namedGraph = "http://graph/sealit";
        restURL = restURL + "?graph=" + namedGraph;

        WebTarget webTarget = client.target(restURL);
        Response importResponse = webTarget.request().header("Authorization", null).post(Entity.entity(Utils.readFileData(rdfFile), "text/plain"));

        System.out.println("Response code: " + importResponse.getStatus());
        System.out.println("Import executed, return message is: " + importResponse.readEntity(String.class));
        client.close();
    }

}
