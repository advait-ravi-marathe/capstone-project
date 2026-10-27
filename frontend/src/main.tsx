import React from 'react';
import { createRoot } from 'react-dom/client';
import { ProductPanel } from './components/ProductPanel';
import { HomeSecureBrowserHarness } from './e2e/HomeSecureBrowserHarness';
import { PolicyLifecycleBrowserHarness } from './e2e/PolicyLifecycleBrowserHarness';
import { PolicyRenewalPreviewBrowserHarness } from './e2e/PolicyRenewalPreviewBrowserHarness';
import { AiFirstCoverStoryApp } from './ai/AiFirstCoverStoryApp';

const rootElement = document.getElementById('root');
if (!rootElement) throw new Error('Root element is missing');
const aiFirstRoute = window.location.hash === '#/ai-first/coverstory';

createRoot(rootElement).render(
  <React.StrictMode>
    {aiFirstRoute
      ? <AiFirstCoverStoryApp />
      : window.location.pathname === '/__e2e/day45-renewal-preview'
      ? <PolicyRenewalPreviewBrowserHarness />
      : window.location.pathname === '/__e2e/day44-lifecycle'
      ? <PolicyLifecycleBrowserHarness />
      : window.location.pathname === '/__e2e/home-secure'
      ? <HomeSecureBrowserHarness />
      : <ProductPanel state={{ status: 'loading' }} />}
  </React.StrictMode>,
);
