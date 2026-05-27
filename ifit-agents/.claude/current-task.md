# Task: Support Ticket via Email

## Request
Implementar un endpoint de soporte técnico que permita al usuario autenticado enviar un
ticket de ayuda/duda al equipo de soporte mediante email. El usuario proporciona asunto,
categoría y mensaje; el sistema envía un email HTML al buzón de soporte usando la
infraestructura JavaMailSender ya existente en el módulo `notification`.
No se requiere persistencia en base de datos ni chat conversacional.

## Affected services
- **Ifit** (puerto 8081) — módulo `notification`: nuevo endpoint REST, nuevo método de
  servicio, nuevo DTO y nueva plantilla HTML. Es el único servicio afectado.

## Change type
NEW_ENDPOINT

## Agent assignments

### Agent 1 — Context analyst
Analizar el módulo `notification` completo antes de cualquier escritura:

- `ifit/src/main/java/com/uca/juangarcia/ifit/modules/notification/controller/AppEmailController.java`
  — actualmente vacío; confirmar que no hay endpoints previos que colisionen.
- `ifit/src/main/java/com/uca/juangarcia/ifit/modules/notification/service/AppEmailService.java`
  — revisar `sendEmail(AppEmailDetails)` y `sendVerificationEmail(AppUserResponseDto)` para
  entender el patrón exacto de sustitución de plantillas (`{{placeholder}}`).
- `ifit/src/main/resources/templates/email/verificationmail.html` — plantilla de referencia
  visual (paleta de colores, estructura de tabla HTML).
- `ifit/src/main/resources/application.properties` — confirmar que `spring.mail.username`
  existe y decidir el email receptor de soporte.
- `ifit/src/main/java/com/uca/juangarcia/ifit/modules/notification/model/AppEmailDetails.java`
  — verificar campos disponibles (`recipient`, `subject`, `msgBody`, `attachment`).
- Inspeccionar cómo otros controllers del proyecto extraen el JWT autenticado
  (`@AuthenticationPrincipal Jwt jwt`) para replicar el patrón exacto.

### Agent 2A — Gateway guardian
Verificar si la ruta `/ifit/api/v1/appemail/**` ya está declarada en el ApiGateway.
Según la convención `StripPrefix=3`, el path efectivo en el servicio Ifit sería `/appemail/**`.

- Si la ruta **existe**: confirmar que el predicado `Path` y el filtro `StripPrefix=3` son
  correctos y que no está marcada como pública en la whitelist de rutas sin JWT.
- Si la ruta **no existe**: crear el predicado en la configuración del Gateway apuntando al
  servicio `ifit` en Eureka, con `StripPrefix=3` y sin añadirla a rutas públicas (el endpoint
  requiere `ROLE_USER`).

Archivo a revisar/modificar: `gateway/src/main/resources/application.properties` o la clase
`@Configuration` equivalente de rutas del Gateway.

### Agent 2B — Domain guardian
Módulo `notification` — verificar y crear:

1. **`SupportTicketRequestDto`** (CREAR como Java Record):
   - Campos: `String subject`, `String category`, `String message`
   - Valores de categoría admitidos: `TECNICO`, `RUTINA`, `CUENTA`, `OTRO`
   - Verificar en `ifit/pom.xml` si existe `spring-boot-starter-validation` antes de añadir
     anotaciones `@NotBlank`.

2. **`AppEmailService.sendSupportTicketEmail()`** (MODIFICAR):
   - Añadir `@Value("${ifit.support.email}") private String supportEmail`
   - Firma: `sendSupportTicketEmail(String userName, String userEmail, SupportTicketRequestDto ticket)`
   - Lee `classpath:templates/email/supportticket.html` con el patrón `ClassPathResource` +
     `readAllBytes` idéntico al de `sendVerificationEmail`
   - Sustituye los seis placeholders: `{{userName}}`, `{{userEmail}}`, `{{category}}`,
     `{{subject}}`, `{{message}}`, `{{submittedAt}}`
   - Llama a `sendEmail(AppEmailDetails)` con `recipient = supportEmail`
   - Retorna `EmailResponseDto(true, ...)` o `EmailResponseDto(false, ...)` en caso de error
   - **No modificar** `sendEmail()` ni `sendVerificationEmail()`.

3. **`AppEmailController`** (MODIFICAR):
   - Añadir `@PostMapping("/support-ticket")`
   - Anotar con `@PreAuthorize("hasRole('ROLE_USER')")`
   - Extraer `preferred_username` y `email` del JWT via `@AuthenticationPrincipal Jwt jwt`
   - Retornar `ResponseEntity<EmailResponseDto>`
   - Añadir Javadoc al método.

### Agent 2C — AI guardian
SKIP — Ronnie no está involucrado. No hay cambios en `@AiService`, `@SystemMessage`,
`ChatMemory` ni RAG.

### Agent 3 — Quality reviewer
Verificar antes de marcar como completo:

