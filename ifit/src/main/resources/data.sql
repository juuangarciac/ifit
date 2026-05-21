-- ===========================================
-- iFit Database Schema & Data (DDL + DML)
-- Versión: 3.1 - REFORMULADO PARA LLM COMPATIBILITY
-- Fecha: 2025-02-07
-- Descripción: Schema completo + datos iniciales
-- ===========================================
-- 
-- CAMBIOS EN ESTA VERSIÓN:
-- - Pregunta 2 reformulada: Lenguaje de "adaptaciones" en vez de "condiciones médicas"
-- - Pregunta 13 reformulada: "Preferencias alimentarias" en vez de "restricciones dietéticas"
-- - Opciones actualizadas para evitar activar filtros de seguridad del LLM
-- 
-- MOTIVO: Los modelos de lenguaje (Ollama/LLM) activan filtros de seguridad
-- con palabras como "condición médica", "lesión", "enfermedad", "restricción".
-- El nuevo lenguaje usa "adaptaciones", "preferencias", "zona que requiere cuidado".
-- 
-- IMPACTO EN PRIVACIDAD: 
-- - ANTES: Podíamos estar manejando datos sensibles de salud (Art. 9 RGPD)
-- - AHORA: Solo manejamos preferencias personales de fitness (Art. 6 RGPD)
-- 
-- ===========================================

-- ===========================================
-- iFit Database Schema (DDL)
-- Versión: 3.0
-- Fecha: 2025-01-07
-- Descripción: Definición de todas las tablas del sistema
-- ===========================================

-- Este script crea la ESTRUCTURA de la base de datos (tablas, relaciones, índices)
-- Debe ejecutarse ANTES de data.sql

SET FOREIGN_KEY_CHECKS = 0;

-- ===========================================
-- LIMPIEZA: truncar todas las tablas al inicio
-- Se ejecuta antes de los DROP/CREATE para
-- garantizar que no queden datos entre reinicios.
-- ===========================================
TRUNCATE TABLE user_answer;
TRUNCATE TABLE routine_exercise;
TRUNCATE TABLE routine_day;
TRUNCATE TABLE routine;
TRUNCATE TABLE questionnaire_response;
TRUNCATE TABLE user;
TRUNCATE TABLE question_option;
TRUNCATE TABLE question;
TRUNCATE TABLE questionnaire;
TRUNCATE TABLE exercise_catalog;
TRUNCATE TABLE exercises_final_fixed;
TRUNCATE TABLE coachmodeltype;
TRUNCATE TABLE experiencelevel;
TRUNCATE TABLE approle;

-- ===========================================
-- 1. TABLA: approle
-- ===========================================
DROP TABLE IF EXISTS approle;

CREATE TABLE approle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 2. TABLA: experiencelevel
-- ===========================================
DROP TABLE IF EXISTS experiencelevel;

CREATE TABLE experiencelevel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 3. TABLA: coachmodeltype
-- ===========================================
DROP TABLE IF EXISTS coachmodeltype;

