package it.fcambi.news.ws.server;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Created by Francesco on 28/09/15.
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        MultivaluedMap<String, Object> headers = containerResponseContext.getHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
    }

//    @Override
//    public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {
//        containerResponse.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
//        containerResponse.getHttpHeaders().add("Access-Control-Allow-Methods", "PUT,GET,POST,DELETE,OPTIONS");
//        containerResponse.getHttpHeaders().add("Access-Control-Allow-Headers", "Content-Type");
//        return containerResponse;
//    }


}
