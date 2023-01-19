package gr.forth.ics.isl.aqub.accessServices.services;

import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("")
public class InfoService {

    @Context
    private ServletContext context;
    @Context
    private UriInfo uri;

    public InfoService() {
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getHtml() throws MalformedURLException {
        System.out.println("URI: " + uri.getAbsolutePath());

        return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n"
                + "   \"http://www.w3.org/TR/html4/loose.dtd\">\n"
                + "\n"
                + "<!DOCTYPE HTML>\n"
                + "<html>\n"
                + "<head> \n"
                + "    <title>A-QuB-2 | Access Services</title> \n"
                + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "</head>\n"
                + "<body>\n"
                + "<div class=\"colmask fullpage\">\n"
                + "	<h2>Welcome to the A-QuB-2 Access Services</h2>\n"
                + "	<p >If you can see this page the services have been correctly deployed and it should be possible to use the supported services. \n"
                + "	</p>\n"
                + "	<p>\n"
                + "	</p>\n"
                + "	\n"
                + "	\n"
                + "</div>\n"
                + "	 \n"
                + "</body>\n"
                + "</html>\n";
    }

}
