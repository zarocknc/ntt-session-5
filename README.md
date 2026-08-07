# ms-account

Microservicio `account-balance-service` desarrollado como parte del **Taller 5: Patrones + Resiliencia + Redis**. Está construido con Spring Boot 3.5 (WebFlux), Spring Cloud 2025.0.1, Spring Data Redis Reactive y Resilience4j, exponiendo endpoints reactivos para consultar saldos de cuentas con apertura a un API Core Banking simulado. Aplica los patrones de diseño Cache-Aside (Redis), Repository (encapsulando acceso a Redis) y Strategy (cálculo de comisiones/margen sobre el saldo).

El proyecto se entrega con un código base parcial (~50%) que será retomado y completado a lo largo del laboratorio: se agregarán las piezas faltantes de Redis (configuración y repositorio reactivo), las estrategias de comisión (`PercentageFeeStrategy`, `FeeStrategySelector`) y las protecciones con Resilience4j (CircuitBreaker, Retry, TimeLimiter y Ratelimiter) sobre cada uno de los cuatro endpoints expuestos. El flujo de trabajo se documenta a continuación bajo la sección **Laboratorio**.

## Laboratorio

### feat-01 Redis TTL config

Incluir en el archivo `application.yaml` una propiedad para configurar el *'Time To Live' (TTL)* de los registros que se guarden en redis, según lo solicitado en la guía del taller.

Se implementó la propiedad personalizada `cache.ttl.account-balance: 5m` en `src/main/resources/application.yaml`, mapeable vía `@Value` a `java.time.Duration` en el futuro `AccountBalanceCacheRepository`. Además se agregó la configuración de conexión a Redis (`spring.data.redis.*`) para que el microservicio se integre con la instancia del `docker-compose.yml` (Redis 7 en `localhost:6379`). El health indicator de Redis se activó en actuator para reflejar que el servicio ahora depende del caché.