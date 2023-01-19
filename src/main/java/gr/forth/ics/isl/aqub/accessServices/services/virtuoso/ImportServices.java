package gr.forth.ics.isl.aqub.accessServices.services.virtuoso;

import gr.forth.ics.isl.aqub.accessServices.utils.PropertiesManager;
import gr.forth.ics.isl.aqub.virtuoso.SesameVirtRep;
import gr.forth.ics.isl.aqub.virtuoso.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONObject;
import org.openrdf.rio.RDFFormat;

@Path("import/virtuoso")
public class ImportServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private SesameVirtRep virtuoso;

    @PostConstruct
    public void initialize() {
        System.out.println("### initialize IMPORT service ()");
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
    public Response importFileContentsPOSTJSON(InputStream incomingData,
            @QueryParam("graph") String graph,
            @HeaderParam("content-type") String contentType,
            @DefaultValue("") @QueryParam("token") String token) throws ClientProtocolException, IOException {

        System.out.println("--using virtuoso rest api --");

        boolean isTokenValid = true;
        int status = 0;
        JSONObject result = new JSONObject();
        String message;
        if (!isTokenValid) {
            message = "User not authenticated!";
            status = 401;
        } else {
            try {
                System.out.println("Content type: " + contentType);
                RDFFormat format = Utils.RDFFormatfromString(contentType);
                System.out.println("Format: " + format);
                System.out.println("GRAPH: " + graph);

                virtuoso.importInputStream(incomingData, format, graph);
                status = 200;
                message = "Data were inserted successfully.";
            } catch (Exception ex) {
                message = ex.getMessage();
                System.out.println("ERROR: " + ex.getMessage());
                ex.printStackTrace();
                status = 500;
            }
        }
        result.put("message", message);
        virtuoso.terminate();
        return Response.status(status).entity(result.toString()).header("Access-Control-Allow-Origin", "*").build();
    }

}
