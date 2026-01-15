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
(2, '¿Tienes alguna condición médica o lesión que debamos considerar?', 'MULTIPLE_CHOICE', TRUE, NOW()),
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
(13, '¿Tienes alguna preferencia alimentaria o restricción dietética?', 'TEXT_INPUT', TRUE, NOW())
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

-- Opciones para Pregunta 2: Condiciones médicas
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(6, 2, 'No tengo ninguna limitación', 3, 1, FALSE, NULL, NULL),
(7, 2, 'Tengo una lesión reciente', 3, 2, TRUE, '¿Podrías describir brevemente tu lesión?', 'Ej: Esguince de tobillo hace 2 meses'),
(8, 2, 'Tengo una condición médica crónica', 3, 3, TRUE, '¿Qué condición médica tienes? (Es importante para personalizar tu entrenamiento)', 'Ej: Diabetes, hipertensión, asma'),
(9, 2, 'Tengo limitaciones de movilidad', 3, 4, TRUE, 'Cuéntanos sobre tus limitaciones de movilidad', 'Ej: Problemas de rodilla, espalda')
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

-- Opciones para Pregunta 13: Restricciones dietéticas (pregunta final)
INSERT INTO question_option (id, question_id, text, next_question_id, display_order, requires_text_input, text_input_prompt, text_input_placeholder) VALUES
(46, 13, 'Sí, tengo restricciones', NULL, 1, TRUE, 'Por favor describe tus restricciones o preferencias alimentarias', 'Ej: Vegetariano, intolerancia a lactosa, bajo en carbohidratos'),
(47, 13, 'No, como de todo', NULL, 2, FALSE, NULL, NULL)
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