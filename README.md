# quarkus-photon-demo

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

This is a demo application that shows how to re-use, in pure Java a Rust library thanks to WebAssembly.

## Build

You need a valid Rust toolchain installed.

First you need to compile the Rust part of the program:

Install the correct target(only once):

```bash
rustup target add wasm32-unknown-unknown
```

and compile the example:

```bash
cd photon
make build
```

## Notes

The original Photon library is available here:
https://github.com/silvia-odwyer/photon

and this is an online demo doing the same but client-side only:
https://silvia-odwyer.github.io/photon/demo.html

- The effects are applied to the originally uploaded image
- Native image works out of the box



## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

Live reload works for the Java code.
For example, if you edit the colours in `filters.rs` and then run `cd photon; make build`, the new colours will be applied.

Note that the backend is not stateless, so you will need to upload the image in the frontend each time.