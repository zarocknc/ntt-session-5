# ms-account

Microservicio `account-balance-service` desarrollado como parte del **Taller 5: Patrones + Resiliencia + Redis**. Está construido con Spring Boot 3.5 (WebFlux), Spring Cloud 2025.0.1, Spring Data Redis Reactive y Resilience4j, exponiendo endpoints reactivos para consultar saldos de cuentas con apertura a un API Core Banking simulado. Aplica los patrones de diseño Cache-Aside (Redis), Repository (encapsulando acceso a Redis) y Strategy (cálculo de comisiones/margen sobre el saldo).

El proyecto se entrega con un código base parcial (~50%) que será retomado y completado a lo largo del laboratorio: se agregarán las piezas faltantes de Redis (configuración y repositorio reactivo), las estrategias de comisión (`PercentageFeeStrategy`, `FeeStrategySelector`) y las protecciones con Resilience4j (CircuitBreaker, Retry, TimeLimiter y Ratelimiter) sobre cada uno de los cuatro endpoints expuestos. El flujo de trabajo se documenta a continuación bajo la sección **Laboratorio**.

## Laboratorio

### feat-01 Redis TTL config

Incluir en el archivo `application.yaml` una propiedad para configurar el *'Time To Live' (TTL)* de los registros que se guarden en redis, según lo solicitado en la guía del taller.

Se implementó la propiedad personalizada `cache.ttl.account-balance: 5m` en `src/main/resources/application.yaml`, mapeable vía `@Value` a `java.time.Duration` en `AccountBalanceCacheRepository`. Además se agregó la configuración de conexión a Redis (`spring.data.redis.*`) para que el microservicio se integre con la instancia del `docker-compose.yml` (Redis 7 en `localhost:6379`). El health indicator de Redis se activó en actuator para reflejar que el servicio ahora depende del caché.

### feat-02 Strategy pattern

Aplicar Patrón Strategy para calcular comisión/margen sobre el saldo. La guía exige: `PercentageFeeStrategy` que multiplique el saldo por el 5%, y `FeeStrategySelector` que devuelva `PercentageFeeStrategy` si la cuenta es `PREMIUM`, caso contrario `FixedFeeStrategy`.

Se crearon `fee/PercentageFeeStrategy.java` (`@Component("percentageFeeStrategy")`, `balance.multiply(0.05)`) y `fee/FeeStrategySelector.java` (`@Component`, recibe `Map<String, FeeStrategy>` inyectado por Spring y resuelve por nombre de bean). La validación se hace con `"PREMIUM".equalsIgnoreCase(accountType)`. Con esto `AccountBalanceServiceImpl` compila, pues ya tenía escrita la lógica de Cache-Aside + `feeStrategySelector.select(...)`.

### feat-03 Redis (Cache-Aside + Repository)

Agregar `RedisConfig` (carpeta `config`) y `AccountBalanceCacheRepository` (carpeta `cache`) con métodos `findByAccountId` y `save`. Incluir TTL configurable.

Se creó `config/RedisConfig.java` exponiendo un `ReactiveRedisTemplate<String, AccountBalance>` con `StringRedisSerializer` para claves y `Jackson2JsonRedisSerializer` para valores. Se creó `cache/AccountBalanceCacheRepository.java` con `findByAccountId(String)` -> `Mono<AccountBalance>` (lee de `account-balance:{id}`) y `save(AccountBalance)` -> `Mono<AccountBalance>` (escribe con el TTL leído de `cache.ttl.account-balance` vía `@Value`), implementando el patrón Repository que encapsula el acceso a Redis.

### feat-04 CircuitBreaker + Retry

En `WebClientCoreBankingClient`, implementar circuit breaker y retry en `getAccountBalanceCbRetry` con configuración en `application.yaml`. Crear método `fallbackBalance` que devuelva `ERROR` en `currency` y `accountType`. Habilitar métricas de retry en actuator.

Se anotó `getAccountBalanceCbRetry` con `@CircuitBreaker(name="coreBankingCbRetry", fallbackMethod="fallbackBalance")` y `@Retry(name="coreBankingCbRetry", fallbackMethod="fallbackBalance")`. El método `fallbackBalance(String, Throwable)` retorna un `Mono<AccountBalance>` con `currency="ERROR"` y `accountType="ERROR"`. En `application.yaml` se configuraron `resilience4j.circuitbreaker.instances.coreBankingCbRetry` (sliding window de 10, failure-rate 50%, 10s open, half-open 3) y `resilience4j.retry.instances.coreBankingCbRetry` (max-attempts 3, wait 1s, reintenta ante respuestas 5xx). Se habilitaron `resilience4j.retry.metrics.enabled=true` y el endpoint `retryevents` ya estaba expuesto en actuator.

### feat-05 TimeLimiter

En `WebClientCoreBankingClient`, implementar time limiter en `getAccountBalanceTl` con configuración en `application.yaml`. Crear método `fallbackBalance` que devuelva `ERROR TIMELIMITER` en `currency` y `accountType`. Agregar log de timelimiter.

Se cambió la firma de `getAccountBalanceTl` a `CompletableFuture<AccountBalance>` (requerido por `@TimeLimiter`) en la interfaz `CoreBankingClient` y la implementación usa `.toFuture()` sobre el `Mono` de WebClient. Se anotó con `@TimeLimiter(name="coreBankingTl", fallbackMethod="fallbackBalanceTimeLimiter")` que retorna `CompletableFuture` con `currency="ERROR TIMELIMITER"` y `accountType="ERROR TIMELIMITER"`. En `application.yaml` se configuró `resilience4j.timelimiter.instances.coreBankingTl.timeout-duration: 3s` (menor al delay de 6s del mock `slow`, forzando el fallback). Se activó `logging.level.io.github.resilience4j.timelimiter: DEBUG` y se expuso el endpoint `timelimiters` en actuator. `AccountBalanceServiceImpl.getBalanceTl` se ajustó para usar `Mono.fromFuture(...)`.