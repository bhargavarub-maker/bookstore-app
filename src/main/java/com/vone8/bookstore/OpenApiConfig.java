package com.vone8.bookstore;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Title, description, server URL and login scheme shown in Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookstoreOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bookstore API")
                        .version("1.0.0")
                        .contact(new Contact().name("Vone8").url("https://vone8.com"))
                        .description("""
                                Sample backend for the Apigee training. Two REST APIs share one app:
                                **Books** (the catalog) and **Orders** (customer purchases).
                                Later in the course this same API is placed behind an Apigee proxy.

                                Every call needs **HTTP Basic** authentication: click **Authorize** and enter
                                the username and password (default `student` / `bookstore123`)."""))
                // Adds the Authorize button; Swagger UI then sends the Authorization header for you
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .servers(List.of(new Server().url("/").description("This server (localhost or Cloud Run)")))
                .tags(List.of(
                        new Tag().name("Books").description("Browse and manage the book catalog"),
                        new Tag().name("Orders").description("Place and track customer orders")));
    }
}
