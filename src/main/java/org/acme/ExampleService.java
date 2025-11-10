package org.acme;

import io.quarkiverse.chicory.runtime.WasmModuleContext;
import io.quarkiverse.chicory.runtime.WasmModuleContextRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ExampleService {

    public static final String WASM_MODULE_CONTEXT_NAME = "example";
    @Inject
    WasmModuleContextRegistry wasmModuleContextRegistry;
    private WasmModuleContext wasmModuleContext;

    @PostConstruct
    public void init() {
        wasmModuleContext = wasmModuleContextRegistry.getModuleContextById(WASM_MODULE_CONTEXT_NAME);
        if (wasmModuleContext == null) {
            throw new IllegalStateException(String.format("WasmModuleContext %s not found", WASM_MODULE_CONTEXT_NAME));
        }
    }

    public long compute(Long content) {
        var instance = wasmModuleContext.getInstance();
        var exportedFunction = instance.export("exported_function");
        var result = exportedFunction.apply(content);

        return result[0];
    }
}
