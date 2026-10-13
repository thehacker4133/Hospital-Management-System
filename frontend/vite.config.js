import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The React plugin is required: the .jsx files do not import React themselves.
export default defineConfig({
  plugins: [react()],
  server: { port: 5173 }
});
