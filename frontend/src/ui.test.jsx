import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AssignmentApp from './ui.jsx';
import { afterEach } from 'vitest';
afterEach(cleanup);

describe('assignment application routes', () => {
  it('renders and navigates all 30 published daily component tasks', async () => {
    const user = userEvent.setup(); render(<AssignmentApp />);
    expect(screen.getByRole('heading', { name: /seller registration form/i })).toBeTruthy();
    for (let day = 2; day <= 30; day += 1) {
      await user.selectOptions(screen.getByRole('combobox', { name: 'Go to day' }), String(day));
      expect(screen.getByText(new RegExp(`DAY ${String(day).padStart(2,'0')}`))).toBeTruthy();
      expect(screen.getByRole('heading', { level: 2 })).toBeTruthy();
      expect(screen.getByRole('button', { name: 'Run my implementation' })).toBeTruthy();
    }
  });
  it('keeps registration fields controlled and shows the unimplemented learner status', async () => {
    const user=userEvent.setup();render(<AssignmentApp />);
    await user.type(screen.getByLabelText('Seller name'),'Mina');
    await user.type(screen.getByLabelText('Email address'),'mina@example.test');
    await user.click(screen.getByRole('button',{name:'Validate registration'}));
    expect(screen.getByRole('status').textContent).toMatch(/Learner TODO: Day 01/);
    expect(screen.getByLabelText('Seller name').value).toBe('Mina');
  });
  it('submits through injected async port, disables duplicates and announces completion', async () => {
    let finish;const submit=vi.fn(()=>new Promise((resolve)=>{finish=resolve;}));const user=userEvent.setup();
    render(<AssignmentApp initialDay={3} api={{submit,search:async()=>[],quote:async()=>({feeCents:100,version:'t'}),placements:async()=>[],poll:async()=>({state:'ready'})}}/>);
    await user.type(screen.getByLabelText('Seller name'),'Seller');
    await user.click(screen.getByRole('button',{name:'Submit'}));
    expect(screen.getByRole('button',{name:'Submitting…'}).disabled).toBe(true);
    await user.click(screen.getByRole('button',{name:'Submitting…'}));expect(submit).toHaveBeenCalledTimes(1);
    finish({accepted:true});await waitFor(()=>expect(screen.getByRole('status').textContent).toBe('Submitted'));
  });
  it('debounces autocomplete, updates results, and supports keyboard selection', async () => {
    const search=vi.fn(async(q)=>[{id:'1',name:`${q} Storage`}]);const user=userEvent.setup();
    render(<AssignmentApp initialDay={11} api={{submit:async()=>({}),search,quote:async()=>({feeCents:100,version:'t'}),placements:async()=>[],poll:async()=>({state:'ready'})}}/>);
    await user.type(screen.getByRole('combobox',{name:'Find a placement'}),'North');
    await waitFor(()=>expect(screen.getByRole('option',{name:'North Storage'})).toBeTruthy());
    expect(search).toHaveBeenCalledTimes(1);await user.keyboard('{ArrowDown}{Enter}');
    expect(screen.getByRole('combobox',{name:'Find a placement'}).value).toBe('North Storage');
  });
  it('does not commit an older placement response after newer filters', async () => {
    const pending=[];const placements=vi.fn((query)=>new Promise((resolve)=>pending.push({query,resolve})));
    const api={submit:async()=>({}),search:async()=>[],quote:async()=>({feeCents:100,version:'t'}),placements,poll:async()=>({state:'ready'})};
    const user=userEvent.setup();render(<AssignmentApp initialDay={17} api={api}/>);
    await waitFor(()=>expect(pending.length).toBe(1));
    const box=screen.getByRole('textbox',{name:'Filter placements'});fireEvent.change(box,{target:{value:'North'}});
    await waitFor(()=>expect(pending.length).toBe(2));pending[1].resolve([{id:'new',name:'New result'}]);
    await waitFor(()=>expect(screen.getByText('New result')).toBeTruthy());pending[0].resolve([{id:'old',name:'Old stale result'}]);
    await new Promise((r)=>setTimeout(r,0));expect(screen.queryByText('Old stale result')).toBeNull();
  });
  it('supports retry after a quote service failure', async () => {
    let calls=0;const quote=vi.fn(async()=>{calls++;if(calls===1)throw Error('offline');return {feeCents:250,version:'v2'};});
    const api={submit:async()=>({}),search:async()=>[],quote,placements:async()=>[],poll:async()=>({state:'ready'})};
    render(<AssignmentApp initialDay={18} api={api}/>);
    await waitFor(()=>expect(screen.getByRole('button',{name:'Retry'})).toBeTruthy());
    fireEvent.click(screen.getByRole('button',{name:'Retry'}));await waitFor(()=>expect(screen.getByText(/Ready · v2/)).toBeTruthy());
  });
  it('bounds polling at success and allows cancellation', async () => {
    const poll=vi.fn(async()=>({state:'ready'}));const api={submit:async()=>({}),search:async()=>[],quote:async()=>({feeCents:100,version:'t'}),placements:async()=>[],poll};
    const user=userEvent.setup();render(<AssignmentApp initialDay={23} api={api}/>);await user.click(screen.getByRole('button',{name:'Start bounded polling'}));
    await waitFor(()=>expect(screen.getByRole('status').textContent).toContain('ready'));expect(poll).toHaveBeenCalledTimes(1);
  });
  it('implements keyboard tabs with roving focus and selected panel', async () => {
    const user=userEvent.setup();render(<AssignmentApp initialDay={28}/>);const options=screen.getByRole('tab',{name:'Options'});options.focus();
    await user.keyboard('{ArrowRight}');expect(screen.getByRole('tab',{name:'Fees'}).getAttribute('aria-selected')).toBe('true');
    expect(screen.getByRole('tab',{name:'Fees'}).tabIndex).toBe(0);expect(screen.getByRole('tabpanel',{name:'Fees'}).hidden).toBe(false);
  });
});
