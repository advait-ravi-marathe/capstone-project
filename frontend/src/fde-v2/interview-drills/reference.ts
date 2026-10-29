/** Instructor reference fixes for isolated role-bank exercises. */
import { createElement, useRef, useState } from 'react';

export function acceptsResponse(currentRequestId: number, responseRequestId: number): boolean {
  return currentRequestId === responseRequestId;
}

export function shouldRetry(status: number): boolean {
  return status === 429 || status >= 500;
}

export function iconButtonProps(label: string): { type: 'button'; 'aria-label': string } {
  if (!label.trim()) throw new Error('accessible label required');
  return { type: 'button', 'aria-label': label };
}

export function SearchPanel({ search }: { search: (query: string) => Promise<{ requestId: number; result: string }> }) {
  const [query, setQuery] = useState('');
  const [result, setResult] = useState('');
  const latestRequest = useRef(0);
  const submit = async () => {
    ++latestRequest.current;
    const response = await search(query);
    if (acceptsResponse(latestRequest.current, response.requestId)) setResult(response.result);
  };
  return createElement('section',null,
    createElement('label',null,'Search ',createElement('input',{value:query,onChange:(event:any)=>setQuery(event.target.value)})),
    createElement('button',{onClick:submit},'Search'),
    createElement('button',{...iconButtonProps('Clear search'),onClick:()=>setResult('')},'×'),
    createElement('output',{'aria-live':'polite'},result));
}
