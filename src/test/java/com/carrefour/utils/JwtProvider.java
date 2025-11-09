package com.carrefour.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.concurrent.TimeUnit;

/**
 * Classe responsável por obter e armazenar o token JWT.
 * Versão com logs detalhados para debug - Compatível com Java 19.
 */
public class JwtProvider {

    // ═══════════════════════════════════════════════════════════════════════════
    // CONFIGURAÇÕES DA API (ajuste as credenciais se necessário)
    // ═══════════════════════════════════════════════════════════════════════════
    private static final String BASE_URI = "https://serverest.dev";
    private static final String LOGIN_ENDPOINT = "/login";
    private static final String USER = "beltrano@qa.com.br";        // ← Teste outras se não funcionar
    private static final String PASSWORD = "teste";            // ← Teste outras se não funcionar

    // ═══════════════════════════════════════════════════════════════════════════
    // VARIÁVEIS DE ESTADO (cache do token)
    // ═══════════════════════════════════════════════════════════════════════════
    private static String token;
    private static long tokenAcquiredAt = 0; // epoch millis

    private JwtProvider() {
        // classe utilitária – não pode ser instanciada
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODO PÚBLICO PRINCIPAL
    // ═══════════════════════════════════════════════════════════════════════════
    /**
     * Retorna um token válido. Se o token atual estiver expirado ou ainda não foi obtido,
     * faz login na API e armazena o novo token.
     *
     * @return token JWT (string) ou null se falhar
     */
    public static synchronized String getToken() {
        System.out.println("🔑 [JWT] getToken() chamado");

        // Verifica se já temos um token válido
        if (token == null || isExpired()) {
            System.out.println("🔄 [JWT] Token inválido/expirado. Fazendo login...");
            token = loginAndGetToken();
            if (token != null && !token.trim().isEmpty()) {
                tokenAcquiredAt = System.currentTimeMillis();
                System.out.println("✅ [JWT] Token armazenado com sucesso (cache por 30 min)");
            } else {
                System.out.println("❌ [JWT] Falha ao obter token! Retornando null");
                token = null;
            }
        } else {
            System.out.println("✅ [JWT] Usando token cacheado");
        }

        // Log do token (primeiros 20 chars para não poluir o console)
        if (token != null && token.length() > 20) {
            System.out.println("🔑 [JWT] Token: " + token.substring(0, 20) + "...");
        } else {
            System.out.println("🔑 [JWT] Token: " + (token != null ? token : "NULL"));
        }

        return token;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VERIFICAÇÃO DE EXPIRAÇÃO
    // ═══════════════════════════════════════════════════════════════════════════
    /** Verifica se o token já tem mais de 30 minutos. */
    private static boolean isExpired() {
        if (tokenAcquiredAt == 0) {
            return true;
        }
        long elapsed = System.currentTimeMillis() - tokenAcquiredAt;
        long minutesElapsed = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        boolean expired = minutesElapsed >= 30;
        System.out.println("⏰ [JWT] Token tem " + minutesElapsed + " minutos. Expirado? " + expired);
        return expired;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODO PRINCIPAL: FAZ O LOGIN E EXTRAI O TOKEN
    // ═══════════════════════════════════════════════════════════════════════════
    /** Faz a chamada de login e devolve o token. */
    private static String loginAndGetToken() {
        System.out.println("🌐 [JWT] Iniciando processo de login...");
        System.out.println("📧 [JWT] Usuário: " + USER);
        System.out.println("🔒 [JWT] Senha: " + PASSWORD.replaceAll(".", "*"));
        System.out.println("🔗 [JWT] Endpoint: " + BASE_URI + LOGIN_ENDPOINT);

        // 1️⃣ Preparar o payload de login (JSON manual - compatível com Java 8+)
        String loginPayload = "{\"email\":\"" + USER + "\",\"password\":\"" + PASSWORD + "\"}";
        System.out.println("📄 [JWT] Payload JSON: " + loginPayload);

        try {
            // 2️⃣ Fazer a requisição HTTP POST usando RestAssured
            Response response = RestAssured
                    .given()
                    .baseUri(BASE_URI)
                    .contentType("application/json")
                    .body(loginPayload)
                    .when()
                    .post(LOGIN_ENDPOINT);

            // 3️⃣ Extrair informações da resposta
            int statusCode = response.getStatusCode();
            String responseBody = response.asString();

            // 4️⃣ Log detalhado da resposta
            System.out.println("📊 [JWT] Status Code: " + statusCode);
            System.out.println("📄 [JWT] Resposta completa:");
            System.out.println(responseBody);
            System.out.println("─────────────────────────────────────────────────────────────");

            // 5️⃣ Verificar se o login foi bem-sucedido
            if (statusCode != 200) {
                System.err.println("❌ [JWT] LOGIN FALHOU! Status: " + statusCode);
                System.err.println("   Possíveis causas:");
                System.err.println("   - Credenciais incorretas");
                System.err.println("   - API fora do ar");
                System.err.println("   - Endpoint mudou");
                return null;
            }

            // 6️⃣ Tentar extrair o token de diferentes campos possíveis
            String authToken = null;

            // Tenta o campo "authorization" (mais comum)
            authToken = response.jsonPath().getString("authorization");
            if (authToken != null && !authToken.trim().isEmpty()) {
                System.out.println("✅ [JWT] Token encontrado no campo 'authorization'");
            } else {
                // Tenta o campo "token"
                authToken = response.jsonPath().getString("token");
                if (authToken != null && !authToken.trim().isEmpty()) {
                    System.out.println("✅ [JWT] Token encontrado no campo 'token'");
                } else {
                    // Tenta o campo "accessToken"
                    authToken = response.jsonPath().getString("accessToken");
                    if (authToken != null && !authToken.trim().isEmpty()) {
                        System.out.println("✅ [JWT] Token encontrado no campo 'accessToken'");
                    } else {
                        // Última tentativa: procura qualquer campo que contenha "token"
                        System.out.println("⚠️ [JWT] Nenhum campo comum de token encontrado");
                        System.err.println("❌ [JWT] FALHA: Token não encontrado na resposta!");
                        return null;
                    }
                }
            }

            // 7️⃣ Limpar o token (remover espaços) e validar
            String cleanToken = authToken.trim();
            if (cleanToken.length() < 10) {
                System.err.println("❌ [JWT] Token muito curto ou inválido: '" + cleanToken + "'");
                return null;
            }

            System.out.println("✅ [JWT] Token extraído com sucesso (" + cleanToken.length() + " caracteres)");
            return cleanToken;

        } catch (Exception e) {
            System.err.println("❌ [JWT] ERRO DURANTE O LOGIN: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
