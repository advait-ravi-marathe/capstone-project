import React from 'react';
import { createRoot } from 'react-dom/client';
import { LocalCoverStoryApp } from './local/LocalCoverStoryApp';

const root = document.getElementById('root');
if (!root) throw new Error('Local application root is missing');

createRoot(root).render(
  <React.StrictMode>
    <LocalCoverStoryApp />
  </React.StrictMode>,
);
