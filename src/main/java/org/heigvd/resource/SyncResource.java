package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

@Path("/sync")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Authenticated
public class SyncResource {

    @POST
    public Response sync(SecurityContext ctx) {
        return Response.ok().build();
    }

}
