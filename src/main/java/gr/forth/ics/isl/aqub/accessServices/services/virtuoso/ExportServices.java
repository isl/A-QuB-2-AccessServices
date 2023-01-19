package gr.forth.ics.isl.aqub.accessServices.services.virtuoso;

import gr.forth.ics.isl.aqub.accessServices.utils.PropertiesManager;
import gr.forth.ics.isl.aqub.virtuoso.SesameVirtRep;
import gr.forth.ics.isl.aqub.virtuoso.Utils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

@Path("export/virtuoso")
public class ExportServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    String namespace = propertiesManager.getTripleStoreNamespace();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private SesameVirtRep virtuoso;

    public ExportServices() {
    }

    @PostConstruct
    public void initialize() {
        System.out.println("### initialize EXPORT services ()");
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

    @GET
    public Response exportFileGETJSON(@QueryParam("graph") String graph,
            @QueryParam("format") String contentType,
            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
        String namespace = this.namespace;
        return execExportGET(token, contentType, graph, namespace);
    }

    public Response execExportGET(String token, String contentType, String graph, String namespace) throws UnsupportedEncodingException {
        System.out.println("--using virtuoso rest api --");
        int status;
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }

        boolean isTokenValid = true;
        String message;
        if (!isTokenValid) {
            message = "User not authenticated!";
            status = 401;
        } else if (contentType == null) {
            status = 400;
            message = "Error in the provided format.";
        } else {
            RDFFormat format = Utils.RDFFormatfromString(contentType);
            OutputStream output = new OutputStream() {
                private StringBuilder string = new StringBuilder();

                @Override
                public void write(int b) throws IOException {
                    this.string.append((char) b);
                }

                public String toString() {
                    return this.string.toString();
                }
            };
            try {
                RDFWriter writer = Rio.createWriter(format, output);          
                virtuoso.getCon().export(writer, new URIImpl(graph));
                return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin", "*").build();
            } catch (Exception ex) {
                message = ex.getMessage();
                status = 500;
            }
        }

        virtuoso.terminate();
        JSONObject result = new JSONObject();
        result.put("message", message);
        return Response.status(status).entity(result.toJSONString()).header("Access-Control-Allow-Origin", "*").build();
    }
}
