import React from 'react';
import { afterEach, describe, it, expect, vi } from 'vitest';
import { act, render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AssignmentApp from './ui.jsx';
afterEach(()=>{cleanup();vi.useRealTimers();});
const apiBase=()=>({submit:async()=>({accepted:true}),search:async()=>[],quote:async()=>({feeCents:100,version:'test-v1',status:200}),placements:async()=>[],poll:async()=>({state:'ready'})});

describe('learner-facing assignment components',()=>{
 it('renders all 30 role-specific task workspaces through the actual navigator',async()=>{
  const user=userEvent.setup();render(<AssignmentApp/>);
  expect(screen.getByRole('heading',{name:'Seller registration'})).toBeTruthy();
  for(let day=2;day<=30;day++){
   await user.selectOptions(screen.getByRole('combobox',{name:'Go to day'}),String(day));
   expect(screen.getByLabelText('Go to day').value).toBe(String(day));
   expect(document.querySelector(`[data-assignment-day="${day}"]`),`Day ${day} did not render its assignment workspace`).toBeTruthy();
  }
 });
 it('uses the Day 1 learner result to show errors, focus, then success',async()=>{
  const user=userEvent.setup();const day01=vi.fn(({email})=>email.includes('@')?{}:{email:'Enter a valid email'});
  render(<AssignmentApp exerciseFunctions={{day01}}/>);
  const email=screen.getByLabelText('Email address');await user.type(screen.getByLabelText('Seller name'),'Mina');await user.type(email,'bad');await user.click(screen.getByRole('button',{name:'Validate registration'}));
  expect(email.getAttribute('aria-invalid')).toBe('true');expect(screen.getByText('Enter a valid email')).toBeTruthy();expect(document.activeElement).toBe(email);
  await user.clear(email);await user.type(email,'mina@example.test');await user.click(screen.getByRole('button',{name:'Validate registration'}));
  expect(screen.getByText('Registration details accepted for this practice form.')).toBeTruthy();expect(day01).toHaveBeenCalledWith({name:'Mina',email:'mina@example.test'});
 });
 it('submits through the async port and lets the Day 3 reducer decide visible state',async()=>{
  let finish;const submit=vi.fn(()=>new Promise(resolve=>{finish=resolve;}));const user=userEvent.setup();
  const day03=vi.fn(({events})=>events.reduce((state,event)=>({SUBMIT:'submitting',RESOLVE:'success',REJECT:'error',VALIDATION_ERROR:'validation',RESET:'idle'}[event]||state), 'idle'));
  render(<AssignmentApp initialDay={3} api={{...apiBase(),submit}} exerciseFunctions={{day03}}/>);
  await user.type(screen.getByLabelText('Seller name'),'Seller');await user.click(screen.getByRole('button',{name:'Submit'}));
  expect(screen.getByRole('button',{name:'Submitting…'}).disabled).toBe(true);expect(submit).toHaveBeenCalledTimes(1);
  finish({accepted:true});await waitFor(()=>expect(screen.getAllByRole('status').some(x=>x.textContent.includes('success'))).toBe(true));expect(day03).toHaveBeenCalledWith({events:['SUBMIT','RESOLVE']});
 });
 it('exposes a retry action when the Day 3 learner reducer returns retryable',async()=>{
  const submit=vi.fn().mockRejectedValueOnce(Error('offline')).mockResolvedValueOnce({accepted:true});const user=userEvent.setup();
  const day03=({events})=>events.reduce((state,event)=>({SUBMIT:'submitting',RESOLVE:'success',REJECT:'retryable',VALIDATION_ERROR:'validation',RESET:'idle'}[event]||state),'idle');
  render(<AssignmentApp initialDay={3} api={{...apiBase(),submit}} exerciseFunctions={{day03}}/>);await user.type(screen.getByLabelText('Seller name'),'Seller');await user.click(screen.getByRole('button',{name:'Submit'}));
  await user.click(await screen.findByRole('button',{name:'Retry submission'}));await waitFor(()=>expect(screen.getAllByRole('status').some(x=>x.textContent.includes('success'))).toBe(true));expect(submit).toHaveBeenCalledTimes(2);
 });
 it('uses the Day 11 async search port, then delegates matching options to the learner function',async()=>{
  const search=vi.fn(async()=>[{id:'1',name:'North Storage'},{id:'2',name:'South Depot'}]);const day11=vi.fn(({query,options})=>options.filter(x=>x.toLowerCase().startsWith(query.toLowerCase())).sort());const user=userEvent.setup();
  render(<AssignmentApp initialDay={11} api={{...apiBase(),search}} exerciseFunctions={{day11}}/>);
  await user.type(screen.getByRole('combobox',{name:'Find a placement'}),'North');await waitFor(()=>expect(screen.getByRole('option',{name:'North Storage'})).toBeTruthy());
  expect(screen.queryByRole('option',{name:'South Depot'})).toBeNull();expect(search).toHaveBeenCalledTimes(1);expect(day11).toHaveBeenCalledWith({query:'North',options:['North Storage','South Depot']});
 });
 it('passes quote cents using the Day 12 contract and renders learner-computed totals',async()=>{
  const day12=vi.fn(({rows,feeCents})=>({subtotalCents:rows.reduce((n,r)=>n+r.cents,0),totalCents:rows.reduce((n,r)=>n+r.cents,0)+feeCents}));const quote=vi.fn(async()=>({feeCents:500,version:'q1'}));
  render(<AssignmentApp initialDay={12} api={{...apiBase(),quote}} exerciseFunctions={{day12}}/>);
  await waitFor(()=>expect(day12).toHaveBeenCalledWith({rows:[{cents:124000}],feeCents:500}));expect(screen.getByText(/total ₹1245\.00/i)).toBeTruthy();
 });
 it('delegates delayed response arbitration to Day 17 and preserves highest request sequence',async()=>{
  const pending=[];const placements=vi.fn(query=>new Promise(resolve=>pending.push({query,resolve})));
  const day17=vi.fn(({responses})=>[...responses].sort((a,b)=>b.seq-a.seq)[0]?.value);const api={...apiBase(),placements};const user=userEvent.setup();
  render(<AssignmentApp initialDay={17} api={api} exerciseFunctions={{day17}}/>);await waitFor(()=>expect(pending.length).toBe(1));
  fireEvent.change(screen.getByRole('textbox',{name:'Filter placements'}),{target:{value:'North'}});await waitFor(()=>expect(pending.length).toBe(2));
  pending[1].resolve([{id:'new',name:'New result'}]);await waitFor(()=>expect(screen.getByText('New result')).toBeTruthy());
  pending[0].resolve([{id:'old',name:'Old stale result'}]);await waitFor(()=>expect(day17).toHaveBeenCalledTimes(2));
  expect(day17.mock.calls[1][0].responses.map(x=>x.seq).sort()).toEqual([1,2]);expect(screen.queryByText('Old stale result')).toBeNull();
 });
 it('uses Day 18 status mapping to drive retryable and ready views',async()=>{
  let calls=0;const quote=vi.fn(async()=>{calls++;if(calls===1)throw Object.assign(Error('offline'),{status:503});return {feeCents:250,version:'v2',status:200};});
  const day18=({statuses})=>statuses.map(x=>x==='loading'?'loading':x===200?'ready':x===503?'retryable':'error');const user=userEvent.setup();
  render(<AssignmentApp initialDay={18} api={{...apiBase(),quote}} exerciseFunctions={{day18}}/>);await waitFor(()=>expect(screen.getByRole('button',{name:'Retry'})).toBeTruthy());
  await user.click(screen.getByRole('button',{name:'Retry'}));await waitFor(()=>expect(screen.getByText(/Ready · v2/)).toBeTruthy());
 });
 it('uses Day 23 delay output for bounded, cancellable polling',async()=>{
  vi.useFakeTimers();const poll=vi.fn().mockResolvedValueOnce({state:'pending'}).mockResolvedValueOnce({state:'pending'}).mockResolvedValueOnce({state:'ready'});
  const day23=vi.fn(({attempts})=>Array.from({length:attempts},(_,i)=>100*(i+1)));const user=userEvent.setup({advanceTimers:vi.advanceTimersByTime});
  render(<AssignmentApp initialDay={23} api={{...apiBase(),poll}} exerciseFunctions={{day23}}/>);fireEvent.click(screen.getByRole('button',{name:'Start bounded polling'}));
  await act(async()=>{await vi.advanceTimersByTimeAsync(99);});expect(poll).toHaveBeenCalledTimes(0);
  await act(async()=>{await vi.advanceTimersByTimeAsync(1);});expect(poll).toHaveBeenCalledTimes(1);
  await act(async()=>{await vi.advanceTimersByTimeAsync(199);});expect(poll).toHaveBeenCalledTimes(1);
  await act(async()=>{await vi.advanceTimersByTimeAsync(1);});expect(poll).toHaveBeenCalledTimes(2);
  await act(async()=>{await vi.advanceTimersByTimeAsync(300);});expect(poll).toHaveBeenCalledTimes(3);expect(screen.getAllByRole('status').some(x=>x.textContent.includes('ready'))).toBe(true);
 });
 it('uses Day 28 selected-index output to move the active tab',async()=>{
  const day28=vi.fn(({tabs,active})=>Math.max(0,tabs.indexOf(active)));const user=userEvent.setup();render(<AssignmentApp initialDay={28} exerciseFunctions={{day28}}/>);
  screen.getByRole('tab',{name:'overview'}).focus();await user.keyboard('{ArrowRight}');await waitFor(()=>expect(screen.getByRole('tab',{name:'inventory'}).getAttribute('aria-selected')).toBe('true'));
  expect(screen.getByRole('tabpanel',{name:'inventory'}).hidden).toBe(false);expect(day28).toHaveBeenCalledWith({tabs:['overview','inventory','quotes'],active:'inventory'});
 });
 it('renders markup-looking seller text inertly through React text nodes',async()=>{
  const day20=vi.fn(({text})=>text);const user=userEvent.setup();render(<AssignmentApp initialDay={20} exerciseFunctions={{day20}}/>);
  expect(screen.queryByRole('img')).toBeNull();await user.click(screen.getByRole('button',{name:'Render as safe text'}));
  expect((await screen.findAllByText('<img src="x">')).length).toBeGreaterThan(0);expect(screen.queryByRole('img')).toBeNull();expect(day20).toHaveBeenCalledWith({text:'<img src="x">'});
 });
 it('highlights all weakest topics while showing attempted and passed progress',async()=>{
  const day30=vi.fn(({attempts})=>attempts.filter(x=>x.topic==='async').map(x=>x.topic));const user=userEvent.setup();render(<AssignmentApp initialDay={30} exerciseFunctions={{day30}}/>);
  expect(screen.getByText(/async: 1\/4 passed/)).toBeTruthy();await user.click(screen.getByRole('button',{name:'Find weakest topics'}));
  expect((await screen.findByLabelText('Weakest practice area')).textContent).toContain('async: 1/4 passed');expect(day30).toHaveBeenCalledWith({attempts:[{topic:'forms',attempted:5,passed:4},{topic:'async',attempted:4,passed:1},{topic:'security',attempted:2,passed:1}]});
 });
});