- `sendSupportTicketEmail()` tiene null/blank guard al inicio: si `userName`, `userEmail`
  o `ticket.message()` son nulos lanzar `IllegalArgumentException` con mensaje descriptivo.
- `AppEmailController` tiene Javadoc en el método del endpoint (requisito de convención).
- La `IOException` de lectura de plantilla está capturada en try/catch, retornando
  `EmailResponseDto(false, ...)` — nunca propagada al controlador.
- El campo `@Value("${ifit.support.email}")` está declarado en `application.properties`
  antes de cualquier otro cambio; sin él el contexto Spring no arranca.
- `SupportTicketRequestDto` es un Java Record, no una clase con getters/setters.
- La plantilla `supportticket.html` declara `charset="UTF-8"` explícito.
- Verificar patrón de inyección de `AppEmailService` en el controlador (constructor vs campo)
  siguiendo la convención del proyecto.

### Agent 4 — Implementor
Orden obligatorio de creación (dependencias en cascada):

1. **`ifit/src/main/resources/application.properties`** — añadir al final:
   ```
   # Support ticket recipient
   ifit.support.email=ifit.communication@gmail.com
   ```

2. **`ifit/src/main/java/com/uca/juangarcia/ifit/modules/notification/dto/SupportTicketRequestDto.java`**
   — Java Record con los tres campos `subject`, `category`, `message`.

3. **`ifit/src/main/resources/templates/email/supportticket.html`**
   — HTML con inline-styles, misma paleta que `verificationmail.html`:
   fondo `#222831`, tarjeta `#393E46`, acento `#FFD369`, texto `#EEEEEE`.
   Tabla con: usuario, email, categoría, asunto, mensaje, timestamp.
   Los seis `{{placeholder}}` deben coincidir exactamente con los que sustituye el servicio.

4. **`ifit/src/main/java/com/uca/juangarcia/ifit/modules/notification/service/AppEmailService.java`**
   — Añadir `@Value`, método `sendSupportTicketEmail()`.

5. **`ifit/src/main/java/com/uca/juangarcia/ifit/modules/notification/controller/AppEmailController.java`**
   — Inyectar `AppEmailService`, añadir endpoint `POST /support-ticket`.

6. **Gateway** — si Agent 2A detecta que la ruta `/ifit/api/v1/appemail/**` falta,
   añadirla como último paso.

### Agent 5 — Final verifier
Comprobar la cadena completa extremo a extremo:

- **Ruta Gateway → Servicio**: `POST /ifit/api/v1/appemail/support-ticket` con JWT válido
  de `ROLE_USER` llega al `AppEmailController` sin 401/403/404.
- **JWT claim `email`**: verificar que el realm `ifit-realm` de Keycloak mapea el claim
  `email` en el access token. Si retorna `null`, documentar el mapper de Keycloak necesario
  o añadir fallback via `AppUserRepository` usando `preferred_username`.
- **Deserialización de Record**: confirmar que Jackson (Spring Boot 3 / Jackson 2.12+)
  deserializa `SupportTicketRequestDto` sin configuración adicional.
- **Retrocompatibilidad**: verificar mentalmente que `sendVerificationEmail()` y el flujo
  de registro no están afectados.
- **Sin ruta pública**: `/ifit/api/v1/appemail/support-ticket` no debe aparecer en la
  whitelist de rutas sin autenticación del Gateway.

## Acceptance criteria
- [ ] `POST /ifit/api/v1/appemail/support-ticket` con JWT de `ROLE_USER` y body válido
  retorna HTTP 200 con `{"isSend": true, "message": "..."}`.
- [ ] El email llega a `ifit.communication@gmail.com` con HTML formateado mostrando
  nombre de usuario, email, categoría, asunto y mensaje del ticket.
- [ ] `POST /ifit/api/v1/appemail/support-ticket` sin JWT retorna HTTP 401.
- [ ] El flujo de verificación de email (`sendVerificationEmail`) continúa funcionando sin
  ningún cambio de comportamiento.
- [ ] Si `message` o `subject` son nulos/vacíos, el servicio retorna
  `{"isSend": false, "message": "..."}` sin excepción no controlada.

## Risk areas
- **Claim `email` ausente en JWT de Keycloak**: el realm `ifit-realm` puede no mapear
  `email` al access token por defecto. Si `jwt.getClaimAsString("email")` retorna `null`,
  el ticket no identifica al usuario. Mitigación: fallback a `AppUserRepository` via
  `preferred_username`.
- **Propiedad `ifit.support.email` no declarada**: si se omite el paso 1, el arranque de
  Spring falla con `IllegalArgumentException: Could not resolve placeholder`. Añadirla
  siempre antes de cualquier otra modificación.
- **Spam / ausencia de rate limiting**: sin limitación de llamadas, un usuario autenticado
  podría inundar el buzón. Riesgo aceptable para TFG; documentar como limitación conocida
  en el Javadoc del endpoint.
- **`AppEmailController` vacío**: si existen tests de carga de contexto que validen el
  controlador vacío, la adición del bean puede hacer fallar esos tests. Verificar antes
  de implementar.
