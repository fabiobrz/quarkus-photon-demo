package org.acme;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Path("/photon")
public class PhotonResource {

    @Inject
    PhotonService service;

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response upload(InputStream image) throws IOException {
        try (image) {
            if (image.available() <= 0) {
                throw new IllegalArgumentException("ERROR: Image NOT uploaded 0");
            }
            service.setImage(image.readAllBytes());
            Log.info("Image uploaded");
            return Response.ok().build();
        }
    }

    @GET
    @Path("/effect")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] applyEffect(@QueryParam("effect") String effect) {
        return service.applyEffect(effect);
    }

    @GET
    @Path("/transform")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] applyTransformation(@QueryParam("transformation") String transformation) {
        return service.applyTransformation(transformation);
    }
}
