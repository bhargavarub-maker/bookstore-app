package com.vone8.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Starts the whole app with the H2 database and calls the API like a client would. */
@SpringBootTest
@AutoConfigureMockMvc
class BookstoreApiTests {

    /** The default user from application.properties, sent the way any HTTP client would. */
    static final String AUTH = basic("student", "bookstore123");

    @Autowired
    MockMvc mvc;

    static String basic(String user, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    // GET and POST that log in with the default user
    static MockHttpServletRequestBuilder get(String url) {
        return MockMvcRequestBuilders.get(url).header(HttpHeaders.AUTHORIZATION, AUTH);
    }

    static MockHttpServletRequestBuilder post(String url) {
        return MockMvcRequestBuilders.post(url).header(HttpHeaders.AUTHORIZATION, AUTH);
    }

    static MockHttpServletRequestBuilder getWithoutLogin(String url) {
        return MockMvcRequestBuilders.get(url);
    }

    @Test
    void listsSeededBooks() throws Exception {
        mvc.perform(get("/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test
    void filtersBooksByGenre() throws Exception {
        mvc.perform(get("/v1/books").param("genre", "programming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genre").value("programming"));
    }

    @Test
    void missingBookReturns404() throws Exception {
        mvc.perform(get("/v1/books/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book 9999 not found"));
    }

    @Test
    void createsBook() throws Exception {
        mvc.perform(post("/v1/books").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title":"Refactoring","author":"Martin Fowler","genre":"programming","price":41.00,"stock":3}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void rejectsInvalidBook() throws Exception {
        mvc.perform(post("/v1/books").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void placesOrderAndRejectsTooManyCopies() throws Exception {
        mvc.perform(post("/v1/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerName":"Test","customerEmail":"t@vone8.com","items":[{"bookId":2,"quantity":1}]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.total").value(14.50));

        mvc.perform(post("/v1/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerName":"Test","customerEmail":"t@vone8.com","items":[{"bookId":2,"quantity":500}]}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void servesOpenApiDocument() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Bookstore API"));
    }

    @Test
    void apiNeedsAPassword() throws Exception {
        mvc.perform(getWithoutLogin("/v1/books"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"Bookstore\""))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(getWithoutLogin("/v1/orders").header(HttpHeaders.AUTHORIZATION, basic("student", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webPageGetsNoBrowserLoginPopup() throws Exception {
        mvc.perform(getWithoutLogin("/v1/books").header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void webPagesAndSwaggerArePublic() throws Exception {
        mvc.perform(getWithoutLogin("/login.html")).andExpect(status().isOk());
        mvc.perform(getWithoutLogin("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("basicAuth")));
    }
}
