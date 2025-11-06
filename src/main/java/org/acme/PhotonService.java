package org.acme;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.runtime.ByteArrayMemory;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;
import io.quarkus.logging.Log;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;

import static java.nio.charset.StandardCharsets.UTF_8;

@ApplicationScoped
public class PhotonService {

    private int imagePtr;
    private int imageSize;
    private Instance instance;
    private PhotonApi_ModuleExports photonApi;


    public PhotonService() {
        var module = Parser.parse(new File("./src/main/resources/photon_example.wasm"));

        Instance.Builder builder = Instance.builder(module);

        // In dev mode, use runtime compilation so that live reload works
        if (LaunchMode.current() == LaunchMode.NORMAL) {
            builder = builder.withMachineFactory(Photon::create);
        } else {
            builder = builder.withMachineFactory(MachineFactoryCompiler::compile)
                    .withMemoryFactory(ByteArrayMemory::new);
        }

        instance = builder.build();
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
