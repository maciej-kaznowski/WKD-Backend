## Micronaut 3.1.4 Documentation

- [User Guide](https://docs.micronaut.io/3.1.4/guide/index.html)
- [API Reference](https://docs.micronaut.io/3.1.4/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/3.1.4/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

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