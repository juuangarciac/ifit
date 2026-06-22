# 🚀 Guía Rápida - Scripts de Pruebas Funcionales

## 📌 Resumen Rápido

He creado dos suites de tests funcionales completas en `test/functional/`:

### 1️⃣ **Auth Flow** (`auth-flow.spec.ts`)
Prueba el flujo COMPLETO de autenticación:
- ✅ Registro de usuario
- ✅ Obtención de código de verificación DIRECTAMENTE de la base de datos
- ✅ Verificación de usuario con el código
- ✅ Login
- ✅ Obtención y refresh de tokens

### 2️⃣ **Model Testing** (`model-testing.spec.ts`)
Prueba Ronnie (modelo IA) con DIFERENTES perfiles:
- ✅ 4 tipos de coaches (Ronnie, Serena, Eliud, Kael)
- ✅ 3 niveles de experiencia (Principiante, Intermedio, Avanzado)
- ✅ Diferentes cuestionarios rellenados
- ✅ Validaciones de calidad de respuestas
- ✅ Reglas específicas por coach

---

## 🎯 Cómo Usar

### Preparación

```bash
# 1. Instalar dependencias (solo primera vez)
cd test
npm install

# 2. Asegurar que todos los servicios están corriendo
docker-compose up -d

# 3. Verificar que los servicios están healthy
docker-compose ps
```

### Ejecutar Tests

```bash
# Correr AMBAS suites
npm test

# O específicamente:

# Solo autenticación
npm run test:auth

# Solo modelo Ronnie
npm run test:ronnie

# Con salida verbose
npm run test:all

# En modo watch (se actualiza automáticamente)
npm test -- --watch
```

---

## 📝 Auth Flow - Explicación Detallada

**Archivo**: `test/functional/auth-flow.spec.ts`

### ¿Qué Hace?

1. **Crea un usuario nuevo** vía POST `/auth/register`
   - Email único (generado con timestamp)
   - Contraseña: `Test@1234`
   - Retorna tokens JWT automáticamente

2. **Obtiene el código de verificación DIRECTAMENTE de MySQL**
   ```typescript
   const verificationCode = await getVerificationCode(testEmail);
   // Resultado: "123456" (6 dígitos)
   ```

3. **Verifica el usuario** vía POST `/auth/verify`
   - Usa el código obtenido de la BD
   - Recibe nuevos tokens JWT
   - Usuario ahora completamente activo

4. **Logea el usuario** vía POST `/auth/login`
   - Email y contraseña
   - Obtiene access token y refresh token

5. **Prueba token refresh** vía POST `/auth/refresh`
   - Usa refresh token para obtener nuevo access token
   - Valida que funciona correctamente

6. **Prueba logout** vía POST `/auth/logout`
   - Invalida el refresh token
   - Usuario deslogueado

### Salida de Ejemplo

```
✅ User registered successfully
   Email: test_1718962845123@example.com
   User ID: 42

✅ Verification code retrieved from DB
   Code: 123456

✅ User verified successfully

✅ User login successful

✅ Tokens refreshed successfully
   New Access Token: eyJhbGciOiJIUzI1NiIs...

✅ User logged out successfully
```

---

## 🤖 Model Testing - Explicación Detallada

**Archivo**: `test/functional/model-testing.spec.ts`

### ¿Qué Hace?

Prueba que Ronnie (el modelo IA) genera rutinas coherentes para DIFERENTES usuarios:

#### Perfil 1: Principiante con Coach Ronnie
- **Usuario**: Nunca ha entrenado, sin lesiones
- **Objetivo**: Perder peso
- **Rutina esperada**: 3 días, ejercicios básicos, full-body

#### Perfil 2: Intermedio con Coach Serena
- **Usuario**: Entrena 2-3 veces/semana
- **Objetivo**: Ganar masa muscular
- **Rutina esperada**: 4 días, ejercicios compuestos, split

#### Perfil 3: Avanzado con Coach Eliud
- **Usuario**: Entrena 5+ días/semana
- **Objetivo**: Mejorar performance
- **Rutina esperada**: 5 días, alta intensidad

#### Perfil 4: Wellness con Coach Kael
- **Usuario**: Sedentario, dolor de espalda
- **Objetivo**: Mejorar salud
- **Rutina esperada**: 3 días, low-impact, seguro

### Validaciones Realizadas

✅ **Todos los ejercicios están en el catálogo** (nombre exacto)
✅ **No hay ejercicios repetidos en el mismo día**
✅ **Mínimo 4 ejercicios por día** (bloque principal)
✅ **Reglas específicas por coach**:
   - Cardio: Sin press de banca, curl bíceps, peso muerto
   - Calistenia: Ejercicios de peso corporal
   - Musculación: Compuestos e aislamiento
   - Wellness: Low-impact, seguro

✅ **Estructura JSON correcta** con todos los campos requeridos

### Salida de Ejemplo

```
✅ Beginner routine generated with 3 days
   Day 1: 5 exercises
   Day 2: 5 exercises
   Day 3: 4 exercises

✅ Intermediate routine generated with 4 days
   Day 1: 6 exercises
   Day 2: 5 exercises
   Day 3: 6 exercises
   Day 4: 5 exercises

✅ No duplicate exercises within same day

✅ All exercises match catalog exactly

✅ All model tests completed successfully
```

