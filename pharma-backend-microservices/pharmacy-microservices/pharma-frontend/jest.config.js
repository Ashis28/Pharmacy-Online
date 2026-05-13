/** @type {import('jest').Config} */
module.exports = {
  preset: 'ts-jest/presets/js-with-ts-esm',
  testEnvironment: 'jsdom',
  roots: ['<rootDir>/src'],
  testMatch: ['**/*.spec.ts'],
  extensionsToTreatAsEsm: ['.ts'],
  moduleNameMapper: {
    '^(\\.{1,2}/.*)\\.js$': '$1',
    '^../../environments/environment$': '<rootDir>/src/environments/environment.ts',
    '^../environments/environment$':    '<rootDir>/src/environments/environment.ts',
    // Stub Angular decorators and DI — we don't need them in unit tests
    '^@angular/core$':          '<rootDir>/src/__mocks__/@angular/core.ts',
    '^@angular/common$':        '<rootDir>/src/__mocks__/@angular/common.ts',
    '^@angular/common/http$':   '<rootDir>/src/__mocks__/@angular/common/http.ts',
    '^@angular/forms$':         '<rootDir>/src/__mocks__/@angular/forms.ts',
    '^@angular/router$':        '<rootDir>/src/__mocks__/@angular/router.ts',
    '^@angular/platform-browser$': '<rootDir>/src/__mocks__/@angular/platform-browser.ts',
  },
  transform: {
    '^.+\\.ts$': ['ts-jest', {
      useESM: false,
      tsconfig: {
        strict: false,
        esModuleInterop: true,
        allowSyntheticDefaultImports: true,
        experimentalDecorators: true,
        emitDecoratorMetadata: true,
        module: 'commonjs',
        target: 'es2020',
      }
    }]
  },
  verbose: true,
  collectCoverageFrom: [
    'src/app/**/*.ts',
    '!src/app/**/*.html',
    '!src/main.ts',
    '!src/app/app.routes.ts',
    '!src/app/app.config.ts',
    '!src/app/app.ts',
  ],
  coverageReporters: ['text', 'text-summary'],
};
