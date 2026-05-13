import 'zone.js';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .then(() => console.log('✅ PharmaOnline bootstrapped'))
  .catch((err) => {
    console.error('❌ Bootstrap error:', err);
    document.body.innerHTML = `
      <div style="background:#1a1a2e;color:#e94560;font-family:monospace;padding:2rem;min-height:100vh;display:flex;flex-direction:column;align-items:center;justify-content:center;">
        <h2 style="color:white;margin-bottom:1rem">⚠️ Bootstrap Error</h2>
        <pre style="background:rgba(255,255,255,0.05);padding:1.5rem;border-radius:8px;max-width:800px;overflow:auto;color:#ff6b8a;font-size:0.85rem;white-space:pre-wrap;">${err?.message || err}</pre>
      </div>`;
  });
