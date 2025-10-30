
.PHONY: clean
clean:
	rm -rf photon-example/target

.PHONY: build
build:
	cd photon-example && cargo build --release --target wasm32-unknown-unknown
	rm -f ../wasm/photon_example.wasm
	cp photon-example/target/wasm32-unknown-unknown/release/photon_example.wasm ../wasm/photon_example.wasm
