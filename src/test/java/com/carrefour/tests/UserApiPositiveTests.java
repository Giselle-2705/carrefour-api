package com.carrefour.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserApiPositiveTests {

    private static final String BASE_URI = "https://serverest.dev";
    private static String token;
    private static String createdUserId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
        
        // Login para obter token
        String loginPayload = """
                {
                  "email": "fulano@qa.com",
                  "password": "teste"
                }
                """;

        Response loginResp = given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/login");

        System.out.println("[POSITIVE] Login status: " + loginResp.getStatusCode());
        
        if (loginResp.getStatusCode() == 200) {
            token = loginResp.jsonPath().getString("authorization");
            System.out.println("[POSITIVE] Token obtido: " + (token != null ? "SIM" : "NÃO"));
        } else {
            token = null;
            System.out.println("[POSITIVE] Login falhou - status: " + loginResp.getStatusCode());
        }
    }

    // Método auxiliar para gerar e-mail único
    private String gerarEmailUnico(String prefixo) {
        long ts = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return prefixo + "-" + ts + "-" + uuid + "@qa.com";
    }

    @Test
    @Order(1)
    @DisplayName("✅ CREATE usuário válido")
    public void createUser_successful() {
        if (token == null) {
            System.out.println("[POSITIVE] Sem token, pulando criação");
            return;
        }

        String email = gerarEmailUnico("positive");
        System.out.println("[POSITIVE] Criando com email: " + email);

        String json = """
                {
                  "nome": "Usuário Positivo",
                  "email": "%s",
                  "password": "Abc12345",
                  "administrador": "false"
                }
                """.formatted(email);

        Response resp = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .post("/usuarios");

        System.out.println("[POSITIVE] Status criação: " + resp.getStatusCode());
        
        if (resp.getStatusCode() == 201) {
            createdUserId = resp.jsonPath().getString("_id");
            System.out.println("[POSITIVE] ID criado: " + createdUserId);
        } else {
            System.out.println("[POSITIVE] Criação falhou: " + resp.asString());
            createdUserId = null;
        }

        assertEquals(201, resp.getStatusCode(), 
                "Criação deve retornar 201. Obtido: " + resp.getStatusCode());
    }

    @Test
    @Order(2)
    @DisplayName("✅ GET usuário por ID")
    public void getUser_byId_successful() {
        if (createdUserId == null) {
            System.out.println("[POSITIVE] Sem ID criado, pulando GET");
            return;
        }

        System.out.println("[POSITIVE] GET com ID: " + createdUserId);

        Response resp = given()
                .get("/usuarios/" + createdUserId);

        System.out.println("[POSITIVE] Status GET: " + resp.getStatusCode());

        assertEquals(200, resp.getStatusCode(), 
                "GET deve retornar 200. Obtido: " + resp.getStatusCode());

        String responseId = resp.jsonPath().getString("_id");
        assertNotNull(responseId, "Resposta deve conter _id");
        assertEquals(createdUserId, responseId, "IDs devem ser iguais");
    }

    @Test
    @Order(3)
    @DisplayName("✅ LISTAR todos os usuários")
    public void listUsers_successful() {
        System.out.println("[POSITIVE] Listando usuários...");

        Response resp = given()
                .get("/usuarios");

        System.out.println("[POSITIVE] Status lista: " + resp.getStatusCode());

        assertEquals(200, resp.getStatusCode(), 
                "Lista deve retornar 200. Obtido: " + resp.getStatusCode());

        // Lista pode estar vazia, mas status deve ser 200
        Object usuarios = resp.jsonPath().get("usuarios");
        System.out.println("[POSITIVE] Usuários encontrados: " + 
                (usuarios != null ? usuarios.toString() : "null"));
    }

    @Test
    @Order(4)
    @DisplayName("✅ UPDATE usuário existente")
    public void updateUser_successful() {
        if (createdUserId == null || token == null) {
            System.out.println("[POSITIVE] Sem ID ou token, pulando UPDATE");
            return;
        }

        String email = gerarEmailUnico("positive-update");
        System.out.println("[POSITIVE] Atualizando ID: " + createdUserId);

        String json = """
                {
                  "nome": "Usuário Atualizado",
                  "email": "%s",
                  "password": "NovaSenha123",
                  "administrador": "false"
                }
                """.formatted(email);

        Response resp = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .put("/usuarios/" + createdUserId);

        System.out.println("[POSITIVE] Status UPDATE: " + resp.getStatusCode());
        System.out.println("[POSITIVE] Mensagem: " + resp.jsonPath().getString("message"));

        assertEquals(200, resp.getStatusCode(), 
                "UPDATE deve retornar 200. Obtido: " + resp.getStatusCode());

        String message = resp.jsonPath().getString("message");
        assertTrue(message.contains("sucesso"), 
                "Deve conter 'sucesso'. Obtido: " + message);
    }

    @Test
    @Order(5)
    @DisplayName("✅ DELETE usuário criado")
    public void deleteUser_successful() {
        if (createdUserId == null || token == null) {
            System.out.println("[POSITIVE] Sem ID ou token, pulando DELETE");
            return;
        }

        System.out.println("[POSITIVE] Deletando ID: " + createdUserId);

        Response resp = given()
                .header("Authorization", "Bearer " + token)
                .delete("/usuarios/" + createdUserId);

        System.out.println("[POSITIVE] Status DELETE: " + resp.getStatusCode());
        System.out.println("[POSITIVE] Mensagem: " + resp.jsonPath().getString("message"));

        assertEquals(200, resp.getStatusCode(), 
                "DELETE deve retornar 200. Obtido: " + resp.getStatusCode());

        String message = resp.jsonPath().getString("message");
        assertTrue(message.contains("sucesso"), 
                "Deve conter 'sucesso'. Obtido: " + message);
    }
}
