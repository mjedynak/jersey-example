package pl.mjedynak.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/resource")
public class Resource {

    private static final Logger logger = LoggerFactory.getLogger(Resource.class);

    @GET
    @Produces("text/plain")
    public String getResource() {
        logger.info("request for resource");
        return "some resource";
    }
}
