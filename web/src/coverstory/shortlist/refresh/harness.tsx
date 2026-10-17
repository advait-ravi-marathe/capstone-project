import { createElement } from 'react'
import { createRoot, type Root } from 'react-dom/client'
import { CheckpointRefreshPanel } from './CheckpointRefreshPanel'
import type { CheckpointRefreshPreview, Resolution } from './types'

const preview: CheckpointRefreshPreview = {
  schemaMajor: 1,
  shortlistId: 'SYNTHETIC_SHORTLIST_A',
  checkpointRevision: 1,
  catalogGeneration: 20,
  fingerprint: 'SYNTHETIC_FINGERPRINT_20',
  slots: [
    { slotId: 's1', base: version('H1','v1'), local: version('H1','v1'), upstream: version('H1','v2'), state: 'RESOLVED' },
    { slotId: 's2', base: version('H2','v1'), local: version('H2','v2'), upstream: version('H2','v3'), state: 'CONFLICT', allowedChoices: [version('H2','v2'), version('H2','v3')] },
    { slotId: 's3', base: version('H3','v1'), local: version('H3','v1'), upstream: null, state: 'UNAVAILABLE' },
  ],
}
function version(productId: string, versionId: string) { return { productId, versionId, sourceSha256: `${productId.toLowerCase().charCodeAt(0).toString(16)}`.repeat(64).slice(0,64) } }
let root: Root | undefined
let accepted: { fingerprint: string; decisions: Resolution[] } | undefined
function render(stale=false, currentPreview=preview) {
  if (!root) root=createRoot(document.getElementById('root')!)
  root.render(createElement(CheckpointRefreshPanel,{preview:currentPreview,stale,onRetry:()=>{(window as any).__retryCount++},
    onAccept:async(fingerprint,decisions)=>{accepted={fingerprint,decisions}},error:undefined}))
}
(window as any).__retryCount=0
;(window as any).__setCsm14Stale=()=>render(true)
;(window as any).__setCsm14Fingerprint=(fingerprint: string)=>render(false,{...preview,fingerprint})
;(window as any).__acceptedCsm14=()=>accepted
render()
