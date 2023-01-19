package gr.forth.ics.isl.aqub.accessServices.services.virtuoso;

import gr.forth.ics.isl.aqub.accessServices.utils.PropertiesManager;
import gr.forth.ics.isl.aqub.virtuoso.RestVirtRep;
import gr.forth.ics.isl.aqub.virtuoso.SesameVirtRep;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.repository.RepositoryException;

@Path("query/virtuoso")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class QueryServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private SesameVirtRep virtuoso;
    private RestVirtRep restVirtuoso;

    public QueryServices() {
    }

    @PostConstruct
    public void initialize() {
        Properties prop = propertiesManager.getProperties();
        try {
            virtuoso = new SesameVirtRep(
                    prop.getProperty("virtuoso.url"),
                    Integer.parseInt(prop.getProperty("virtuoso.port")),
                    prop.getProperty("virtuoso.username"),
                    prop.getProperty("virtuoso.password"));
        } catch (RepositoryException ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
        }
        restVirtuoso = new RestVirtRep(prop.getProperty("virtuoso.rest.url"));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response queryExecGETJSON(
            @DefaultValue("application/json") @QueryParam("format") String f,
            @QueryParam("query") String q,
            @DefaultValue("0") @QueryParam("timeout") int timeout,
            @DefaultValue("") @QueryParam("token") String token) throws IOException {
        System.out.println("### queryExecGETJSON ###");
        JSONObject message = new JSONObject();
        return queryExecVirtuoso(timeout, f, q, message);
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response queryCountExecGETJSON(
            @DefaultValue("application/json") @QueryParam("format") String f,
            @QueryParam("query") String q,
            @DefaultValue("0") @QueryParam("timeout") int timeout,
            @DefaultValue("") @QueryParam("token") String token) throws IOException {
        System.out.println("### queryCountExecGETJSON ###");
        String authToken = requestContext.getHeader("Authorization");

        if (authToken == null) {
            authToken = token;
        }

        return queryExecVirtuoso(timeout, f, ConvertToCountQuery(q), new JSONObject());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response queryExecPOSTJSON(String jsonInput,
            @DefaultValue("") @QueryParam("token") String token) throws IOException, ParseException {
        System.out.println("### queryExecPOSTJSON ###");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String authToken = requestContext.getHeader("Authorization");

        if (authToken == null) {
            authToken = token;
        }

        JSONObject result = new JSONObject();
        if (jsonObject.size() != 2) {
            result.put("message", "JSON input message should have exactly 2 arguments.");
            return Response.status(400).entity(result.toJSONString()).
                    //header("Content-Type", "UTF-8").
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            int timeout = 0;
            if (jsonObject.get("timeout") == null) {
                timeout = 0;
            } else {
                timeout = (int) jsonObject.get("timeout");
            }
            String f = (String) jsonObject.get("format");

            return queryExecVirtuoso(timeout, f, q, result);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/count")
    public Response queryCountExecPOSTJSON(String jsonInput,
            @DefaultValue("") @QueryParam("token") String token) throws IOException, ParseException {
        System.out.println("### queryCountExecPOSTJSON ###");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String authToken = requestContext.getHeader("Authorization");

        JSONObject result = new JSONObject();
        if (authToken == null) {
            authToken = token;
        }

        if (jsonObject.size() != 2) {
            result.put("message", "JSON input message should have exactly 2 arguments.");
            return Response.status(400).entity(result.toJSONString()).
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            String q = (String) jsonObject.get("query");
            String f = (String) jsonObject.get("format");
            int timeout;
            if (jsonObject.get("timeout") == null) {
                timeout = 0;
            } else {
                timeout = (int) jsonObject.get("timeout");
            }
            return queryExecVirtuoso(timeout, f, ConvertToCountQuery(q), result);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Path("/batch")
    public Response batchQueryExecPOSTJSONWithNS(String jsonInput,
            @DefaultValue("") @QueryParam("token") String token) throws IOException, ParseException {
        System.out.println("### batchQueryExecPOSTJSONWithNS ###");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        JSONArray result = new JSONArray();
        JSONObject resultJSON = new JSONObject();
        int status = 0;
        if (jsonObject.size() != 2) {
            resultJSON.put("message", "JSON input message should have exactly 2 arguments.");
            return Response.status(400).entity(resultJSON.toJSONString()).
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            String queriesStr = (String) jsonObject.get("query");
            JSONArray queries = (JSONArray) jsonParser.parse(queriesStr);
            String f = (String) jsonObject.get("format");
            for (int i = 0; i < queries.size(); i++) {
                String query = (String) queries.get(i);
                restVirtuoso.setTimeout(0);
                Response resp = restVirtuoso.executeSparqlQuery(query, f);
                status = resp.getStatus();
                String data = resp.readEntity(String.class);
                if (status != 200) {
                    return Response.status(status).entity(resp.readEntity(String.class)).
                            header("Access-Control-Allow-Origin", "*").build();
                }
                result.add(data);
            }
        }
        return Response.status(status).entity(result.toJSONString()).
                header("Access-Control-Allow-Origin", "*").build();
    }

    public static String ConvertToCountQuery(String query) {
        String queryTmp = query.toLowerCase();
        int end = queryTmp.indexOf("from");
        if (end == -1) {
            end = queryTmp.indexOf("where");
        }
        int selectStart = queryTmp.indexOf("select");
        int distinctStart = queryTmp.indexOf("distinct");
        StringBuilder finalQuery = new StringBuilder();
        if (distinctStart != -1) {
            finalQuery.append(queryTmp.substring(0, distinctStart));
            finalQuery.append(" (count(distinct *) as ?count) ").append(query.substring(end));
        } else {
            finalQuery.append(queryTmp.substring(0, selectStart + "select".length()));
            finalQuery.append(" (count(*) as ?count) ").append(query.substring(end));
        }
        return finalQuery.toString();
    }

    private Response queryExecVirtuoso(int timeout, String f, String q, JSONObject result) throws IOException, UnsupportedEncodingException {
        boolean isTokenValid = true;
        System.out.println("--using virtuoso rest api --");
        System.out.println(q);
        int statusInt;
        Response response = null;
        String responseData = "";
        if (!isTokenValid) {
            System.out.println("!!! User not authenticated!");
            result.put("message", "User not authenticated!");
            statusInt = 401;
        } else if (f == null) {
            System.out.println("!!! Error in the provided format!");
            result.put("message", "Error in the provided format.");
            statusInt = 500;
        } else {
            System.out.println("  OK!");
            restVirtuoso.setTimeout(timeout);
            response = restVirtuoso.executeSparqlQuery(q, f);
            statusInt = response.getStatus();
            responseData = response.readEntity(String.class);
        }
        if (statusInt == 200) {
            result.put("message", "Query was executed successfully.");
        }
        virtuoso.terminate();
        if (statusInt == 200) {
            return Response.status(statusInt).entity(responseData).
                    header("Access-Control-Allow-Origin", "*").build();
        } else {
            return Response.status(statusInt).entity(result.toJSONString()).
                    header("Access-Control-Allow-Origin", "*").
                    build();
        }
    }

}