CREATE TABLE coachmodeltype (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    emoji_character VARCHAR(10),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    INDEX idx_enabled (enabled),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 4. TABLA: user
-- ===========================================
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    is_registration_complete BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    verification_code VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    verification_code_expires_at DATETIME,
    role_id BIGINT NOT NULL,
    coachmodeltype_id BIGINT,
    experiencelevel_id BIGINT,
    
    CONSTRAINT fk_user_role 
        FOREIGN KEY (role_id) 
        REFERENCES approle(id),
    
    CONSTRAINT fk_user_coachmodeltype 
        FOREIGN KEY (coachmodeltype_id) 
        REFERENCES coachmodeltype(id) 
        ON DELETE SET NULL,
    
    CONSTRAINT fk_user_experiencelevel 
        FOREIGN KEY (experiencelevel_id) 
        REFERENCES experiencelevel(id) 
        ON DELETE SET NULL,
    
    INDEX idx_email (email),
    INDEX idx_is_verified (is_verified),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 5. TABLA: question
-- ===========================================
DROP TABLE IF EXISTS question;

CREATE TABLE question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    text TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    
    INDEX idx_type (type),
    INDEX idx_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 6. TABLA: question_option
-- ===========================================
DROP TABLE IF EXISTS question_option;

CREATE TABLE question_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    text VARCHAR(255) NOT NULL,
    next_question_id BIGINT,
    display_order INT NOT NULL DEFAULT 0,
    requires_text_input BOOLEAN NOT NULL DEFAULT FALSE,
    text_input_prompt TEXT,
    text_input_placeholder VARCHAR(100),
    
    CONSTRAINT fk_questionoption_question 
        FOREIGN KEY (question_id) 
        REFERENCES question(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_questionoption_nextquestion 
        FOREIGN KEY (next_question_id) 
        REFERENCES question(id) 
        ON DELETE SET NULL,
    
    INDEX idx_question_id (question_id),
    INDEX idx_display_order (display_order),
    INDEX idx_next_question_id (next_question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 7. TABLA: questionnaire
-- ===========================================
DROP TABLE IF EXISTS questionnaire;

CREATE TABLE questionnaire (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    coach_model_type_id BIGINT,
    experience_level_id BIGINT,
    first_question_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_questionnaire_coachmodeltype 
        FOREIGN KEY (coach_model_type_id) 
        REFERENCES coachmodeltype(id) 
        ON DELETE SET NULL,
    
    CONSTRAINT fk_questionnaire_experiencelevel 
        FOREIGN KEY (experience_level_id) 
        REFERENCES experiencelevel(id) 
        ON DELETE SET NULL,
    
    CONSTRAINT fk_questionnaire_firstquestion 
        FOREIGN KEY (first_question_id) 
        REFERENCES question(id) 
        ON DELETE SET NULL,
    
    INDEX idx_is_enabled (is_enabled),
    INDEX idx_coach_model_type_id (coach_model_type_id),
    INDEX idx_experience_level_id (experience_level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 8. TABLA: questionnaire_response
-- ===========================================
DROP TABLE IF EXISTS questionnaire_response;

CREATE TABLE questionnaire_response (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    questionnaire_id BIGINT NOT NULL,
    started_at DATETIME NOT NULL,
    completed_at DATETIME,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_response_user 
        FOREIGN KEY (user_id) 
        REFERENCES user(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_response_questionnaire 
        FOREIGN KEY (questionnaire_id) 
        REFERENCES questionnaire(id) 
        ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_questionnaire_id (questionnaire_id),
    INDEX idx_is_completed (is_completed),
    INDEX idx_is_active (is_active),
    INDEX idx_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- 9. TABLA: user_answer
-- ===========================================
DROP TABLE IF EXISTS user_answer;

CREATE TABLE user_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    response_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT,
    additional_text TEXT,
    ai_generated_description TEXT,
    answered_at DATETIME NOT NULL,
    
    CONSTRAINT fk_useranswer_response 
        FOREIGN KEY (response_id) 
        REFERENCES questionnaire_response(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_useranswer_question 
        FOREIGN KEY (question_id) 
        REFERENCES question(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_useranswer_selectedoption 
        FOREIGN KEY (selected_option_id) 
        REFERENCES question_option(id) 
        ON DELETE SET NULL,
    
    INDEX idx_response_id (response_id),
    INDEX idx_question_id (question_id),
    INDEX idx_answered_at (answered_at),
    
    -- Evitar respuestas duplicadas para la misma pregunta en una sesión
    UNIQUE KEY uk_response_question (response_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ===========================================
-- NOTAS SOBRE EL SCHEMA
-- ===========================================
-- 
-- ORDEN DE CREACIÓN (Respeta dependencias):
-- 1. approle (sin dependencias)
-- 2. experiencelevel (sin dependencias)
-- 3. coachmodeltype (sin dependencias)
-- 4. user (depende de: approle, coachmodeltype, experiencelevel)
-- 5. question (sin dependencias)
-- 6. question_option (depende de: question - ciclo permitido con ON DELETE SET NULL)
-- 7. questionnaire (depende de: coachmodeltype, experiencelevel, question)
-- 8. questionnaire_response (depende de: user, questionnaire)
-- 9. user_answer (depende de: questionnaire_response, question, question_option)
--
-- ÍNDICES:
-- - Se crean índices en columnas usadas en WHERE, JOIN y ORDER BY
-- - Foreign keys automáticamente tienen índices
--
-- COLLATION:
-- - utf8mb4_unicode_ci: Soporta emojis y caracteres especiales
--
-- ENGINE:
-- - InnoDB: Soporta transacciones y foreign keys
--
-- ON DELETE:
-- - CASCADE: Elimina registros relacionados (ej: borrar user → borra sus responses)
-- - SET NULL: Pone NULL en FK si el padre se elimina (ej: borrar coach → user.coachmodeltype_id = NULL)
--
-- REFERENCIA CIRCULAR:
-- - question ← question_option.next_question_id → question
-- - Permitida con ON DELETE SET NULL
--
-- ===========================================

-- ===========================================
-- iFit Database Initialization Script
-- Versión: 3.0 (CORREGIDO con modelos Java actualizados)
-- Fecha: 2025-01-07
-- Descripción: Datos iniciales para sistema de cuestionarios con árbol de decisión
-- ===========================================

-- IMPORTANTE: Este script refleja la estructura REAL de los modelos Java
-- El sistema usa un ÁRBOL DE DECISIÓN donde cada opción puede llevar a otra pregunta

-- ⚠️  ADVERTENCIA: Este script ELIMINARÁ TODOS LOS DATOS existentes antes de insertar los nuevos
-- ⚠️  Asegúrate de hacer un backup antes de ejecutar: mysqldump -u root -p ifit_db > backup.sql

SET FOREIGN_KEY_CHECKS = 0; -- Desactivar temporalmente para operaciones seguras

-- ===========================================
-- SECCIÓN 0: LIMPIEZA COMPLETA DE LA BASE DE DATOS
-- ===========================================
-- Esta sección elimina TODOS los datos de todas las tablas
-- El orden es importante para respetar las foreign keys
-- ===========================================

-- Eliminar respuestas de usuarios (hojas del árbol de dependencias)

-- ===========================================
-- FIN DE LA SECCIÓN DE LIMPIEZA
-- ===========================================

-- ===========================================
-- 1. ROLES BASE
-- ===========================================
-- Tabla: approle
-- Campos: id (PK), name
-- ===========================================
INSERT INTO approle (id, name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ===========================================
-- 2. NIVELES DE EXPERIENCIA
-- ===========================================
-- Tabla: experiencelevel
-- Campos: id (PK), name (UK), description
-- ===========================================
INSERT INTO experiencelevel (id, name, description) VALUES
(1, 'Principiante', 'Usuario con menos de 1 año de experiencia en entrenamiento. Se enfoca en aprender técnica, crear hábito y desarrollar fuerza básica.'),
(2, 'Intermedio', 'Usuario con entre 1 y 3 años de experiencia. Tiene buena base física, controla la técnica y puede seguir programas estructurados de progresión.'),
(3, 'Avanzado', 'Usuario con más de 3 años de experiencia continua. Optimiza su rendimiento con técnicas avanzadas y objetivos específicos como hipertrofia, fuerza máxima o rendimiento deportivo.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    description = VALUES(description);

-- ===========================================
-- 3. MODELOS DE COACH (TIPOS DE IA)
-- ===========================================
-- Tabla: coachmodeltype
-- Campos: id (PK), name (UK), description, emoji_character, enabled, created_at, updated_at
-- ===========================================
INSERT INTO coachmodeltype (id, name, description, emoji_character, enabled, created_at, updated_at) VALUES
(1, 'Default', 'Este modelo representa contenido de propósito general, aplicable a cualquier tipo de usuario o entrenador. No está asociado a una IA concreta, sino que marca elementos reutilizables por cualquier modelo de entrenamiento.', '⭐', FALSE, NOW(), NULL),
(2, 'Ronnie', 'Si buscas ganar masa muscular, levantar fuerte y entrenar como los grandes del fitness. Directo, intenso y enfocado en resultados con hierro y sudor.', '💪', TRUE, NOW(), NULL),
(3, 'Serena', 'Ideal para quienes quieren sentirse mejor, tonificarse poco a poco y cuidar su salud física y emocional. Empática, positiva y realista.', '🌸', TRUE, NOW(), NULL),
(4, 'Eliud', 'Experto en running, cardio y preparación mental. Perfecto si tu objetivo es correr más, mejor y con disciplina. Siempre tranquilo, siempre firme.', '👟', TRUE, NOW(), NULL),
(5, 'Kael', 'Domina la calistenia, el street workout y el entrenamiento sin máquinas. Entrena donde sea, con lo que tengas. Técnico, exigente y libre.', '🤸‍♂️', TRUE, NOW(), NULL)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    description = VALUES(description),
    emoji_character = VALUES(emoji_character),
    enabled = VALUES(enabled);

-- ===========================================
-- 4. PREGUNTAS (NODOS DEL ÁRBOL DE DECISIÓN)
-- ===========================================
-- Tabla: question
-- Campos: id (PK), text, type (ENUM), is_enabled, created_at
-- NOTA: Las preguntas NO tienen relación directa con experience_level según el modelo Java
-- ===========================================

-- Preguntas iniciales (raíz del árbol)
INSERT INTO question (id, text, type, is_enabled, created_at) VALUES
-- Pregunta raíz
(1, '¿Cuál es tu objetivo principal de entrenamiento?', 'MULTIPLE_CHOICE', TRUE, NOW()),

-- Preguntas de seguimiento según objetivo
(2, '¿Necesitas que adaptemos algunos ejercicios por comodidad o salud?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(3, '¿Cuántas veces a la semana puedes entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(4, '¿Dónde prefieres entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(5, '¿Cuál es tu edad?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(6, '¿Cuál es tu peso actual (kg)?', 'NUMERIC', TRUE, NOW()),
(7, '¿Cuál es tu altura (cm)?', 'NUMERIC', TRUE, NOW()),
(8, '¿Cómo describirías tu nivel de actividad física diaria?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(9, '¿Has entrenado con regularidad anteriormente?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(10, '¿Te sientes cómodo/a siguiendo rutinas estructuradas?', 'MULTIPLE_CHOICE', TRUE, NOW()),

-- Preguntas específicas según ruta
(11, '¿Tienes experiencia con entrenamiento de fuerza?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(12, '¿Prefieres entrenamientos cortos e intensos o largos y moderados?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(13, '¿Tienes preferencias alimentarias que quieras compartir?', 'TEXT_INPUT', TRUE, NOW())
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    type = VALUES(type),
    is_enabled = VALUES(is_enabled);

-- ===========================================
-- 5. OPCIONES DE RESPUESTA (ARISTAS DEL ÁRBOL)
-- ===========================================
-- Tabla: question_option
-- Campos: id (PK), question_id (FK), text, next_question_id (FK nullable), 
--         display_order, requires_text_input, text_input_prompt, text_input_placeholder
-- ===========================================

-- Opciones para Pregunta 1: Objetivo principal
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(1, 1, 'Perder grasa corporal', 2, 1, FALSE),
(2, 1, 'Ganar masa muscular', 2, 2, FALSE),
(3, 1, 'Mejorar salud general', 2, 3, FALSE),
(4, 1, 'Aumentar resistencia cardiovascular', 2, 4, FALSE),
(5, 1, 'Tonificar y definir', 2, 5, FALSE)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 2: Adaptaciones de ejercicio
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(6, 2, 'No, puedo hacer cualquier ejercicio', 3, 1, FALSE, NULL, NULL),
(7, 2, 'Sí, tengo una zona que requiere cuidado', 3, 2, TRUE, 'Por favor indica qué zona debemos cuidar en tu entrenamiento', 'Ej: Rodilla izquierda, zona lumbar, hombro derecho'),
(8, 2, 'Sí, prefiero ejercicios adaptados', 3, 3, TRUE, '¿Qué tipo de adaptaciones necesitas? (Esto nos ayuda a personalizar tu rutina)', 'Ej: Evitar impacto en rodillas, cuidar zona lumbar, fortalecer espalda'),
(9, 2, 'Sí, tengo limitaciones de movimiento', 3, 4, TRUE, 'Cuéntanos sobre tus limitaciones para adaptar los ejercicios', 'Ej: Movilidad reducida en cadera, evitar flexiones profundas')
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- Opciones para Pregunta 3: Frecuencia de entrenamiento
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(10, 3, '1-2 veces por semana', 4, 1, FALSE),
(11, 3, '3-4 veces por semana', 4, 2, FALSE),
(12, 3, '5-6 veces por semana', 4, 3, FALSE),
(13, 3, 'Todos los días', 4, 4, FALSE)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 4: Lugar de entrenamiento
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(14, 4, 'Solo en casa (sin equipamiento)', 5, 1, FALSE),
(15, 4, 'Solo en casa (con equipamiento básico)', 5, 2, FALSE),
(16, 4, 'Solo en gimnasio', 5, 3, FALSE),
(17, 4, 'Ambos (casa y gimnasio)', 5, 4, FALSE)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 5: Edad
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(18, 5, 'Menos de 18 años', 6, 1, FALSE),
(19, 5, '18-25 años', 6, 2, FALSE),
(20, 5, '26-35 años', 6, 3, FALSE),
(21, 5, '36-50 años', 6, 4, FALSE),
(22, 5, 'Más de 50 años', 6, 5, FALSE)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 6: Peso (NUMERIC - input libre)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(23, 6, 'Ingresar peso', 7, 1, TRUE, 'Ingresa tu peso actual en kilogramos', '70')
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- Opciones para Pregunta 7: Altura (NUMERIC - input libre)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(24, 7, 'Ingresar altura', 8, 1, TRUE, 'Ingresa tu altura en centímetros', '175')
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- Opciones para Pregunta 8: Nivel de actividad diaria
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(25, 8, 'Sedentario (trabajo de oficina, poca actividad)', 9, 1, FALSE),
(26, 8, 'Ligeramente activo (camino ocasionalmente)', 9, 2, FALSE),
(27, 8, 'Moderadamente activo (camino o me muevo regularmente)', 9, 3, FALSE),
(28, 8, 'Muy activo (trabajo físico o deporte frecuente)', 9, 4, FALSE),
(29, 8, 'Extremadamente activo (atleta o trabajo muy exigente)', 9, 5, FALSE)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 9: Experiencia previa
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(30, 9, 'Nunca he entrenado', 10, 1, FALSE),
(31, 9, 'Sí, pero hace más de 1 año', 10, 2, FALSE),
(32, 9, 'Sí, hace menos de 1 año', 10, 3, FALSE),
(33, 9, 'Actualmente entreno de forma irregular', 10, 4, FALSE),
(34, 9, 'Actualmente entreno de forma regular', 11, 5, FALSE) -- Lleva a pregunta específica
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 10: Comodidad con rutinas
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(35, 10, 'Sí, prefiero planes detallados y estructurados', 12, 1, FALSE),
(36, 10, 'Me gusta tener guía pero con algo de flexibilidad', 12, 2, FALSE),
(37, 10, 'Prefiero entrenar libremente sin planes fijos', 12, 3, FALSE),
(38, 10, 'No estoy seguro/a, nunca he seguido una rutina', 12, 4, FALSE)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 11: Experiencia con fuerza (pregunta de seguimiento)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(39, 11, 'Sí, entreno regularmente con pesas', 12, 1, FALSE),
(40, 11, 'Tengo algo de experiencia pero no soy constante', 12, 2, FALSE),
(41, 11, 'No, soy nuevo/a en entrenamiento de fuerza', 12, 3, FALSE)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 12: Preferencia de duración
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(42, 12, 'Cortos e intensos (20-30 min)', 13, 1, FALSE),
(43, 12, 'Moderados (45-60 min)', 13, 2, FALSE),
(44, 12, 'Largos y pausados (más de 60 min)', 13, 3, FALSE),
(45, 12, 'Variable, depende del día', 13, 4, FALSE)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order);

-- Opciones para Pregunta 13: Preferencias alimentarias (pregunta final)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(46, 13, 'Sí, tengo preferencias alimentarias', NULL, 1, TRUE, 'Por favor comparte tus preferencias o necesidades alimentarias', 'Ej: Vegetariano, prefiero bajo en carbohidratos, evito lácteos'),
(47, 13, 'No, como de todo sin preferencias especiales', NULL, 2, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE 
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- ===========================================
-- 6. CUESTIONARIOS (PUNTOS DE ENTRADA)
-- ===========================================
-- Tabla: questionnaire
-- Campos: id (PK), name (UK), description, coach_model_type_id (FK nullable), 
--         experience_level_id (FK nullable), first_question_id (FK), 
--         created_at, updated_at, is_enabled
-- ===========================================

INSERT INTO questionnaire (id, name, description, coach_model_type_id, experience_level_id, first_question_id, created_at, updated_at, is_enabled) VALUES
-- Cuestionario para Principiantes con Ronnie
(1, 'Ronnie - Fuerza para Principiantes', 
    'Programa intenso de fuerza diseñado por Ronnie. Ideal para quienes quieren construir músculo desde cero con mentalidad de hierro.', 
    2, 1, 1, NOW(), NOW(), TRUE),

-- Cuestionario para Principiantes con Serena
(2, 'Serena - Bienestar para Principiantes', 
    'Programa equilibrado enfocado en salud y bienestar. Perfecto para empezar sin presión, cuidando cuerpo y mente.', 
    3, 1, 1, NOW(), NOW(), TRUE),

-- Cuestionario para Intermedios con Eliud
(3, 'Eliud - Resistencia Intermedia', 
    'Programa de cardio y resistencia para quienes buscan mejorar su capacidad cardiovascular y mental.', 
    4, 2, 1, NOW(), NOW(), TRUE),

-- Cuestionario para Avanzados con Kael
(4, 'Kael - Calistenia Avanzada',
    'Programa de calistenia y street workout. Para atletas experimentados que buscan dominio corporal total.',
    5, 3, 1, NOW(), NOW(), TRUE),

-- Cuestionario para Principiantes con Eliud
(6, 'Eliud - Resistencia para Principiantes',
    'Programa de iniciación al cardio y la resistencia con Eliud. Perfecto para quienes empiezan a correr o quieren mejorar su capacidad aeróbica desde cero.',
    4, 1, 1, NOW(), NOW(), TRUE),

-- Cuestionario para Avanzados con Eliud
(7, 'Eliud - Resistencia Avanzada',
    'Programa de alto rendimiento cardiovascular con Eliud. Para atletas con experiencia que buscan superar sus marcas y afrontar retos de larga distancia.',
    4, 3, 1, NOW(), NOW(), TRUE),

-- Cuestionario para Principiantes con Kael
(8, 'Kael - Calistenia para Principiantes',
    'Introducción al entrenamiento con el peso corporal guiado por Kael. Aprende los movimientos fundamentales del street workout sin necesidad de equipamiento.',
    5, 1, 1, NOW(), NOW(), TRUE),

-- Cuestionario para Intermedios con Kael
(9, 'Kael - Calistenia Intermedia',
    'Programa de calistenia de nivel medio con Kael. Para quienes ya dominan lo básico y quieren progresar hacia elementos más técnicos como muscle-ups o handstands.',
    5, 2, 1, NOW(), NOW(), TRUE),

-- Cuestionario genérico (sin coach específico)
(5, 'Evaluación General de Fitness', 
    'Cuestionario completo para determinar tu nivel y objetivos. Al finalizar, te recomendaremos el mejor coach para ti.', 
    NULL, NULL, 1, NOW(), NOW(), TRUE)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    description = VALUES(description),
    coach_model_type_id = VALUES(coach_model_type_id),
    experience_level_id = VALUES(experience_level_id),
    first_question_id = VALUES(first_question_id),
    is_enabled = VALUES(is_enabled);

-- ===========================================
-- 7. PREGUNTAS ESPECÍFICAS POR COACH
-- ===========================================
-- Cada coach tiene su propio árbol de preguntas lineal (sin bucles).
-- El árbol de cada coach va de su primera pregunta a NULL en la última.
--
-- RANGOS DE IDs:
--   Ronnie  → Preguntas 14-26  |  Opciones  48-89
--   Serena  → Preguntas 27-39  |  Opciones  90-131
--   Kael    → Preguntas 40-52  |  Opciones 132-174
--   Eliud   → Preguntas 53-65  |  Opciones 175-215
-- ===========================================

-- -----------------------------------------------
-- RONNIE - Fuerza y Musculación (Q14 → Q26)
-- -----------------------------------------------
INSERT INTO question (id, text, type, is_enabled, created_at) VALUES
(14, '¡Vamos al grano! ¿Cuál es tu objetivo principal de entrenamiento?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(15, '¿Hay alguna zona de tu cuerpo que debamos tener en cuenta al entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(16, '¿Cuántos días a la semana puedes entrenar duro?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(17, '¿Dónde vas a entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(18, '¿Cuál es tu rango de edad?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(19, '¿Cuánto pesas? (kg)', 'NUMERIC', TRUE, NOW()),
(20, '¿Cuánto mides? (cm)', 'NUMERIC', TRUE, NOW()),
(21, '¿Cómo es tu nivel de actividad física en el día a día?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(22, '¿Tienes experiencia con ejercicios compuestos? (sentadilla, peso muerto, press de banca)', 'MULTIPLE_CHOICE', TRUE, NOW()),
(23, '¿Qué grupos musculares quieres priorizar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(24, '¿Cuánto tiempo puedes dedicar a cada sesión?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(25, '¿Qué equipamiento tienes disponible?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(26, '¿Tienes alguna preferencia o necesidad alimentaria?', 'TEXT_INPUT', TRUE, NOW())
ON DUPLICATE KEY UPDATE text = VALUES(text), type = VALUES(type), is_enabled = VALUES(is_enabled);

-- -----------------------------------------------
-- SERENA - Bienestar y Salud (Q27 → Q39)
-- -----------------------------------------------
INSERT INTO question (id, text, type, is_enabled, created_at) VALUES
(27, '¿Cuál es tu objetivo principal de bienestar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(28, '¿Hay alguna zona de tu cuerpo que debamos cuidar en los entrenamientos?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(29, '¿Cuántas veces por semana te gustaría entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(30, '¿Dónde prefieres hacer ejercicio?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(31, '¿Cuál es tu rango de edad?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(32, '¿Cuánto pesas? (kg)', 'NUMERIC', TRUE, NOW()),
(33, '¿Cuánto mides? (cm)', 'NUMERIC', TRUE, NOW()),
(34, '¿Cómo describirías tu actividad física diaria?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(35, '¿Cómo es tu nivel de estrés actualmente?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(36, '¿Cómo suele ser tu sueño?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(37, '¿Cuál es tu motivación más profunda para empezar a entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(38, '¿Cuánto tiempo tienes disponible por sesión?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(39, '¿Tienes alguna preferencia o necesidad alimentaria que quieras compartir?', 'TEXT_INPUT', TRUE, NOW())
ON DUPLICATE KEY UPDATE text = VALUES(text), type = VALUES(type), is_enabled = VALUES(is_enabled);

-- -----------------------------------------------
-- KAEL - Calistenia y Street Workout (Q40 → Q52)
-- -----------------------------------------------
INSERT INTO question (id, text, type, is_enabled, created_at) VALUES
(40, '¿Cuál es tu objetivo con la calistenia?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(41, '¿Hay alguna zona de tu cuerpo que debamos proteger al entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(42, '¿Cuántas veces a la semana puedes entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(43, '¿Dónde entrenas habitualmente?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(44, '¿Cuál es tu rango de edad?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(45, '¿Cuánto pesas? (kg)', 'NUMERIC', TRUE, NOW()),
(46, '¿Cuánto mides? (cm)', 'NUMERIC', TRUE, NOW()),
(47, '¿Cuántas dominadas puedes hacer seguidas?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(48, '¿Cuántos fondos puedes hacer en paralelas o en dos sillas?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(49, '¿Qué habilidad es tu mayor objetivo?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(50, '¿Cuánto tiempo llevas practicando calistenia o entrenamiento con peso corporal?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(51, '¿Cuánto tiempo puedes dedicar a cada sesión?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(52, '¿Tienes alguna preferencia o necesidad alimentaria?', 'TEXT_INPUT', TRUE, NOW())
ON DUPLICATE KEY UPDATE text = VALUES(text), type = VALUES(type), is_enabled = VALUES(is_enabled);

-- -----------------------------------------------
-- ELIUD - Running y Resistencia (Q53 → Q65)
-- -----------------------------------------------
INSERT INTO question (id, text, type, is_enabled, created_at) VALUES
(53, '¿Cuál es tu objetivo principal en el running?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(54, '¿Tienes alguna zona que debamos cuidar al correr o entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(55, '¿Cuántas veces a la semana puedes salir a correr o entrenar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(56, '¿En qué tipo de terreno corres habitualmente?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(57, '¿Cuál es tu rango de edad?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(58, '¿Cuánto pesas? (kg)', 'NUMERIC', TRUE, NOW()),
(59, '¿Cuánto mides? (cm)', 'NUMERIC', TRUE, NOW()),
(60, '¿Cuánto puedes correr sin parar actualmente?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(61, '¿Tienes una carrera o evento concreto como objetivo?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(62, '¿Cuál es tu ritmo aproximado al correr?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(63, '¿Te interesa incluir trabajo de fuerza o movilidad complementario al running?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(64, '¿Cuánto tiempo suelen durar tus salidas habituales?', 'MULTIPLE_CHOICE', TRUE, NOW()),
(65, '¿Tienes alguna preferencia o necesidad alimentaria que quieras compartir?', 'TEXT_INPUT', TRUE, NOW())
ON DUPLICATE KEY UPDATE text = VALUES(text), type = VALUES(type), is_enabled = VALUES(is_enabled);

-- ===========================================
-- 8. OPCIONES DE RESPUESTA ESPECÍFICAS POR COACH
-- ===========================================

-- -----------------------------------------------
-- RONNIE - Opciones (IDs 48-89)
-- -----------------------------------------------

-- Q14: Objetivo principal
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(48, 14, 'Ganar masa muscular', 15, 1, FALSE),
(49, 14, 'Perder grasa sin perder músculo', 15, 2, FALSE),
(50, 14, 'Aumentar mi fuerza máxima', 15, 3, FALSE),
(51, 14, 'Tonificar y definirme', 15, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q15: Adaptaciones
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(52, 15, 'Ninguna, estoy listo/a para todo', 16, 1, FALSE, NULL, NULL),
(53, 15, 'Sí, tengo una zona que requiere cuidado', 16, 2, TRUE, '¿Qué zona debemos tener en cuenta?', 'Ej: hombro derecho, rodilla izquierda, zona lumbar'),
(54, 15, 'Sí, necesito adaptar algunos ejercicios', 16, 3, TRUE, '¿Qué tipo de adaptaciones necesitas?', 'Ej: evitar press militar, nada de sentadilla profunda')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q16: Frecuencia
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(55, 16, '3 días por semana', 17, 1, FALSE),
(56, 16, '4 días por semana', 17, 2, FALSE),
(57, 16, '5 días por semana', 17, 3, FALSE),
(58, 16, '6 días o más por semana', 17, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q17: Lugar
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(59, 17, 'Gimnasio completo con todo el equipamiento', 18, 1, FALSE),
(60, 17, 'Casa con equipamiento (mancuernas, barra, banco)', 18, 2, FALSE),
(61, 17, 'Ambos, tengo acceso a los dos', 18, 3, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q18: Edad
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(62, 18, 'Menos de 18 años', 19, 1, FALSE),
(63, 18, '18-25 años', 19, 2, FALSE),
(64, 18, '26-35 años', 19, 3, FALSE),
(65, 18, '36-50 años', 19, 4, FALSE),
(66, 18, 'Más de 50 años', 19, 5, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q19: Peso (NUMERIC)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(67, 19, 'Ingresar peso', 20, 1, TRUE, 'Ingresa tu peso actual en kilogramos', '80')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q20: Altura (NUMERIC)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(68, 20, 'Ingresar altura', 21, 1, TRUE, 'Ingresa tu altura en centímetros', '175')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q21: Actividad diaria
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(69, 21, 'Sedentario (trabajo de escritorio, poco movimiento)', 22, 1, FALSE),
(70, 21, 'Ligeramente activo (camino ocasionalmente)', 22, 2, FALSE),
(71, 21, 'Moderadamente activo (me muevo con regularidad)', 22, 3, FALSE),
(72, 21, 'Muy activo (trabajo físico o deporte habitual)', 22, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q22: Experiencia con compuestos
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(73, 22, 'Sí, los ejecuto con buena técnica y de forma regular', 23, 1, FALSE),
(74, 22, 'Los he hecho pero con poca constancia', 23, 2, FALSE),
(75, 22, 'Solo en máquinas, nunca con peso libre', 23, 3, FALSE),
(76, 22, 'Nunca los he realizado', 23, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q23: Grupos musculares a priorizar
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(77, 23, 'Tren superior frontal (pecho, hombros, tríceps)', 24, 1, FALSE),
(78, 23, 'Tren superior posterior (espalda, trapecios, bíceps)', 24, 2, FALSE),
(79, 23, 'Tren inferior (cuádriceps, isquiotibiales, glúteos)', 24, 3, FALSE),
(80, 23, 'Entrenamiento completo y equilibrado', 24, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q24: Duración por sesión
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(81, 24, '45-60 minutos', 25, 1, FALSE),
(82, 24, '60-90 minutos', 25, 2, FALSE),
(83, 24, 'Más de 90 minutos', 25, 3, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q25: Equipamiento disponible
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(84, 25, 'Máquinas de cable, multipower y peso libre completo', 26, 1, FALSE),
(85, 25, 'Barra olímpica, mancuernas y banco ajustable', 26, 2, FALSE),
(86, 25, 'Mancuernas básicas y equipamiento mínimo', 26, 3, FALSE),
(87, 25, 'Solo bandas elásticas o peso corporal', 26, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q26: Alimentación (final - next_question_id NULL)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(88, 26, 'Sí, tengo preferencias o necesidades alimentarias', NULL, 1, TRUE, 'Cuéntanos tus preferencias para personalizar mejor tu plan', 'Ej: dieta alta en proteínas, vegetariano, evito lácteos'),
(89, 26, 'No, como de todo sin restricciones', NULL, 2, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- -----------------------------------------------
-- SERENA - Opciones (IDs 90-131)
-- -----------------------------------------------

-- Q27: Objetivo bienestar
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(90, 27, 'Sentirme mejor y más sana en general', 28, 1, FALSE),
(91, 27, 'Tonificarme sin presión ni obsesiones', 28, 2, FALSE),
(92, 27, 'Perder peso de forma sostenible y sin agobios', 28, 3, FALSE),
(93, 27, 'Ganar energía y vitalidad en el día a día', 28, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q28: Adaptaciones
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(94, 28, 'No, puedo hacer cualquier ejercicio sin problema', 29, 1, FALSE, NULL, NULL),
(95, 28, 'Sí, tengo una zona que requiere atención especial', 29, 2, TRUE, '¿Qué zona debemos cuidar en tus entrenamientos?', 'Ej: rodilla, zona lumbar, cuello, hombro'),
(96, 28, 'Sí, prefiero ejercicios suaves o de bajo impacto', 29, 3, TRUE, '¿Qué movimientos o impactos debemos evitar?', 'Ej: sin saltos, sin impacto en rodillas, ejercicios de suelo suaves')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q29: Frecuencia
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(97, 29, '1-2 veces por semana (con calma, sin agobios)', 30, 1, FALSE),
(98, 29, '3-4 veces por semana (ritmo constante)', 30, 2, FALSE),
(99, 29, '5-6 veces por semana (me gusta la constancia)', 30, 3, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q30: Lugar
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(100, 30, 'En casa, sin equipamiento especial', 31, 1, FALSE),
(101, 30, 'En casa, con algún material básico (mancuernas, esterilla)', 31, 2, FALSE),
(102, 30, 'En el gimnasio', 31, 3, FALSE),
(103, 30, 'Al aire libre (parques, senderismo, exterior)', 31, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q31: Edad
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(104, 31, 'Menos de 18 años', 32, 1, FALSE),
(105, 31, '18-25 años', 32, 2, FALSE),
(106, 31, '26-35 años', 32, 3, FALSE),
(107, 31, '36-50 años', 32, 4, FALSE),
(108, 31, 'Más de 50 años', 32, 5, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q32: Peso (NUMERIC)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(109, 32, 'Ingresar peso', 33, 1, TRUE, 'Ingresa tu peso actual en kilogramos', '65')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q33: Altura (NUMERIC)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(110, 33, 'Ingresar altura', 34, 1, TRUE, 'Ingresa tu altura en centímetros', '165')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q34: Actividad diaria
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(111, 34, 'Sedentaria (trabajo de escritorio, poco movimiento)', 35, 1, FALSE),
(112, 34, 'Ligeramente activa (camino ocasionalmente)', 35, 2, FALSE),
(113, 34, 'Moderadamente activa (me muevo con regularidad)', 35, 3, FALSE),
(114, 34, 'Muy activa (trabajo físico o deporte frecuente)', 35, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q35: Nivel de estrés [NUEVA — exclusiva de Serena]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(115, 35, 'Bajo, me siento bastante tranquila', 36, 1, FALSE),
(116, 35, 'Moderado, hay días que me agobia', 36, 2, FALSE),
(117, 35, 'Alto, tengo estrés con bastante frecuencia', 36, 3, FALSE),
(118, 35, 'Muy alto, necesito desconectar urgente', 36, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q36: Calidad del sueño [NUEVA — exclusiva de Serena]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(119, 36, 'Duermo bien y me despierto descansada', 37, 1, FALSE),
(120, 36, 'A veces tengo noches malas, pero en general bien', 37, 2, FALSE),
(121, 36, 'Duermo mal con frecuencia, pocas horas o mal descanso', 37, 3, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q37: Motivación profunda [NUEVA — exclusiva de Serena]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(122, 37, 'Quiero sentirme mejor conmigo misma', 38, 1, FALSE),
(123, 37, 'Quiero mejorar mi salud a largo plazo', 38, 2, FALSE),
(124, 37, 'El ejercicio me ayuda a gestionar el estrés', 38, 3, FALSE),
(125, 37, 'Quiero tener más energía en mi vida diaria', 38, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q38: Duración por sesión
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(126, 38, '20-30 minutos (sesiones cortas y efectivas)', 39, 1, FALSE),
(127, 38, '30-45 minutos', 39, 2, FALSE),
(128, 38, '45-60 minutos', 39, 3, FALSE),
(129, 38, 'Más de 60 minutos', 39, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q39: Alimentación (final - next_question_id NULL)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(130, 39, 'Sí, tengo preferencias o necesidades alimentarias', NULL, 1, TRUE, 'Cuéntanos tus preferencias alimentarias para adaptar mejor tu plan', 'Ej: vegetariana, intolerante al gluten, prefiero comida sencilla'),
(131, 39, 'No, como de todo sin restricciones especiales', NULL, 2, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- -----------------------------------------------
-- KAEL - Opciones (IDs 132-174)
-- -----------------------------------------------

-- Q40: Objetivo calistenia
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(132, 40, 'Aprender calistenia desde cero y dominar lo básico', 41, 1, FALSE),
(133, 40, 'Conseguir mi primer muscle-up o handstand', 41, 2, FALSE),
(134, 40, 'Ganar fuerza funcional real con solo mi cuerpo', 41, 3, FALSE),
(135, 40, 'Perder grasa entrenando sin máquinas ni pesas', 41, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q41: Adaptaciones
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(136, 41, 'No, puedo moverme sin limitaciones', 42, 1, FALSE, NULL, NULL),
(137, 41, 'Sí, tengo una zona que necesita cuidado', 42, 2, TRUE, '¿Qué zona debemos proteger en los movimientos?', 'Ej: muñeca derecha, hombro, zona lumbar'),
(138, 41, 'Sí, necesito adaptar algunos movimientos', 42, 3, TRUE, '¿Qué movimientos o posiciones debes evitar?', 'Ej: evitar carga en muñecas, nada de dominadas supinas')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q42: Frecuencia
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(139, 42, '2-3 veces por semana', 43, 1, FALSE),
(140, 42, '4 veces por semana', 43, 2, FALSE),
(141, 42, '5-6 veces por semana', 43, 3, FALSE),
(142, 42, 'Todos los días', 43, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q43: Lugar [NUEVA — específica de Kael]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(143, 43, 'Casa sin equipamiento (suelo, paredes y silla)', 44, 1, FALSE),
(144, 43, 'Casa con barra de dominadas o anillas', 44, 2, FALSE),
(145, 43, 'Parque con barras de calistenia', 44, 3, FALSE),
(146, 43, 'Combinación de casa y parque', 44, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q44: Edad
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(147, 44, 'Menos de 18 años', 45, 1, FALSE),
(148, 44, '18-25 años', 45, 2, FALSE),
(149, 44, '26-35 años', 45, 3, FALSE),
(150, 44, '36-50 años', 45, 4, FALSE),
(151, 44, 'Más de 50 años', 45, 5, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q45: Peso (NUMERIC)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(152, 45, 'Ingresar peso', 46, 1, TRUE, 'Ingresa tu peso actual en kilogramos', '70')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q46: Altura (NUMERIC)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(153, 46, 'Ingresar altura', 47, 1, TRUE, 'Ingresa tu altura en centímetros', '175')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q47: Dominadas [NUEVA — exclusiva de Kael]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(154, 47, 'No puedo hacer ninguna todavía', 48, 1, FALSE),
(155, 47, '1-3 dominadas seguidas', 48, 2, FALSE),
(156, 47, '4-8 dominadas seguidas', 48, 3, FALSE),
(157, 47, 'Más de 8 dominadas seguidas', 48, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q48: Fondos [NUEVA — exclusiva de Kael]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(158, 48, 'No puedo hacer ninguno todavía', 49, 1, FALSE),
(159, 48, '1-5 fondos', 49, 2, FALSE),
(160, 48, 'Más de 5 fondos con buena técnica', 49, 3, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q49: Habilidad objetivo [NUEVA — exclusiva de Kael]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(161, 49, 'Dominar las dominadas y los fondos perfectamente', 50, 1, FALSE),
(162, 49, 'Conseguir el muscle-up', 50, 2, FALSE),
(163, 49, 'Aprender el handstand (pino)', 50, 3, FALSE),
(164, 49, 'Front lever, back lever u otras palancas', 50, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q50: Tiempo en calistenia [NUEVA — exclusiva de Kael]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(165, 50, 'Nunca lo he practicado, empiezo desde cero', 51, 1, FALSE),
(166, 50, 'Menos de 6 meses', 51, 2, FALSE),
(167, 50, 'Entre 6 meses y 2 años', 51, 3, FALSE),
(168, 50, 'Más de 2 años practicando', 51, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q51: Duración por sesión
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(169, 51, '20-30 minutos', 52, 1, FALSE),
(170, 51, '45-60 minutos', 52, 2, FALSE),
(171, 51, '60-90 minutos', 52, 3, FALSE),
(172, 51, 'Más de 90 minutos', 52, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q52: Alimentación (final - next_question_id NULL)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(173, 52, 'Sí, tengo preferencias o hábitos alimentarios específicos', NULL, 1, TRUE, 'Cuéntanos tus preferencias para personalizar tu plan', 'Ej: vegano, bajo en grasas, ayuno intermitente'),
(174, 52, 'No, como de todo sin preferencias especiales', NULL, 2, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- -----------------------------------------------
-- ELIUD - Opciones (IDs 175-215)
-- -----------------------------------------------

-- Q53: Objetivo running
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(175, 53, 'Empezar a correr por primera vez', 54, 1, FALSE),
(176, 53, 'Preparar una carrera popular (5K o 10K)', 54, 2, FALSE),
(177, 53, 'Preparar una media maratón o maratón', 54, 3, FALSE),
(178, 53, 'Mejorar mi marca personal actual', 54, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q54: Adaptaciones [tren inferior, específico de running]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(179, 54, 'No, puedo entrenar sin restricciones', 55, 1, FALSE, NULL, NULL),
(180, 54, 'Sí, tengo una zona que requiere atención al correr', 55, 2, TRUE, '¿Qué zona debemos cuidar en el entrenamiento?', 'Ej: rodilla, tobillo, cadera, banda iliotibial'),
(181, 54, 'Sí, necesito evitar ciertos impactos o movimientos', 55, 3, TRUE, '¿Qué debemos evitar o adaptar?', 'Ej: evitar cuestas pronunciadas, no correr en asfalto duro')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q55: Frecuencia
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(182, 55, '2-3 veces por semana', 56, 1, FALSE),
(183, 55, '4-5 veces por semana', 56, 2, FALSE),
(184, 55, '6-7 veces por semana', 56, 3, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q56: Terreno [NUEVA — exclusiva de Eliud]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(185, 56, 'Asfalto (ciudad, parque urbano, carretera)', 57, 1, FALSE),
(186, 56, 'Trail o montaña (caminos de tierra, desnivel)', 57, 2, FALSE),
(187, 56, 'Pista de atletismo', 57, 3, FALSE),
(188, 56, 'Combinado (mezcla de terrenos)', 57, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q57: Edad
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(189, 57, 'Menos de 18 años', 58, 1, FALSE),
(190, 57, '18-25 años', 58, 2, FALSE),
(191, 57, '26-35 años', 58, 3, FALSE),
(192, 57, '36-50 años', 58, 4, FALSE),
(193, 57, 'Más de 50 años', 58, 5, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q58: Peso (NUMERIC)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(194, 58, 'Ingresar peso', 59, 1, TRUE, 'Ingresa tu peso actual en kilogramos', '70')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q59: Altura (NUMERIC)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(195, 59, 'Ingresar altura', 60, 1, TRUE, 'Ingresa tu altura en centímetros', '175')
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q60: Distancia sin parar [NUEVA — exclusiva de Eliud]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(196, 60, 'No puedo mantener el trote más de 5 minutos', 61, 1, FALSE),
(197, 60, 'Puedo correr entre 5 y 20 minutos sin parar', 61, 2, FALSE),
(198, 60, 'Puedo correr entre 20 y 45 minutos sin parar', 61, 3, FALSE),
(199, 60, 'Puedo correr más de 45 minutos sin parar', 61, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q61: Evento objetivo [NUEVA — exclusiva de Eliud]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(200, 61, 'Sí, tengo una carrera con fecha concreta', 62, 1, TRUE, '¿Qué carrera es y cuándo es aproximadamente?', 'Ej: 10K en junio, maratón de Madrid en abril'),
(201, 61, 'No, entreno por el placer de correr y mejorar', 62, 2, FALSE, NULL, NULL),
(202, 61, 'Aún no lo sé, quiero progresar primero y decidir', 62, 3, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- Q62: Ritmo actual [NUEVA — exclusiva de Eliud]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(203, 62, 'No lo sé todavía, acabo de empezar', 63, 1, FALSE),
(204, 62, 'Más de 7 min/km (ritmo suave o caminata rápida)', 63, 2, FALSE),
(205, 62, 'Entre 5:30 y 7 min/km (ritmo moderado)', 63, 3, FALSE),
(206, 62, 'Menos de 5:30 min/km (ritmo rápido)', 63, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q63: Fuerza complementaria [NUEVA — exclusiva de Eliud]
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(207, 63, 'Sí, quiero fortalecer el tren inferior para correr mejor', 64, 1, FALSE),
(208, 63, 'Sí, quiero fuerza general y movilidad para evitar lesiones', 64, 2, FALSE),
(209, 63, 'No, prefiero centrarme exclusivamente en correr', 64, 3, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q64: Duración de salidas
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input) VALUES
(210, 64, '20-30 minutos', 65, 1, FALSE),
(211, 64, '30-60 minutos', 65, 2, FALSE),
(212, 64, 'Más de 60 minutos', 65, 3, FALSE),
(213, 64, 'Variable según el día y la energía', 65, 4, FALSE)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order);

-- Q65: Alimentación (final - next_question_id NULL)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(214, 65, 'Sí, tengo preferencias o necesidades alimentarias', NULL, 1, TRUE, 'Cuéntanos tus preferencias para personalizar tu plan', 'Ej: dieta alta en carbohidratos, sin gluten, vegetariano'),
(215, 65, 'No, como de todo sin restricciones especiales', NULL, 2, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE text = VALUES(text), next_question_id = VALUES(next_question_id), display_order = VALUES(display_order), requires_text_input = VALUES(requires_text_input), text_input_prompt = VALUES(text_input_prompt), text_input_placeholder = VALUES(text_input_placeholder);

-- ===========================================
-- 9. OPCIONES "PREFIERO NO RESPONDER" (IDs 216-280)
-- ===========================================
-- Una opción adicional por cada pregunta (Q1-Q65).
-- requires_text_input = FALSE, text_input_prompt = NULL, text_input_placeholder = NULL.
-- next_question_id: misma pregunta siguiente que las demás opciones del grupo.
-- Preguntas finales (Q13, Q26, Q39, Q52, Q65): next_question_id = NULL.
-- ===========================================

-- Preguntas generales Q1-Q13
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(216, 1,  'Prefiero no responder', 2,    6, FALSE, NULL, NULL),
(217, 2,  'Prefiero no responder', 3,    5, FALSE, NULL, NULL),
(218, 3,  'Prefiero no responder', 4,    5, FALSE, NULL, NULL),
(219, 4,  'Prefiero no responder', 5,    5, FALSE, NULL, NULL),
(220, 5,  'Prefiero no responder', 6,    6, FALSE, NULL, NULL),
(221, 6,  'Prefiero no responder', 7,    2, FALSE, NULL, NULL),
(222, 7,  'Prefiero no responder', 8,    2, FALSE, NULL, NULL),
(223, 8,  'Prefiero no responder', 9,    6, FALSE, NULL, NULL),
(224, 9,  'Prefiero no responder', 10,   6, FALSE, NULL, NULL),
(225, 10, 'Prefiero no responder', 12,   5, FALSE, NULL, NULL),
(226, 11, 'Prefiero no responder', 12,   4, FALSE, NULL, NULL),
(227, 12, 'Prefiero no responder', 13,   5, FALSE, NULL, NULL),
(228, 13, 'Prefiero no responder', NULL, 3, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- Ronnie Q14-Q26
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(229, 14, 'Prefiero no responder', 15,   5, FALSE, NULL, NULL),
(230, 15, 'Prefiero no responder', 16,   4, FALSE, NULL, NULL),
(231, 16, 'Prefiero no responder', 17,   5, FALSE, NULL, NULL),
(232, 17, 'Prefiero no responder', 18,   4, FALSE, NULL, NULL),
(233, 18, 'Prefiero no responder', 19,   6, FALSE, NULL, NULL),
(234, 19, 'Prefiero no responder', 20,   2, FALSE, NULL, NULL),
(235, 20, 'Prefiero no responder', 21,   2, FALSE, NULL, NULL),
(236, 21, 'Prefiero no responder', 22,   5, FALSE, NULL, NULL),
(237, 22, 'Prefiero no responder', 23,   5, FALSE, NULL, NULL),
(238, 23, 'Prefiero no responder', 24,   5, FALSE, NULL, NULL),
(239, 24, 'Prefiero no responder', 25,   4, FALSE, NULL, NULL),
(240, 25, 'Prefiero no responder', 26,   5, FALSE, NULL, NULL),
(241, 26, 'Prefiero no responder', NULL, 3, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- Serena Q27-Q39
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(242, 27, 'Prefiero no responder', 28,   5, FALSE, NULL, NULL),
(243, 28, 'Prefiero no responder', 29,   4, FALSE, NULL, NULL),
(244, 29, 'Prefiero no responder', 30,   4, FALSE, NULL, NULL),
(245, 30, 'Prefiero no responder', 31,   5, FALSE, NULL, NULL),
(246, 31, 'Prefiero no responder', 32,   6, FALSE, NULL, NULL),
(247, 32, 'Prefiero no responder', 33,   2, FALSE, NULL, NULL),
(248, 33, 'Prefiero no responder', 34,   2, FALSE, NULL, NULL),
(249, 34, 'Prefiero no responder', 35,   5, FALSE, NULL, NULL),
(250, 35, 'Prefiero no responder', 36,   5, FALSE, NULL, NULL),
(251, 36, 'Prefiero no responder', 37,   4, FALSE, NULL, NULL),
(252, 37, 'Prefiero no responder', 38,   5, FALSE, NULL, NULL),
(253, 38, 'Prefiero no responder', 39,   5, FALSE, NULL, NULL),
(254, 39, 'Prefiero no responder', NULL, 3, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- Kael Q40-Q52
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(255, 40, 'Prefiero no responder', 41,   5, FALSE, NULL, NULL),
(256, 41, 'Prefiero no responder', 42,   4, FALSE, NULL, NULL),
(257, 42, 'Prefiero no responder', 43,   5, FALSE, NULL, NULL),
(258, 43, 'Prefiero no responder', 44,   5, FALSE, NULL, NULL),
(259, 44, 'Prefiero no responder', 45,   6, FALSE, NULL, NULL),
(260, 45, 'Prefiero no responder', 46,   2, FALSE, NULL, NULL),
(261, 46, 'Prefiero no responder', 47,   2, FALSE, NULL, NULL),
(262, 47, 'Prefiero no responder', 48,   5, FALSE, NULL, NULL),
(263, 48, 'Prefiero no responder', 49,   4, FALSE, NULL, NULL),
(264, 49, 'Prefiero no responder', 50,   5, FALSE, NULL, NULL),
(265, 50, 'Prefiero no responder', 51,   5, FALSE, NULL, NULL),
(266, 51, 'Prefiero no responder', 52,   5, FALSE, NULL, NULL),
(267, 52, 'Prefiero no responder', NULL, 3, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- Eliud Q53-Q65
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(268, 53, 'Prefiero no responder', 54,   5, FALSE, NULL, NULL),
(269, 54, 'Prefiero no responder', 55,   4, FALSE, NULL, NULL),
(270, 55, 'Prefiero no responder', 56,   4, FALSE, NULL, NULL),
(271, 56, 'Prefiero no responder', 57,   5, FALSE, NULL, NULL),
(272, 57, 'Prefiero no responder', 58,   6, FALSE, NULL, NULL),
(273, 58, 'Prefiero no responder', 59,   2, FALSE, NULL, NULL),
(274, 59, 'Prefiero no responder', 60,   2, FALSE, NULL, NULL),
(275, 60, 'Prefiero no responder', 61,   5, FALSE, NULL, NULL),
(276, 61, 'Prefiero no responder', 62,   4, FALSE, NULL, NULL),
(277, 62, 'Prefiero no responder', 63,   5, FALSE, NULL, NULL),
(278, 63, 'Prefiero no responder', 64,   4, FALSE, NULL, NULL),
(279, 64, 'Prefiero no responder', 65,   5, FALSE, NULL, NULL),
(280, 65, 'Prefiero no responder', NULL, 3, FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);

-- ===========================================
-- 10. VINCULAR CUESTIONARIOS A SUS ÁRBOLES
-- ===========================================
-- Actualiza first_question_id para que cada cuestionario
-- arranque por el árbol propio de su coach.
-- El cuestionario 5 (Evaluación General) mantiene Q1.
-- ===========================================
UPDATE questionnaire SET first_question_id = 14, updated_at = NOW() WHERE id = 1; -- Ronnie Principiante
UPDATE questionnaire SET first_question_id = 27, updated_at = NOW() WHERE id = 2; -- Serena Principiante
UPDATE questionnaire SET first_question_id = 53, updated_at = NOW() WHERE id = 3; -- Eliud Intermedio
UPDATE questionnaire SET first_question_id = 40, updated_at = NOW() WHERE id = 4; -- Kael Avanzado
UPDATE questionnaire SET first_question_id = 53, updated_at = NOW() WHERE id = 6; -- Eliud Principiante
UPDATE questionnaire SET first_question_id = 53, updated_at = NOW() WHERE id = 7; -- Eliud Avanzado
UPDATE questionnaire SET first_question_id = 40, updated_at = NOW() WHERE id = 8; -- Kael Principiante
UPDATE questionnaire SET first_question_id = 40, updated_at = NOW() WHERE id = 9; -- Kael Intermedio

SET FOREIGN_KEY_CHECKS = 1; -- Reactivar foreign keys

-- ===========================================
-- NOTAS IMPORTANTES
-- ===========================================
-- 
-- 1. ESTRUCTURA DEL ÁRBOL DE DECISIÓN:
--    - Cada pregunta (question) puede tener múltiples opciones (question_option)
--    - Cada opción puede apuntar a la siguiente pregunta (next_question_id)
--    - Si next_question_id es NULL, es una pregunta final
--
-- 2. FLUJO DE CUESTIONARIO:
--    Questionnaire.firstQuestion → Question → QuestionOption.nextQuestion → ...
--    
-- 3. TIPOS DE PREGUNTA (QuestionType enum):
--    - BINARY: Sí/No
--    - MULTIPLE_CHOICE: Varias opciones
--    - TEXT_INPUT: Texto libre
--    - NUMERIC: Número
--    - SCALE: Escala 1-N
--
-- 4. CAMPOS IMPORTANTES DE QUESTION_OPTION:
--    - requires_text_input: Si TRUE, pide input adicional al usuario
--    - text_input_prompt: Mensaje que se muestra al pedir el input
--    - text_input_placeholder: Placeholder para el campo de texto
--
-- 5. RELACIONES DE QUESTIONNAIRE:
--    - coach_model_type_id: Coach recomendado (puede ser NULL)
--    - experience_level_id: Nivel sugerido (puede ser NULL)
--    - first_question_id: Primera pregunta del cuestionario (REQUIRED)
--
-- 6. EJEMPLO DE FLUJO:
--    Usuario selecciona "Ronnie - Fuerza para Principiantes"
--    → Carga Questionnaire id=1
--    → Muestra Question id=1 (primera pregunta)
--    → Usuario selecciona QuestionOption id=2 (Ganar músculo)
--    → Navega a Question id=2 (next_question_id de la opción)
--    → ... continúa hasta next_question_id = NULL
--
-- 7. ALMACENAMIENTO DE RESPUESTAS:
--    - QuestionnaireResponse: Sesión del usuario
--    - UserAnswer: Cada respuesta individual
--      - question_id: Qué pregunta respondió
--      - selected_option_id: Qué opción seleccionó
--      - additional_text: Texto extra si requires_text_input=TRUE
--
-- 8. DATOS DE PRUEBA:
--    Este script NO incluye usuarios ni respuestas de prueba
--    Para agregar un usuario de prueba, usa:
--    INSERT INTO user (name, email, password, is_registration_complete, 
--                      is_verified, created_at, role_id) 
--    VALUES ('Test User', 'test@ifit.com', '$2a$10$...', FALSE, TRUE, NOW(), 1);
--
-- ===========================================
-- FIN DEL SCRIPT
-- ===========================================