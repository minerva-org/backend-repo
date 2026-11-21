package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.ObjectMapper;

class AiLaboratoryClientTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    private AiLaboratoryClient client;
    private int responseStatus = 200;
    private String responseBody = "{\"prompts\":{}}";
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> serviceKey = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> contentType = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            serviceKey.set(exchange.getRequestHeaders().getFirst("X-AI-Service-Key"));
            path.set(exchange.getRequestURI().getPath());
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        client = new AiLaboratoryClient(mapper,
                "http://127.0.0.1:" + server.getAddress().getPort(), "test-internal-key");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void forwardsPdfAndJsonPartWithInternalKeyAndPreserves422() {
        responseStatus = 422;
        responseBody = "{\"rawOutput\":\"bad\",\"tree\":null,\"validationErrors\":[\"invalid\"]}";
        var result = client.extract(new MockMultipartFile("file", "plan.pdf",
                "application/pdf", "%PDF-content".getBytes(StandardCharsets.UTF_8)),
                mapper.readTree("""
                        {"sourceType":"STUDY_PLAN","prompts":{"role":"r","instructions":"i","outputContract":"o"}}
                        """));
        assertEquals(422, result.getStatusCode().value());
        assertEquals("bad", result.getBody().get("rawOutput").asString());
        assertEquals("no-store", result.getHeaders().getCacheControl());
        assertEquals("test-internal-key", serviceKey.get());
        assertEquals("/laboratory/documents/extract", path.get());
        assertTrue(contentType.get().startsWith("multipart/form-data;boundary="));
        assertTrue(requestBody.get().contains("filename=\"plan.pdf\""));
        assertTrue(requestBody.get().contains("%PDF-content"));
        assertTrue(requestBody.get().contains("name=\"request\""));
        assertTrue(requestBody.get().contains("application/json"));
        assertTrue(requestBody.get().contains("\"outputContract\":\"o\""));
    }

    @Test
    void loadsDefaultsAndTurnsInternalAuthErrorsOrInvalidJsonIntoGatewayErrors() {
        assertEquals(200, client.prompts().getStatusCode().value());
        assertEquals("/laboratory/documents/prompts", path.get());
        responseStatus = 403;
        var authError = assertThrows(ResponseStatusException.class, () -> client.prompts());
        assertEquals(502, authError.getStatusCode().value());
        responseStatus = 200;
        responseBody = "<html>not json</html>";
        var jsonError = assertThrows(ResponseStatusException.class, () -> client.prompts());
        assertEquals(502, jsonError.getStatusCode().value());
    }

    // Input: PDF válido enviado al endpoint normal /documents/extract.
    // Resultado esperado: reenvía el multipart con clave interna y conserva 200.
    @Test
    void forwardsNormalDocumentExtractionWithInternalKey() {
        responseStatus = 200;
        responseBody = "{\"units\":[{\"name\":\"Unidad\",\"topics\":[{\"name\":\"Tema\",\"concepts\":[{\"name\":\"Concepto\"}]}]}]}";

        var result = client.extractDocument(new MockMultipartFile(
                "file", "plan.pdf", "application/pdf",
                "%PDF-valid-content".getBytes(StandardCharsets.UTF_8)));

        assertEquals(200, result.getStatusCode().value());
        assertEquals("/documents/extract", path.get());
        assertEquals("test-internal-key", serviceKey.get());
        assertTrue(contentType.get().startsWith("multipart/form-data;boundary="));
        assertTrue(requestBody.get().contains("filename=\"plan.pdf\""));
        assertTrue(requestBody.get().contains("%PDF-valid-content"));
        assertEquals("Unidad", result.getBody().at("/units/0/name").asString());
    }

    // Input: AI devuelve 422 por documento sin jerarquía utilizable.
    // Resultado esperado: preserva 422 y el código/mensaje de AI.
    @Test
    void preservesNormalDocumentExtraction422Diagnostic() {
        responseStatus = 422;
        responseBody = "{\"code\":\"NO_USABLE_HIERARCHY\",\"message\":\"No hay contenido utilizable.\"}";

        var result = client.extractDocument(new MockMultipartFile(
                "file", "recibo.pdf", "application/pdf",
                "%PDF-no-academico".getBytes(StandardCharsets.UTF_8)));

        assertEquals(422, result.getStatusCode().value());
        assertEquals("NO_USABLE_HIERARCHY",
                result.getBody().get("code").asString());
        assertEquals("/documents/extract", path.get());
    }
}
