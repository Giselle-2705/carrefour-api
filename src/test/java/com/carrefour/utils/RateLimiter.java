package com.carrefour.utils;

/**
 * Limita o número de requisições a 100 por minuto.
 * Cada chamada a {@code acquire()} pode bloquear a thread por até 600 ms
 * (1 min / 100 req = 600 ms entre requisições).
 *
 * Implementação simples baseada em contagem de chamadas por janela de 1 minuto.
 */
public class RateLimiter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long ONE_MINUTE_MS = 60_000L;

    private static int requestCount = 0;
    private static long windowStart = System.currentTimeMillis();

    private RateLimiter() {
        // utilitário – não pode ser instanciado
    }

    /**
     * Bloqueia a thread até que seja permitido fazer a próxima requisição.
     * Deve ser chamado **antes** de cada chamada à API.
     */
    public static synchronized void acquire() {
        long now = System.currentTimeMillis();

        // Se a janela de 1 minuto terminou, reinicia a contagem
        if (now - windowStart >= ONE_MINUTE_MS) {
            windowStart = now;
            requestCount = 0;
        }

        // Se já atingiu o limite, espera até o próximo minuto
        if (requestCount >= MAX_REQUESTS_PER_MINUTE) {
            long waitTime = ONE_MINUTE_MS - (now - windowStart);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Reinicia a janela após a espera
            windowStart = System.currentTimeMillis();
            requestCount = 0;
        }

        requestCount++;
    }
}