---

## 📂 Archivos Creados

### Scripts de Test

```
test/functional/
├── auth-flow.spec.ts           ← Flujo completo de autenticación
├── model-testing.spec.ts       ← Pruebas del modelo con diferentes perfiles
└── README.md                   ← Documentación detallada
```

### Helpers (Utilidades)

```
test/utils/
├── db-helper.ts                ← NUEVO: Acceso directo a MySQL
├── http-client.ts              ← Cliente HTTP personalizado
├── test-helpers.ts             ← Helpers generales
└── keycloak-helper.ts          ← Helpers para Keycloak
```

### Configuración

```
test/
├── .env.test                   ← ACTUALIZADO: Agregadas vars de BD
├── package.json                ← ACTUALIZADO: Agregado mysql2
└── vitest.config.ts            ← Config de Vitest
```

---

## 🛠️ Características Principales

### ✨ Auth Flow

- **Registro automatizado** con datos únicos
- **Acceso directo a MySQL** para obtener código de verificación
- **Verificación completa** del usuario
- **Login y token management**
- **Refresh token testing**
- **Logout y invalidación**

### ✨ Model Testing

- **5 perfiles de usuario diferentes**
- **Múltiples coaches (4)**
- **Múltiples niveles de experiencia (3)**
- **Validación de catálogo** (nombres exactos)
- **Reglas específicas por coach**
- **Checks de calidad exhaustivos**
- **Respuestas validadas** según perfil

---

## 🔧 Solución de Problemas

### "Verification code is NULL"
```bash
# Solución: El código se genera en el registro
# Espera 500ms antes de consultar BD
await new Promise(r => setTimeout(r, 500));
```

### "Connection refused" (MySQL)
```bash
# Verifica que MySQL está corriendo
docker-compose ps

# Si no, inicia los servicios
docker-compose up -d
```

### "Tests timeout"
```bash
# Aumenta el timeout
API_TIMEOUT=20000 npm test
```

### "Auth token expired"
```bash
# Los tokens duran ~5 minutos
# Los tests usan refresh token automáticamente
# Si vas más lento, necesitas esperar menos entre requests
```

---

## 📊 Próximas Mejoras Sugeridas

Estos scripts son la BASE. Puedes extender con:

1. **Más perfiles de usuario** (ej: con lesiones específicas)
2. **Tests de endpoints de perfil** (GET /users/profile, PUT etc)
3. **Tests de guardado de rutinas** (POST /routines)
4. **Tests de historial de routinas** (GET /routines/history)
5. **Tests de integración E2E** (app completa)
6. **Performance testing** (carga, stress)
7. **Mocking de Groq** para tests sin API key

---

## 📚 Estructura de Archivos Importantes

```
ifit/
├── test/
│   ├── functional/              ← ¡TUS NUEVOS TESTS AQUÍ!
│   │   ├── auth-flow.spec.ts
│   │   ├── model-testing.spec.ts
│   │   └── README.md
│   │
│   ├── utils/
│   │   ├── db-helper.ts         ← NUEVO: DB direct access
│   │   ├── http-client.ts
│   │   └── test-helpers.ts
│   │
│   ├── .env.test                ← ACTUALIZADO
│   └── package.json             ← ACTUALIZADO
│
├── docker-compose.yml           ← Servicios necesarios
├── init-db.sql                  ← Estructura de BD
└── README.md
```

---

## 🎓 Cómo Leer los Tests

### Auth Flow - Estructura

```typescript
describe('🔐 Authentication Flow', () => {
  describe('1️⃣ User Registration', () => {
    it('should create a new user', async () => {
      // Arrange: preparar datos
      const email = generateTestEmail();
      
      // Act: ejecutar acción
      const response = await gatewayClient.post('/auth/register', {...});
      
      // Assert: verificar resultados
      expect(response.status).toBe(201);
      expect(response.data.appUser.email).toBe(email);
      
      // Log: mostrar resultado
      console.log('✅ User registered');
    });
  });
});
```

### Model Testing - Estructura

```typescript
describe('🤖 Model Testing', () => {
  describe('👤 Profile: Beginner', () => {
    it('should generate routine for beginner', async () => {
      // 1. Crear usuario con perfil específico
      const user = await createUserWithProfile(profile);
      
      // 2. Generar rutina desde Ronnie
      const routine = await generateRoutine(user.userId, user.accessToken);
      
      // 3. Validar estructura
      validateRoutineStructure(routine, 'Ronnie');
      
      // 4. Verificar reglas específicas
      for (const exercise of routine.days[0].exercises) {
        expect(catalogNames.has(exercise.exerciseName)).toBe(true);
      }
    });
  });
});
```

---

## 🚀 Siguiente Paso

Una vez que estos tests pasen:

```bash
# 1. Correr tests
npm test

# 2. Ver la salida completa
npm run test:all

# 3. Verificar logs
docker-compose logs -f

# 4. Interrogar la BD directamente
docker exec ifit-mysql mysql -u root -proot ifit \
  -e "SELECT email, verification_code, is_verified FROM user ORDER BY created_at DESC LIMIT 5;"
```

---

**¡Todo listo!** Los scripts están diseñados para ser **extensibles y mantenibles**.

Cualquier pregunta sobre cómo expandirlos o modificarlos, ¡avísame!
