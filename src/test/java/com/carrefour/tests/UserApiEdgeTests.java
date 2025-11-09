package com.carrefour.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserApiEdgeTests {

    private static final String BASE_URI = "https://serverest.dev";
    private static String token;

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

        Response loginResp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/login")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Login status: " + loginResp.getStatusCode());
        
        if (loginResp.getStatusCode() == 200) {
            token = loginResp.jsonPath().getString("authorization");
            System.out.println("[EDGE] Token obtido com sucesso");
        } else {
            token = null;
            System.out.println("[EDGE] Login falhou – status: " + loginResp.getStatusCode());
            System.out.println("[EDGE] Resposta do login: " + loginResp.asString());
        }
        
        System.out.println("[EDGE] Base URI configurado: " + BASE_URI);
    }

    @Test
    @Order(1)
    @DisplayName("⚠️ CREATE com TODOS os campos vazios – 400")
    public void createUser_allEmptyFields_badRequest() {
        String json = """
                {
                  "nome": "",
                  "email": "",
                  "password": "",
                  "administrador": false
                }
                """;

        System.out.println("[EDGE] Testando todos os campos vazios...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status campos vazios: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Todos os campos vazios devem gerar 400");
    }

    @Test
    @Order(2)
    @DisplayName("⚠️ CREATE com nome vazio – 400")
    public void createUser_nomeEmpty_badRequest() {
        String json = """
                {
                  "nome": "",
                  "email": "nomevazio-edge@example.com",
                  "password": "Abc12345",
                  "administrador": false
                }
                """;

        System.out.println("[EDGE] Testando nome vazio...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status nome vazio: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Nome vazio deve gerar 400");
    }

    @Test
    @Order(3)
    @DisplayName("⚠️ CREATE com email vazio – 400")
    public void createUser_emailEmpty_badRequest() {
        String json = """
                {
                  "nome": "Fulano",
                  "email": "",
                  "password": "Abc12345",
                  "administrador": false
                }
                """;

        System.out.println("[EDGE] Testando email vazio...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status email vazio: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Email vazio deve gerar 400");
    }

    @Test
    @Order(4)
    @DisplayName("⚠️ CREATE com password vazio – 400")
    public void createUser_passwordEmpty_badRequest() {
        String json = """
                {
                  "nome": "Fulano",
                  "email": "senhavazia-edge@example.com",
                  "password": "",
                  "administrador": false
                }
                """;

        System.out.println("[EDGE] Testando password vazio...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status password vazio: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Password vazio deve gerar 400");
    }

    @Test
    @Order(5)
    @DisplayName("⚠️ CREATE com administrador vazio – 400")
    public void createUser_adminEmpty_badRequest() {
        String json = """
                {
                  "nome": "Fulano",
                  "email": "adminvazio-edge@example.com",
                  "password": "Abc12345",
                  "administrador": ""
                }
                """;

        System.out.println("[EDGE] Testando administrador vazio...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status admin vazio: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Administrador vazio deve gerar 400");
    }

    @Test
    @Order(6)
    @DisplayName("⚠️ CREATE com administrador inválido – 400")
    public void createUser_adminInvalid_badRequest() {
        String json = """
                {
                  "nome": "Fulano",
                  "email": "admininvalido-edge@example.com",
                  "password": "Abc12345",
                  "administrador": "maybe"
                }
                """;

        System.out.println("[EDGE] Testando administrador inválido...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status admin inválido: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Administrador inválido deve gerar 400");
    }

    @Test
    @Order(7)
    @DisplayName("⚠️ BUG – nome só com espaços – 400")
    public void createUser_nameOnlySpaces_bug() {
        String json = """
                {
                  "nome": "   ",
                  "email": "espacos-edge@example.com",
                  "password": "Abc12345",
                  "administrador": false
                }
                """;

        System.out.println("[EDGE] Testando nome só com espaços...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status nome com espaços: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Nome só com espaços deve gerar 400");
    }

    @Test
    @Order(8)
    @DisplayName("⚠️ BUG – password só com espaços – 400")
    public void createUser_passwordOnlySpaces_bug() {
        String json = """
                {
                  "nome": "Usuário Espaço",
                  "email": "passwordespaco-edge@example.com",
                  "password": "   ",
                  "administrador": false
                }
                """;

        System.out.println("[EDGE] Testando password só com espaços...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status password com espaços: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Password só com espaços deve gerar 400");
    }

    @Test
    @Order(9)
    @DisplayName("⚠️ CREATE com senha fraca – 400")
    public void createUser_weakPassword_badRequest() {
        String json = """
                {
                  "nome": "Usuário Fraco",
                  "email": "senhafraca-edge@example.com",
                  "password": "123",
                  "administrador": false
                }
                """;

        System.out.println("[EDGE] Testando senha fraca...");

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status senha fraca: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Senha fraca deve gerar 400");
    }

    @Test
    @Order(10)
    @DisplayName("⚠️ CREATE com campos exatamente 100 chars – 400")
    public void createUser_maxLength_badRequest() {
        String maxName = "N".repeat(100);
        String maxEmailLocal = "e".repeat(90);
        String maxEmail = maxEmailLocal + "@exemplo.com";
        String maxPassword = "P".repeat(100);

        String json = """
                {
                  "nome": "%s",
                  "email": "%s",
                  "password": "%s",
                  "administrador": false
                }
                """.formatted(maxName, maxEmail, maxPassword);

        System.out.println("[EDGE] Testando campos com 100 caracteres...");
        System.out.println("[EDGE] Tamanho nome: " + maxName.length());
        System.out.println("[EDGE] Tamanho email: " + maxEmail.length());
        System.out.println("[EDGE] Tamanho password: " + maxPassword.length());

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status 100 chars: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Campos com 100 caracteres devem gerar 400");
    }

    @Test
    @Order(11)
    @DisplayName("⚠️ CREATE com campos >100 chars – 400")
    public void createUser_exceedMaxLength_badRequest() {
        String tooLongName = "N".repeat(101);
        String email = "valid.email@example.com";
        String password = "ValidPass123";

        String json = """
                {
                  "nome": "%s",
                  "email": "%s",
                  "password": "%s",
                  "administrador": false
                }
                """.formatted(tooLongName, email, password);

        System.out.println("[EDGE] Testando nome com 101 caracteres...");
        System.out.println("[EDGE] Tamanho nome: " + tooLongName.length());

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status >100 chars: " + resp.getStatusCode());

        assertEquals(400, resp.getStatusCode(),
                "Nome com mais de 100 caracteres deve gerar 400");
    }

    @Test
    @Order(12)
    @DisplayName("⚠️ CREATE com email já cadastrado – 400")
    public void createUser_duplicateEmail_badRequest() {
        // E-mail ULTRA-ÚNICO para este teste
        long ts = System.currentTimeMillis();
        String uniquePart = UUID.randomUUID().toString().substring(0, 8);
        String email = "duplicate-edge-" + ts + "-" + uniquePart + "@qa.com";
        
        System.out.println("[EDGE] E-mail único gerado para teste de duplicação: " + email);

        // Primeiro usuário (deve dar sucesso)
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
                .header("Authorization", "Bearer " + token)
                .body(firstJson)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status criação inicial: " + firstResp.getStatusCode());
        System.out.println("[EDGE] Resposta inicial: " + firstResp.asString());

        assertEquals(201, firstResp.getStatusCode(),
                "Criação inicial deve ser bem‑sucedida"); 

        // Agora tenta duplicar o MESMO e-mail
        String dupJson = """
                {
                  "nome": "Segundo",
                  "email": "%s",
                  "password": "Xyz98765",
                  "administrador": false
                }
                """.formatted(email);

        Response dupResp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dupJson)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status duplicação: " + dupResp.getStatusCode());
        System.out.println("[EDGE] Resposta duplicação: " + dupResp.asString());

        assertEquals(400, dupResp.getStatusCode(),
                "Email duplicado deve gerar 400");
    }

    @Test
    @Order(13)
    @DisplayName("✅ CREATE com payload válido – 201")
    public void createUser_successful() {
        // E-mail ULTRA-ÚNICO para teste de sucesso
        long ts = System.currentTimeMillis();
        String uniquePart = UUID.randomUUID().toString().substring(0, 8);
        String email = "controle-" + ts + "-" + uniquePart + "@gmail.com";
        
        System.out.println("[EDGE] E-mail único para teste de sucesso: " + email);

        String json = """
                {
                  "nome": "Usuário Controle",
                  "email": "%s",
                  "password": "Abc12345",
                  "administrador": "false"
                }
                """.formatted(email);

        Response resp = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(json)
                .when()
                .post("/usuarios")
                .then()
                .extract()
                .response();

        System.out.println("[EDGE] Status criação válida: " + resp.getStatusCode());
        System.out.println("[EDGE] Resposta sucesso: " + resp.asString());

        assertEquals(201, resp.getStatusCode(),
                "Payload válido deve gerar 201");
    }

    @AfterAll
    static void cleanup() {
        System.out.println("\n🧹 [EDGE] Iniciando limpeza de usuários de teste...");

        if (token != null) {
            // Listar todos os usuários para limpeza
            Response listResp = RestAssured
                    .given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/usuarios")
                    .then()
                    .extract()
                    .response();

            if (listResp.getStatusCode() == 200) {
                List<Map<String, Object>> usuarios = listResp.jsonPath().getList("usuarios");
                int countDeleted = 0;

                System.out.println("[EDGE] Encontrados " + usuarios.size() + " usuários na base");

                for (Map<String, Object> usuario : usuarios) {
                    String email = (String) usuario.get("email");
                    if (email != null && email.contains("edge-")) {
                        String id = (String) usuario.get("_id");
                        String nome = (String) usuario.get("nome");
                        
                        // Deletar usuário de teste
                        Response delResp = RestAssured
                                .given()
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .delete("/usuarios/{id}", id)
                                .then()
                                .extract()
                                .response();

                        if (delResp.getStatusCode() == 200) {
                            countDeleted++;
                            System.out.println("   ✅ Usuário removido: " + email + " (ID: " + id + ", Nome: " + nome + ")");
                        } else {
                            System.out.println("   ❌ Falha ao remover: " + email + " (status: " + delResp.getStatusCode() + ")");
                        }
                    }
                }
                
                System.out.println("[EDGE] Limpeza concluída: " + countDeleted + " usuários removidos");
            } else {
                System.out.println("[EDGE] ❌ Não foi possível listar usuários para limpeza (status: " + listResp.getStatusCode() + ")");
            }
        } else {
            System.out.println("[EDGE] ⚠️ Token não disponível, pulando limpeza");
        }
    }
}
