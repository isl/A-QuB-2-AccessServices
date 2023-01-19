package gr.forth.ics.isl.aqub.accessServices.services;

import java.util.Set;
import javax.ws.rs.core.Application;

@javax.ws.rs.ApplicationPath("")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {

        resources.add(gr.forth.ics.isl.aqub.accessServices.services.InfoService.class);
        resources.add(gr.forth.ics.isl.aqub.accessServices.services.virtuoso.ExportServices.class);
        resources.add(gr.forth.ics.isl.aqub.accessServices.services.virtuoso.ImportServices.class);
        resources.add(gr.forth.ics.isl.aqub.accessServices.services.virtuoso.QueryServices.class);
        resources.add(gr.forth.ics.isl.aqub.accessServices.services.virtuoso.UpdateServices.class);
    }

}
