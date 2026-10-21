import { useEffect, useState } from 'react';
import { PolicyServicingPanel, type PolicyServicingSummary } from '../components/PolicyServicingPanel';

type Readback = { witnessId: string; value: string } | null;

/** Test transport harness only. It contains no policy lifecycle rules or learner UI. */
export function PolicyLifecycleBrowserHarness() {
  const [readback, setReadback] = useState<Readback>(null);
  const [message, setMessage] = useState('ready');
  const [flow, setFlow] = useState<string[]>([]);
  const [servicingState, setServicingState] = useState<'loading' | 'ready' | 'unauthenticated' | 'unavailable'>('loading');
  const [servicingSummary, setServicingSummary] = useState<PolicyServicingSummary | undefined>();

  async function load() {
    const response = await fetch('/test/day44/readback');
    if (!response.ok) throw new Error(`readback returned ${response.status}`);
    setReadback(await response.json() as Readback);
  }

  useEffect(() => { void load().catch((error: Error) => setMessage(error.message)); }, []);

  async function writeAndRead() {
    setMessage('writing');
    const response = await fetch('/test/day44/record', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ witnessId: 'lifecycle-smoke-v1', value: 'persisted-after-server-restart' }),
    });
    if (!response.ok) throw new Error(`write returned ${response.status}`);
    await load();
    setMessage('saved');
  }

  async function runLifecycleApiFlow() {
    const paths = [
      ['POST', '/test/day44/lifecycle/issue'],
      ['POST', '/test/day44/lifecycle/replay'],
      ['POST', '/test/day44/lifecycle/endorsement'],
      ['POST', '/test/day44/lifecycle/cancellation'],
      ['GET', '/test/day44/lifecycle/history'],
    ] as const;
    const results: string[] = [];
    for (const [method, path] of paths) {
      try {
        const response = await fetch(path, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: method === 'POST' ? '{}' : undefined,
        });
        let detail = '';
        if (response.headers.get('content-type')?.includes('application/json')) {
          const body = await response.json() as { status?: string };
          detail = body.status ? `:${body.status}` : '';
        }
        results.push(`${path}:${response.status}${detail}`);
      } catch (error) {
        results.push(`${path}:network-error:${String(error)}`);
      }
    }
    setFlow(results);
  }


  async function readServicingSummary() {
    setServicingState('loading');
    setServicingSummary(undefined);
    try {
      const response = await fetch('/test/day44/lifecycle/servicing/policy-a');
      if (response.status === 401) { setServicingState('unauthenticated'); return; }
      if (!response.ok) { setServicingState('unavailable'); return; }
      setServicingSummary(await response.json() as PolicyServicingSummary);
      setServicingState('ready');
    } catch {
      setServicingState('unavailable');
    }
  }

  async function runIssueAndEndorsement() {
    for (const path of ['/test/day44/lifecycle/issue', '/test/day44/lifecycle/replay', '/test/day44/lifecycle/endorsement']) {
      const response = await fetch(path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{}' });
      if (!response.ok) throw new Error(`${path} returned ${response.status}`);
    }
    await readServicingSummary();
  }

  async function applyCancellation() {
    const response = await fetch('/test/day44/lifecycle/cancellation', { method: 'POST' });
    if (!response.ok) throw new Error(`cancellation returned ${response.status}`);
    await readServicingSummary();
  }

  async function readLifecycleAfterRestart() {
    const response = await fetch('/test/day44/lifecycle/history');
    const body = await response.json() as { status: string; version: number; eventCount: number };
    setFlow([`/test/day44/lifecycle/history:${response.status}:${body.status}:v${body.version}:events${body.eventCount}`]);
  }

  return <main aria-label="Day 44 transport harness">
    <h1>Day 44 transport harness</h1>
    <button type="button" onClick={() => void writeAndRead().catch((error: Error) => setMessage(error.message))}>Write synthetic witness</button>
    <button type="button" onClick={() => void load().catch((error: Error) => setMessage(error.message))}>Read witness</button>
    <button type="button" onClick={() => void runLifecycleApiFlow()}>Run lifecycle API acceptance flow</button>
    <button type="button" onClick={() => void readLifecycleAfterRestart()}>Read policy after restart</button>
    <button type="button" onClick={() => void runIssueAndEndorsement().catch((error: Error) => setMessage(error.message))}>Run issue and endorsement</button>
    <button type="button" onClick={() => void readServicingSummary()}>Read servicing summary</button>
    <button type="button" onClick={() => void applyCancellation().catch((error: Error) => setMessage(error.message))}>Apply cancellation</button>
    <PolicyServicingPanel state={servicingState} summary={servicingSummary} />
    <p role="status">{message}</p>
    <output data-testid="readback">{readback ? `${readback.witnessId}:${readback.value}` : 'empty'}</output>
    <output data-testid="lifecycle-flow">{flow.join('|')}</output>
  </main>;
}
