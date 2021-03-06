## Publishing to maven local

`:wkd-api` and `:wkd-client` can be published to maven local by running:

```shell
# Optionally clean the build
./gradlew clean

# Build (if required) and publish
./gradlew publishToMavenLocal

# Verify the artifacts exist in maven local
ls ~/.m2/repository/com/innercirclesoftware/wkd/
```

## OpenAPI/Swagger

swagger-ui is available under `/swagger/views/swagger-ui`. When running locally it is
at http://localhost:8080/swagger/views/swagger-ui

Other OpenAPI views, such as `redoc` and `rapidoc` can be enabled by editing `/wkd-server/build.gradle` as such:

```
kapt {
    arguments {
        arg("micronaut.openapi.views.spec", "redoc.enabled=true,rapidoc.enabled=true,swagger-ui.enabled=true,swagger-ui.theme=flattop")
    }
}
```