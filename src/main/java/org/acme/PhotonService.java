package org.acme;

import com.dylibso.chicory.runtime.Instance;
import io.quarkiverse.chicory.runtime.WasmModuleContext;
import io.quarkiverse.chicory.runtime.WasmModuleContextRegistry;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static java.nio.charset.StandardCharsets.UTF_8;

@ApplicationScoped
public class PhotonService {

    public static final String WASM_MODULE_CONTEXT_NAME = "org.acme.Photon";

    private int imagePtr;
    private int imageSize;
    private Instance instance;
    private PhotonApi_ModuleExports photonApi;

    @Inject
    WasmModuleContextRegistry wasmModuleContextRegistry;

    @PostConstruct
    public void init() {
        WasmModuleContext wasmModuleContext = wasmModuleContextRegistry.get(WASM_MODULE_CONTEXT_NAME);
        if (wasmModuleContext == null) {
            throw new IllegalStateException(String.format("WasmModuleContext %s not found", WASM_MODULE_CONTEXT_NAME));
        }
        instance = wasmModuleContext.instance();
        photonApi = new PhotonApi_ModuleExports(instance);
    }

    public void setImage(byte[] image) {
        imagePtr = photonApi.alloc(image.length);
        instance.memory().write(imagePtr, image);
        imageSize = image.length;
    }

    public byte[] applyEffect(String name) {
        Log.info("Applying effect " + name);
        var effectBytes = name.getBytes(UTF_8);
        var effectPtr = photonApi.alloc(effectBytes.length);
        instance.memory().write(effectPtr, effectBytes);

        var outPtr = photonApi.alloc(4);
        var outLen = photonApi.alloc(4);

        var result = photonApi.applyEffect(
                imagePtr,
                imageSize,
                effectPtr,
                effectBytes.length,
                outPtr,
                outLen);

        assert result == 0;

        var resultImg = instance.memory().readBytes(
                instance.memory().readInt(outPtr),
                instance.memory().readInt(outLen));

        // cleanup
        photonApi.dealloc(
                instance.memory().readInt(outPtr),
                instance.memory().readInt(outLen));

        photonApi.dealloc(outPtr, 4);
        photonApi.dealloc(outLen, 4);
        photonApi.dealloc(effectPtr, effectBytes.length);

        return resultImg;
    }

    public byte[] applyTransformation(String name) {
        Log.info("Applying transformation " + name);
        var effectBytes = name.getBytes(UTF_8);
        var effectPtr = photonApi.alloc(effectBytes.length);
        instance.memory().write(effectPtr, effectBytes);

        var outPtr = photonApi.alloc(4);
        var outLen = photonApi.alloc(4);

        var result = photonApi.applyTransformation(
                imagePtr,
                imageSize,
                effectPtr,
                effectBytes.length,
                outPtr,
                outLen);

        assert result == 0;

        var resultImg = instance.memory().readBytes(
                instance.memory().readInt(outPtr),
                instance.memory().readInt(outLen));

        // cleanup
        photonApi.dealloc(
                instance.memory().readInt(outPtr),
                instance.memory().readInt(outLen));

        photonApi.dealloc(outPtr, 4);
        photonApi.dealloc(outLen, 4);
        photonApi.dealloc(effectPtr, effectBytes.length);

        return resultImg;
    }
}
