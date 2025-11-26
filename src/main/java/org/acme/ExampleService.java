package org.acme;

import com.dylibso.chicory.wasm.Parser;
import io.quarkiverse.chicory.runtime.wasm.ExecutionMode;
import io.quarkiverse.chicory.runtime.wasm.Wasm;
import io.quarkiverse.chicory.runtime.wasm.Wasms;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class ExampleService {

    private static final String DYNAMIC_WASM_MODULE_NAME = "example";

    @Inject
    Wasms wasms;

    public long compute(Long content) {
        var dynamicWasm = wasms.get(DYNAMIC_WASM_MODULE_NAME);
        if (dynamicWasm == null) {
            throw new IllegalStateException(String.format("Wasm module %s not found", DYNAMIC_WASM_MODULE_NAME));
        }
        var instance = dynamicWasm.chicoryInstance();
        var exportedFunction = instance.export("exported_function");
        var result = exportedFunction.apply(content);

        return result[0];
    }

    public Wasm uploadWasm(InputStream wasmModuleInputStream, ExecutionMode executionMode) throws IOException {
        return wasms.add(Wasm.builder(
                        DYNAMIC_WASM_MODULE_NAME, Parser.parse(wasmModuleInputStream.readAllBytes()))
                .withMode(executionMode)
                .build());
    }
}
