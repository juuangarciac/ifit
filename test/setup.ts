import { config } from 'dotenv';
import path from 'path';

// Cargar variables de entorno desde .env.test
config({ path: path.resolve(__dirname, '.env.test') });

// Global setup para todos los tests
beforeAll(async () => {
  console.log('🧪 Starting iFit API Test Suite...');
  console.log(`📍 Gateway: ${process.env.GATEWAY_BASE_URL}`);
  console.log(`📍 iFit API: ${process.env.IFIT_BASE_URL}`);
  console.log(`📍 Ronnie: ${process.env.RONNIE_BASE_URL}`);
});

afterAll(async () => {
  console.log('✅ Test Suite Completed');
});
