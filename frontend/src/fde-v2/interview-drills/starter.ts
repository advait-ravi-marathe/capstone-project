/** Isolated role-bank exercises. Edit only as a timed drill; never as capstone code. */
import { createElement, useRef, useState } from 'react';

export function acceptsResponse(_currentRequestId: number, _responseRequestId: number): boolean {
  return true; // seeded defect: a late response can overwrite newer UI state
}

export function shouldRetry(status: number): boolean {
  return status >= 400; // seeded defect: retries permanent client errors
}

export function iconButtonProps(_label: string): { type: 'button'; 'aria-label'?: string } {
  return { type: 'button' }; // seeded defect: icon-only action has no accessible name
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

type RenderListProps = { items: string[]; selected: string; onRowRender: (item: string) => void };
function Row({ item, selected, onRowRender }: { item: string; selected: boolean; onRowRender: (item: string) => void }) {
  onRowRender(item);
  return createElement('li', { 'aria-selected': selected }, item);
}
// Seeded performance defect: every row executes when selection changes.
const ListRow = Row;
export function RenderList({ items, selected, onRowRender }: RenderListProps) {
  return createElement('ul', null, ...items.map(item => createElement(ListRow, {
    key: item, item, selected: item === selected, onRowRender,
  })));
}
