package org.acme;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/example")
public class ExampleResource {

    @Inject
    ExampleService service;

    @GET
    @Path("/calc")
    public Response calc(
            @QueryParam("value") Long value) {
        Long result = service.compute(value);
        Log.info("Result: " + result);
        return Response.ok().entity(result).build();
    }
}
