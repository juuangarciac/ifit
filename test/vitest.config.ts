import { defineConfig } from 'vitest/config';
import path from 'path';

export default defineConfig({
  test: {
    globals: true,
    environment: 'node',
    testTimeout: 30000,
    hookTimeout: 30000,
    teardownTimeout: 10000,
    isolate: true,
    threads: true,
    maxThreads: 4,
    minThreads: 1,
    setupFiles: ['./setup.ts'],
    include: ['./**/*.spec.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      include: ['./**/*.spec.ts'],
    },
    reporters: ['verbose'],
  },
  resolve: {
    alias: {
      '@test': path.resolve(__dirname, '.'),
      '@utils': path.resolve(__dirname, './utils'),
    },
  },
});
