package gr.forth.ics.isl.aqub.accessServices.services.virtuoso;

import java.util.logging.Level;
import java.util.logging.Logger;
import gr.forth.ics.isl.aqub.virtuoso.SesameVirtRep;
import gr.forth.ics.isl.aqub.accessServices.utils.PropertiesManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Path("update/virtuoso")
public class UpdateServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private SesameVirtRep virtuoso;

    public UpdateServices() {
    }

    @PostConstruct
    public void initialize() {
        System.out.println("### initialize UPDATE services ()");
        Properties prop = propertiesManager.getProperties();
        try {
            virtuoso = new SesameVirtRep(
                    prop.getProperty("virtuoso.url"),
                    Integer.parseInt(prop.getProperty("virtuoso.port")),
                    prop.getProperty("virtuoso.username"),
                    prop.getProperty("virtuoso.password"));
        } catch (Exception ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateExecPOSTJSON(String jsonInput,
            @DefaultValue("") @QueryParam("token") String token) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        JSONObject result = new JSONObject();
        if (jsonObject.size() != 1) {
            result.put("message", "JSON input message should have exactly 1 argument.");
            return Response.status(400).entity(result.toJSONString()).header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            return updateExecVirtuoso(q, result);
        }
    }

    private Response updateExecVirtuoso(String q, JSONObject objectJSON) throws IOException, UnsupportedEncodingException, ParseException {
        System.out.println("--using virtuoso rest api --");

        boolean isTokenValid = true;
        int statusInt;
        if (!isTokenValid) {
            objectJSON.put("message", "User not authenticated!");
            statusInt = 401;
        } else {
            boolean result = virtuoso.executeUpdateQuery(q, false);
            if (result) {
                objectJSON.put("message", "Update query was applied successfully.");
                statusInt = 200;
            } else {
                statusInt = 500;
                objectJSON.put("message", "Error during update query: " + q);
            }
        }
        virtuoso.terminate();
        return Response.status(statusInt).entity(objectJSON.toJSONString()).header("Access-Control-Allow-Origin", "*").build();
    }

}
