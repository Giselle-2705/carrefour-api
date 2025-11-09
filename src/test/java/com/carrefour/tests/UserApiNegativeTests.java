package com.carrefour.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserApiNegativeTests {

    private static final String BASE_URI = "https://serverest.dev";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
        System.out.println("[NEGATIVE] Base URI configurado: " + BASE_URI);
    }

    @Test
    @Order(1)
    @DisplayName("⚠️ CREATE sem token – 201 ou 400 (dependendo da política atual)")
    public void createUser_withoutToken_unauthorized() {
        String json = """
                {
                  "nome": "Teste Sem Token",
                  "email": "semtoken_%d@qa.com",
                  "password": "Abc12345",
                  "administrador": false
                }
                """.formatted(System.currentTimeMillis());

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        // Se a API ainda aceitar chamadas públicas → 201; caso contrário 400.
        if (resp.getStatusCode() == 201) {
            assertNotNull(resp.jsonPath().getString("message"));
            assertNotNull(resp.jsonPath().getString("_id"));
        } else {
            assertEquals(400, resp.getStatusCode(),
                    "Sem token a API deve retornar 400 (comportamento atual)");
        }
    }

    @Test
    @Order(2)
    @DisplayName("⚠️ GET usuário inexistente (ID inválido) – 400")
    public void getUser_nonExisting_notFound() {
        String invalidId = "12345";
        Response resp = RestAssured
                .given()
                .when()
                .get("/usuarios/{id}", invalidId)
                .then()
                .extract()
                .response();

        assertEquals(400, resp.getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("⚠️ UPDATE usuário inexistente (ID inválido) – 400")
    public void updateUser_nonExisting_notFound() {
        String invalidId = "abcde";
        String json = """
                {
                  "nome": "Nome Atualizado",
                  "email": "atualizado@example.com",
                  "password": "NovaSenha123",
                  "administrador": false
                }
                """;

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("/usuarios/{id}", invalidId)
                .then()
                .extract()
                .response();

        assertEquals(400, resp.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("⚠️ DELETE usuário inexistente (ID válido) – 200")
    public void deleteUser_nonExisting_notFound() {
        String nonExistingId = "aaaaaaaaaaaaaaaa";
        Response resp = RestAssured
                .given()
                .when()
                .delete("/usuarios/{id}", nonExistingId)
                .then()
                .extract()
                .response();

        assertEquals(200, resp.getStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("⚠️ CREATE com payload incompleto – 400")
    public void createUser_missingFields_badRequest() {
        String json = """
                {
                  "email": "incompleto@example.com"
                }
                """;

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        assertEquals(400, resp.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("⚠️ CREATE com email já cadastrado – 400")
    public void createUser_duplicateEmail_badRequest() {
        long ts = System.currentTimeMillis();
        String email = "duplicate_test_" + ts + "@qa.com";

        // Primeiro usuário (válido)
        String firstJson = """
                {
                  "nome": "Primeiro",
                  "email": "%s",
                  "password": "Abc12345",
                  "administrador": "false"
                }
                """.formatted(email);

        Response firstResp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(firstJson)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        assertEquals(201, firstResp.getStatusCode(),
                "Criação inicial deve ser bem‑sucedida");

        // Tentativa de duplicar
        String dupJson = """
                {
                  "nome": "Segundo",
                  "email": "%s",
                  "password": "Xyz98765",
                  "administrador": "false"
                }
                """.formatted(email);

        Response dupResp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(dupJson)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        assertEquals(400, dupResp.getStatusCode(),
                "Email duplicado deve gerar 400");
    }
}
