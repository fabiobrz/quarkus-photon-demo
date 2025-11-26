package org.acme;

import io.quarkiverse.chicory.runtime.wasm.Wasms;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/wasm")
public class WasmResource {

    @Inject
    Wasms wasms;

    @GET
    @Path("/all")
    public Response getAll() {
        return Response.ok().entity(wasms.all()).build();
    }
}
