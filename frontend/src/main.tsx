import React from 'react';
import { createRoot } from 'react-dom/client';
import { ProductPanel } from './components/ProductPanel';
import { HomeSecureBrowserHarness } from './e2e/HomeSecureBrowserHarness';

const rootElement = document.getElementById('root');
if (!rootElement) throw new Error('Root element is missing');

createRoot(rootElement).render(
  <React.StrictMode>
    {window.location.pathname === '/__e2e/home-secure'
      ? <HomeSecureBrowserHarness />
      : <ProductPanel state={{ status: 'loading' }} />}
  </React.StrictMode>,
);
