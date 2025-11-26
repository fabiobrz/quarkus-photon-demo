package org.acme;

import com.dylibso.chicory.wasm.Parser;
import io.quarkiverse.chicory.runtime.wasm.ExecutionMode;
import io.quarkiverse.chicory.runtime.wasm.Wasm;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Path("/example")
public class ExampleResource {

    @Inject
    ExampleService service;

    @GET
    @Path("/calc")
    public Response calc(@QueryParam("value") Long value) {
        Long result = service.compute(value);
        Log.info("Result: " + result);
        return Response.ok().entity(result).build();
    }

    @POST
    @Path("/wasm/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@RestForm("module") FileUpload wasmModule,
                           @RestForm("execution-mode") ExecutionMode executionMode) throws IOException {
        try (final InputStream wasmModuleInputStream = Files.newInputStream(wasmModule.uploadedFile())) {
            if (wasmModuleInputStream.available() <= 0) {
                throw new IllegalArgumentException("ERROR: Wasm module NOT uploaded 0");
            }
            Wasm added = service.uploadWasm(wasmModuleInputStream, executionMode == null ? ExecutionMode.Interpreter : executionMode);
            Log.info("Wasm module uploaded");
            return Response.accepted(added).build();
        }
    }
}
