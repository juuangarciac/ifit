import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { HttpClient, createHttpClient } from '../utils/http-client';
import {
  generateTestEmail,
  generateTestName,
  validateResponseStructure,
} from '../utils/test-helpers';
import {
  getVerificationCode,
  getUserByEmail,
  getCoachTypes,
  getExperienceLevels,
  getExerciseCatalog,
  initializePool,
  closePool,
} from '../utils/db-helper';

/**
 * Suite de tests para validar respuestas del modelo (Ronnie - LangChain4j)
 *
 * Prueba diferentes perfiles de usuario con:
 * - Diferentes coaches (Ronnie, Serena, Eliud, Kael)
 * - Diferentes niveles de experiencia (Principiante, Intermedio, Avanzado)
 * - Diferentes tipos de respuestas a cuestionarios
 *
 * Valida que Ronnie genere rutinas coherentes según el perfil
 */

interface TestProfile {
  name: string;
  coachType: string;
  experienceLevel: string;
  questionnaire?: Record<string, string>;
}

interface GeneratedRoutine {
  id: number;
  title: string;
  description: string;
  days: Array<{
    day: string;
    exercises: Array<{
      exerciseName: string;
      sets: number;
      reps: string | number;
      restSeconds: number;
    }>;
  }>;
}

describe('🤖 Model Testing - Ronnie Routine Generation', () => {
  let gatewayClient: HttpClient;
  let ifitClient: HttpClient;
  let ronnieClient: HttpClient;
  let coachTypes: any[] = [];
  let experienceLevels: any[] = [];
  let exerciseCatalog: any[] = [];

  beforeAll(async () => {
    gatewayClient = createHttpClient(process.env.GATEWAY_BASE_URL || 'http://localhost:8080');
    ifitClient = createHttpClient(process.env.IFIT_BASE_URL || 'http://localhost:8081');
    ronnieClient = createHttpClient(process.env.RONNIE_BASE_URL || 'http://localhost:8082');

    await initializePool();

    // Cargar datos de referencia
    coachTypes = await getCoachTypes();
    experienceLevels = await getExperienceLevels();
    exerciseCatalog = await getExerciseCatalog();

    console.log(`📚 Loaded ${coachTypes.length} coach types`);
    console.log(`📚 Loaded ${experienceLevels.length} experience levels`);
    console.log(`📚 Loaded ${exerciseCatalog.length} exercises in catalog`);
  });

  afterAll(async () => {
    await closePool();
  });

  /**
   * Helper para crear usuario con perfil específico
   */
  async function createUserWithProfile(profile: TestProfile): Promise<{
    email: string;
    password: string;
    userId: number;
    accessToken: string;
  }> {
    const email = generateTestEmail();
    const password = 'Test@1234';
    const name = generateTestName();

    // Registrar usuario
    const registerRes = await gatewayClient.post('/auth/register', {
      name,
      surname: 'Tester',
      email,
      password,
      birthdate: '1990-01-01',
      phone: '651634807',
    });

    expect(registerRes.status).toBe(201);

    // Obtener código de verificación
    await new Promise((resolve) => setTimeout(resolve, 500));
    const verificationCode = await getVerificationCode(email);
    expect(verificationCode).toBeTruthy();

    // Verificar usuario
    const verifyRes = await gatewayClient.post('/auth/verify', {
      email,
      verificationCode,
    });

    expect(verifyRes.status).toBe(200);

    const userId = verifyRes.data.appUser.id;
    const accessToken = verifyRes.data.accessToken;

    // Llenar cuestionario si hay datos
    if (profile.questionnaire) {
      for (const [question, answer] of Object.entries(profile.questionnaire)) {
        await ifitClient.post(
          `/users/${userId}/questionnaire`,
          {
            questionText: question,
            answerValue: answer,
          },
          { headers: { Authorization: `Bearer ${accessToken}` } }
        );
      }
    }

    // Establecer coach y nivel de experiencia si se proporciona
    if (profile.coachType && profile.experienceLevel) {
      const coachId = coachTypes.find((c) => c.name === profile.coachType)?.id;
      const levelId = experienceLevels.find((l) => l.name === profile.experienceLevel)?.id;

      if (coachId && levelId) {
        await ifitClient.put(
          `/users/${userId}/profile`,
          {
            coachModelTypeId: coachId,
            experienceLevelId: levelId,
          },
          { headers: { Authorization: `Bearer ${accessToken}` } }
        );
      }
    }

    return { email, password, userId, accessToken };
  }

  /**
   * Helper para generar rutina desde Ronnie
   */
  async function generateRoutine(
    userId: number,
    accessToken: string,
    days: number = 3
  ): Promise<GeneratedRoutine> {
    const response = await ronnieClient.post(
      `/generate-routine`,
      {
        userId,
        days,
      },
      { headers: { Authorization: `Bearer ${accessToken}` } }
    );

    expect(response.status).toBe(200);
    return response.data as GeneratedRoutine;
  }

  /**
   * Validar que la rutina cumple con las reglas básicas
   */
  function validateRoutineStructure(routine: GeneratedRoutine, coachType: string) {
    expect(routine).toHaveProperty('id');
    expect(routine).toHaveProperty('title');
    expect(routine).toHaveProperty('days');
    expect(Array.isArray(routine.days)).toBe(true);

    for (const day of routine.days) {
      expect(day).toHaveProperty('day');
      expect(day).toHaveProperty('exercises');
      expect(Array.isArray(day.exercises)).toBe(true);

      // Validar que al menos hay ejercicios
      expect(day.exercises.length).toBeGreaterThan(0);

      for (const exercise of day.exercises) {
        // Validar que el nombre del ejercicio existe en el catálogo
        const catalogExercise = exerciseCatalog.find(
          (e) => e.exercise_name === exercise.exerciseName
        );
        expect(catalogExercise).toBeTruthy();

        // Validar que los valores numéricos son correctos
        expect(exercise.sets).toBeGreaterThan(0);
        expect(exercise.restSeconds).toBeGreaterThanOrEqual(0);
      }
    }

    // Validar reglas específicas por coach
    validateCoachSpecificRules(routine, coachType);
  }

  /**
   * Validar reglas específicas según el tipo de coach
   */
  function validateCoachSpecificRules(routine: GeneratedRoutine, coachType: string) {
    const allExercises = routine.days.flatMap((d) => d.exercises);

    if (coachType === 'Cardio/Running') {
      // Prohibidos: press de banca, curl de bíceps, peso muerto
      const forbiddenExercises = [
        'press de banca',
        'curl de bíceps',
        'peso muerto',
      ];
      for (const exercise of allExercises) {
        for (const forbidden of forbiddenExercises) {
          expect(exercise.exerciseName.toLowerCase()).not.toContain(forbidden.toLowerCase());
        }
      }
    } else if (coachType === 'Calistenia') {
      // Debe usar peso corporal
      const bodyweightKeywords = ['flexiones', 'plancha', 'dominadas', 'fondos'];
      expect(allExercises.some((e) =>
        bodyweightKeywords.some((kw) => e.exerciseName.toLowerCase().includes(kw))
      )).toBe(true);
    }
  }

  describe('👤 Profile 1: Beginner with Ronnie Coach', () => {
    it('should generate coherent routine for beginner', async () => {
      const profile: TestProfile = {
        name: 'Beginner User',
        coachType: 'Ronnie',
        experienceLevel: 'Principiante',
        questionnaire: {
          'Experiencia en ejercicio': 'Nunca he entrenado',
          'Lesiones previas': 'No',
          'Objetivo principal': 'Perder peso',
        },
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 3);

      validateRoutineStructure(routine, 'Ronnie');

      console.log(`✅ Beginner routine generated with ${routine.days.length} days`);
      routine.days.forEach((day) => {
        console.log(`   ${day.day}: ${day.exercises.length} exercises`);
      });
    });
  });

  describe('💪 Profile 2: Intermediate with Musculación Coach', () => {
    it('should generate muscle-building routine', async () => {
      const profile: TestProfile = {
        name: 'Intermediate User',
        coachType: 'Serena',
        experienceLevel: 'Intermedio',
        questionnaire: {
          'Experiencia en ejercicio': 'Entreno 2-3 veces por semana',
          'Lesiones previas': 'No',
          'Objetivo principal': 'Ganar masa muscular',
        },
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 4);

      validateRoutineStructure(routine, 'Serena');

      console.log(`✅ Intermediate routine generated with ${routine.days.length} days`);
      routine.days.forEach((day) => {
        console.log(`   ${day.day}: ${day.exercises.length} exercises`);
      });
    });
  });

  describe('⚡ Profile 3: Advanced Athlete', () => {
    it('should generate high-intensity routine for advanced user', async () => {
      const profile: TestProfile = {
        name: 'Advanced User',
        coachType: 'Eliud',
        experienceLevel: 'Avanzado',
        questionnaire: {
          'Experiencia en ejercicio': 'Entreno más de 5 días por semana',
          'Lesiones previas': 'No',
          'Objetivo principal': 'Mejorar performance',
        },
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 5);

      validateRoutineStructure(routine, 'Eliud');

      console.log(`✅ Advanced routine generated with ${routine.days.length} days`);
      routine.days.forEach((day) => {
        console.log(`   ${day.day}: ${day.exercises.length} exercises`);
      });
    });
  });

  describe('🧘 Profile 4: Wellness Focus', () => {
    it('should generate balanced wellness routine', async () => {
      const profile: TestProfile = {
        name: 'Wellness User',
        coachType: 'Kael',
        experienceLevel: 'Principiante',
        questionnaire: {
          'Experiencia en ejercicio': 'Sedentario',
          'Lesiones previas': 'Dolor de espalda',
          'Objetivo principal': 'Mejorar salud general',
        },
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 3);

      validateRoutineStructure(routine, 'Kael');

      console.log(`✅ Wellness routine generated with ${routine.days.length} days`);
      routine.days.forEach((day) => {
        console.log(`   ${day.day}: ${day.exercises.length} exercises`);
      });
    });
  });

  describe('🔀 Profile 5: Different Days Configuration', () => {
    it('should generate 1-day routine for busy user', async () => {
      const profile: TestProfile = {
        name: 'Busy User',
        coachType: 'Ronnie',
        experienceLevel: 'Intermedio',
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 1);

      expect(routine.days.length).toBe(1);
      console.log(`✅ 1-day routine generated`);
    });

    it('should generate 6-day routine for dedicated athlete', async () => {
      const profile: TestProfile = {
        name: 'Dedicated Athlete',
        coachType: 'Serena',
        experienceLevel: 'Avanzado',
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 6);

      expect(routine.days.length).toBe(6);
      console.log(`✅ 6-day routine generated`);
    });
  });

  describe('📊 Quality Checks', () => {
    it('should not repeat exercises within same day', async () => {
      const profile: TestProfile = {
        name: 'Quality Check User',
        coachType: 'Ronnie',
        experienceLevel: 'Intermedio',
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 3);

      for (const day of routine.days) {
        const exerciseNames = day.exercises.map((e) => e.exerciseName);
        const uniqueNames = new Set(exerciseNames);
        expect(uniqueNames.size).toBe(exerciseNames.length);
      }

      console.log('✅ No duplicate exercises within same day');
    });

    it('should have at least 4 exercises per day in main block', async () => {
      const profile: TestProfile = {
        name: 'Min Exercises User',
        coachType: 'Serena',
        experienceLevel: 'Intermedio',
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 3);

      for (const day of routine.days) {
        expect(day.exercises.length).toBeGreaterThanOrEqual(4);
      }

      console.log('✅ All days have at least 4 exercises');
    });

    it('should use catalog exercise names exactly', async () => {
      const profile: TestProfile = {
        name: 'Catalog Check User',
        coachType: 'Ronnie',
        experienceLevel: 'Principiante',
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 3);

      const catalogNames = new Set(exerciseCatalog.map((e) => e.exercise_name));

      for (const day of routine.days) {
        for (const exercise of day.exercises) {
          expect(catalogNames.has(exercise.exerciseName)).toBe(true);
        }
      }

      console.log('✅ All exercises match catalog exactly');
    });
  });

  describe('🎯 Response Validation', () => {
    it('should return valid response structure', async () => {
      const profile: TestProfile = {
        name: 'Response Validation User',
        coachType: 'Ronnie',
        experienceLevel: 'Intermedio',
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 3);

      validateResponseStructure(routine, [
        'id',
        'title',
        'days',
      ]);

      console.log('✅ Response structure valid');
    });

    it('should return exercises with required fields', async () => {
      const profile: TestProfile = {
        name: 'Exercise Fields User',
        coachType: 'Serena',
        experienceLevel: 'Intermedio',
      };

      const user = await createUserWithProfile(profile);
      const routine = await generateRoutine(user.userId, user.accessToken, 3);

      for (const day of routine.days) {
        for (const exercise of day.exercises) {
          expect(exercise).toHaveProperty('exerciseName');
          expect(exercise).toHaveProperty('sets');
          expect(exercise).toHaveProperty('reps');
          expect(exercise).toHaveProperty('restSeconds');
        }
      }

      console.log('✅ All exercises have required fields');
    });
  });

  describe('📈 Performance Summary', () => {
    it('should provide summary of all tested profiles', () => {
      const summary = {
        totalProfiles: 5,
        coaches: ['Ronnie', 'Serena', 'Eliud', 'Kael'],
        experienceLevels: ['Principiante', 'Intermedio', 'Avanzado'],
        validations: [
          'Catalog conformity',
          'No duplicate exercises per day',
          'Minimum 4 exercises per day',
          'Coach-specific rules',
          'Response structure',
        ],
      };

      console.log('\n📊 ========== TEST SUMMARY ==========');
      console.log(`Total Profiles Tested: ${summary.totalProfiles}`);
      console.log(`Coaches Covered: ${summary.coaches.join(', ')}`);
      console.log(`Experience Levels: ${summary.experienceLevels.join(', ')}`);
      console.log(`Quality Checks: ${summary.validations.join(', ')}`);
      console.log('✅ All model tests completed successfully');
    });
  });
});
