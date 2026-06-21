# 🧪 Guía Completa de Testing - iFit

**Ahora tienes 2 formas de hacer E2E Testing:**

## 📋 Opción 1: Postman Collections

**Para**: Testing manual, exploración, debugging rápido  
**Ubicación**: `test/`  
**Archivos**: `*-updated.postman_collection.json`

```bash
# Ver README
cat test/README.md

# Ver setup detallado
cat test/POSTMAN_SETUP.md

# Ejecutar vía CLI
./test/run-tests.ps1  (Windows)
bash test/run-tests.sh (Linux/Mac)
```

**Pros**:
- ✅ Visual en Postman UI
- ✅ No requiere setup de Node
- ✅ Variables de entorno integradas
- ✅ Reutilizable con otros equipos

**Contras**:
- ❌ Ejecución manual
- ❌ Difícil integrar en CI/CD

---

## 🚀 Opción 2: Vitest + Axios (NUEVO)

**Para**: Testing automatizado, CI/CD, desarrollo  
**Ubicación**: `test/functional/api/`  
**Archivos**: `*.spec.ts`

```bash
# Setup
npm install

# Ejecutar
npm test              # Watch mode
npm run test:run      # Una sola ejecución
npm run test:auth     # Solo auth
npm run test:users    # Solo users
npm run test:ronnie   # Solo Ronnie
```

**Pros**:
- ✅ Automatizado
- ✅ Fácil en CI/CD
- ✅ TypeScript nativo
- ✅ 41 tests funcionales
- ✅ Reutilizable en proyectos

**Contras**:
- ❌ Requiere Node/npm
- ❌ Menos visual que Postman

---

## 🎯 Comparativa Rápida

| Aspecto | Postman | Vitest |
|---------|---------|--------|
| **Setup** | Importar JSON | `npm install` |
| **Ejecutar** | Click UI o CLI | `npm test` |
| **Lenguaje** | Postman Script | TypeScript |
| **CI/CD** | Posible pero tedioso | Nativo |
| **Desarrollo** | Exploración | Validación |
| **Velocidad** | Lenta (UI) | Rápida (CLI) |
| **Tests** | 15 endpoints | 41 tests |
| **Mantenimiento** | Manual | Automático |

---

## 📊 Estructura Final

```
ifit/
├── test/
│   ├── functional/
│   │   └── api/
│   │       ├── auth.spec.ts         (17 tests)
│   │       ├── users.spec.ts        (13 tests)
│   │       └── ronnie.spec.ts       (11 tests)
│   ├── utils/
│   │   ├── http-client.ts           (Cliente HTTP)
│   │   ├── test-helpers.ts          (Helpers)
│   │   └── keycloak-helper.ts       (Keycloak)
│   ├── setup.ts                      (Setup global)
│   ├── VITEST_README.md             (Guía Vitest)
│   │
│   ├── *-updated.postman_collection.json (4 collections)
│   ├── ifit-local.postman_environment.json
│   ├── README.md                     (Guía Postman)
│   ├── POSTMAN_SETUP.md             (Setup Postman)
│   ├── CHANGES.md                   (Cambios realizados)
│   ├── run-tests.ps1                (Script PowerShell)
│   └── run-tests.sh                 (Script Bash)
│
├── vitest.config.ts                 (Config Vitest)
├── tsconfig.json                    (Config TypeScript)
├── package.json                     (Deps + scripts)
│
├── .env.test                        (Vars entorno)
├── .env.test.example               (Plantilla)
└── .gitignore.test                 (Gitignore)
```

---

## 🚀 Quick Start (Elige uno)

### Para exploración rápida (Postman)
```bash
cd test/

# Importar en Postman
# File → Import → *-updated.postman_collection.json

# O ejecutar vía CLI
./run-tests.ps1  # Windows

# Ver logs
cat POSTMAN_SETUP.md
```

### Para testing automatizado (Vitest)
```bash
# Setup
npm install

# Ejecutar
npm test

# Ver logs
cat test/VITEST_README.md
```

### Para testing completo (Ambos)
```bash
# 1. Tests de API (Vitest)
npm test

# 2. Tests de Postman (Newman)
cd test/
npm install -g newman
./run-tests.ps1

# 3. Verificar que ambos pasan
# ✅ 41 tests de Vitest
# ✅ 15 endpoints de Postman
```

---

## 📈 Ejecución en Pipeline CI/CD

### GitHub Actions
```yaml
- name: Run API Tests
  run: npm run test:run

- name: Run Postman Tests
  run: |
    npm install -g newman
    cd test && ./run-tests.sh
```

### GitLab CI
```yaml
test:api:
  script:
    - npm install
    - npm run test:run

test:postman:
  script:
    - npm install -g newman
    - cd test && bash run-tests.sh
```

---

## 🔄 Workflow Recomendado

### Desarrollo Local
```
1. ❌ Escribo código
2. ✅ Ejecuto: npm test (Vitest)
3. ✅ Ejecuto: npm run test:api (Vitest específico)
4. 📝 Commit cuando pasan tests
```

### Pre-PR
```
1. npm test                    (Vitest)
2. npm run test:coverage       (Coverage)
3. npm run test:run            (Modo no-watch)
4. cd test && ./run-tests.ps1  (Postman)
5. ✅ Todos pasan → Push
```

### En CI/CD
```
1. git clone
2. npm install
3. docker-compose up -d
4. npm run test:run
5. Report results
```

---

## 📚 Documentación Detallada

**Postman**:
- `test/README.md` - Overview (5 min)
- `test/POSTMAN_SETUP.md` - Guía completa (15 min)
- `test/CHANGES.md` - Qué cambió (10 min)

**Vitest**:
- `test/VITEST_README.md` - Guía completa (15 min)
- `vitest.config.ts` - Configuración
- `test/utils/*` - Código fuente

---

## ❓ Preguntas Frecuentes

### ¿Cuál debo usar primero?
**Vitest** - Es más rápido y automatizado. Postman es para exploración.

### ¿Puedo usar ambos?
**Sí** - De hecho se recomienda. Postman para exploración, Vitest para CI/CD.

### ¿Cómo agrego nuevos tests?
**Vitest**: Crear `test/functional/api/nuevo.spec.ts`  
**Postman**: Importar colección, agregar request, exportar

### ¿Tests son lentos?
**Vitest**: Normalmente 10-30 segundos  
**Postman**: Depende de Newman, típicamente más lento

### ¿Necesito Docker corriendo?
**Sí** para ambos. `docker-compose up -d`

### ¿Cómo debuggeo un test que falla?
```bash
npm test -- --reporter=verbose
npm run test:auth -- --grep "POST /auth/register"
```

---

## 🎓 Aprender Más

- [Vitest Docs](https://vitest.dev)
- [Axios Docs](https://axios-http.com)
- [Postman Docs](https://learning.postman.com)
- [Testing Best Practices](https://testingjavascript.com)

---

## 📞 Soporte

**Si los tests fallan**:
1. Verificar Docker: `docker-compose ps`
2. Revisar logs: `docker-compose logs`
3. Verificar .env.test está configurado
4. Revisar error específico en logs

**Si necesitas ayuda**:
- Vitest: `npm test -- --help`
- Postman: Ver POSTMAN_SETUP.md
- Docker: `docker-compose logs -f [service]`

---

**Creado**: 2026-06-21  
**Versión**: 2.0  
**Incluye**: Postman + Vitest  
**Tests**: 41 (Vitest) + 15 (Postman)  
**Cobertura**: APIs Gateway, iFit, Keycloak, Ronnie
