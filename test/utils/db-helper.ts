import mysql from 'mysql2/promise';

/**
 * Database helper para acceso directo a MySQL en tests
 * Permite obtener códigos de verificación y datos de usuario directamente desde BD
 */

let connectionPool: mysql.Pool | null = null;

export async function initializePool(): Promise<mysql.Pool> {
  if (connectionPool) {
    return connectionPool;
  }

  connectionPool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '3306'),
    user: process.env.DB_ROOT_PASSWORD || 'root',
    password: process.env.DB_ROOT_PASSWORD || 'root',
    database: process.env.DB_NAME || 'ifit',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
  });

  return connectionPool;
}

/**
 * Obtener el código de verificación del último usuario creado con un email específico
 */
export async function getVerificationCode(email: string): Promise<string | null> {
  const pool = await initializePool();
  const connection = await pool.getConnection();

  try {
    const [rows] = await connection.query(
      'SELECT verification_code FROM user WHERE email = ? ORDER BY created_at DESC LIMIT 1',
      [email]
    );

    const result = rows as any[];
    return result.length > 0 ? result[0].verification_code : null;
  } finally {
    connection.release();
  }
}

/**
 * Obtener datos de usuario por email
 */
export async function getUserByEmail(email: string): Promise<any | null> {
  const pool = await initializePool();
  const connection = await pool.getConnection();

  try {
    const [rows] = await connection.query(
      'SELECT id, email, name, is_verified, is_registration_complete, coach_model_type_id, experience_level_id FROM user WHERE email = ? LIMIT 1',
      [email]
    );

    const result = rows as any[];
    return result.length > 0 ? result[0] : null;
  } finally {
    connection.release();
  }
}

/**
 * Obtener todos los usuarios con información de verificación
 */
export async function getAllUsers(): Promise<any[]> {
  const pool = await initializePool();
  const connection = await pool.getConnection();

  try {
    const [rows] = await connection.query(
      'SELECT id, email, name, is_verified, coach_model_type_id, experience_level_id FROM user ORDER BY created_at DESC'
    );

    return rows as any[];
  } finally {
    connection.release();
  }
}

/**
 * Obtener ejercicios del catálogo
 */
export async function getExerciseCatalog(): Promise<any[]> {
  const pool = await initializePool();
  const connection = await pool.getConnection();

  try {
    const [rows] = await connection.query(`
      SELECT
        id,
        exercise_name,
        category,
        level,
        sets,
        reps,
        rest_seconds,
        description
      FROM exercise_catalog
      ORDER BY level, category
    `);

    return rows as any[];
  } finally {
    connection.release();
  }
}

/**
 * Obtener respuestas de cuestionario del usuario
 */
export async function getUserQuestionnaireAnswers(userId: number): Promise<any[]> {
  const pool = await initializePool();
  const connection = await pool.getConnection();

  try {
    const [rows] = await connection.query(`
      SELECT
        q.id,
        q.question_text,
        qa.answer_value
      FROM questionnaire_answer qa
      JOIN questionnaire q ON qa.questionnaire_id = q.id
      WHERE qa.user_id = ?
      ORDER BY q.id
    `, [userId]);

    return rows as any[];
  } finally {
    connection.release();
  }
}

/**
 * Obtener tipo de coach disponibles
 */
export async function getCoachTypes(): Promise<any[]> {
  const pool = await initializePool();
  const connection = await pool.getConnection();

  try {
    const [rows] = await connection.query(`
      SELECT id, name, description
      FROM coach_model_type
      ORDER BY id
    `);

    return rows as any[];
  } finally {
    connection.release();
  }
}

/**
 * Obtener niveles de experiencia disponibles
 */
export async function getExperienceLevels(): Promise<any[]> {
  const pool = await initializePool();
  const connection = await pool.getConnection();

  try {
    const [rows] = await connection.query(`
      SELECT id, name, description
      FROM experience_level
      ORDER BY id
    `);

    return rows as any[];
  } finally {
    connection.release();
  }
}

/**
 * Limpiar recursos
 */
export async function closePool(): Promise<void> {
  if (connectionPool) {
    await connectionPool.end();
    connectionPool = null;
  }
}
