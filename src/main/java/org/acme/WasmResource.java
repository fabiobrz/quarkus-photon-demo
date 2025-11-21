package org.acme;

import com.dylibso.chicory.wasm.Parser;
import io.quarkiverse.chicory.runtime.WasmModuleContext;
import io.quarkiverse.chicory.runtime.WasmModuleContextRegistry;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/wasm")
public class WasmResource {

    @Inject
    WasmModuleContextRegistry wasmModuleContextRegistry;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@RestForm("module") FileUpload wasmModule, @RestForm("id") String id) throws IOException {
        try (final InputStream wasmModuleInputStream = Files.newInputStream(wasmModule.uploadedFile())) {
            if (wasmModuleInputStream.available() <= 0) {
                throw new IllegalArgumentException("ERROR: Wasm module NOT uploaded 0");
            }
            wasmModuleContextRegistry.add(
                    WasmModuleContext.builder(id, Parser.parse(wasmModuleInputStream.readAllBytes()))
                            .build()
            );
            Log.info("Wasm module uploaded");
            return Response.ok().build();
        }
    }

    @GET
    @Path("/wasm-module-context/all")
    public Response getAllModuleContexts() {
        return Response.ok().entity(wasmModuleContextRegistry.all()).build();
    }
}
