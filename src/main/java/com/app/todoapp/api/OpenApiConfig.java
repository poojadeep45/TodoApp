package com.app.todoapp.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

@OpenAPIDefinition(
        info = @Info(
                title = "Todo Application API",
                version = "1.0",
                description = "REST API for managing tasks. All endpoints require HTTP Basic auth " +
                        "using the same username/password you registered with in the web app. " +
                        "Every task is scoped to the authenticated user."
        )
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class OpenApiConfig {
}