import assert from 'node:assert/strict'
import { mkdtemp, readFile, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join, resolve } from 'node:path'
import { pathToFileURL } from 'node:url'
import { spawnSync } from 'node:child_process'

const web = resolve(import.meta.dirname, '..')
const fixture = JSON.parse(await readFile(join(web, '../fixtures/coverstory/CAP-166/synthetic-v1.json'), 'utf8'))
const temp = await mkdtemp(join(tmpdir(), 'coverstory-catalog-acceptance-'))
try {
  const compile = spawnSync(join(web, 'node_modules/.bin/tsc'), [
    '--ignoreConfig', join(web, 'src/coverstory/catalog/decodeCatalogEnvelope.ts'),
    '--target', 'ES2022', '--module', 'ES2022', '--moduleResolution', 'bundler',
    '--strict', '--skipLibCheck', '--outDir', temp,
  ], { encoding: 'utf8' })
  assert.equal(compile.status, 0, `${compile.stdout}${compile.stderr}`)
  await writeFile(join(temp, 'package.json'), '{"type":"module"}')
  const { decodeCatalogEnvelope } = await import(pathToFileURL(join(temp, 'decodeCatalogEnvelope.js')).href)

  const base = decodeCatalogEnvelope(fixture.payload)
  assert.equal(base.ok, true)
  if (base.ok) {
    assert.equal(base.value.versions.length, 3)
    assert.equal(base.value.versions[0].status, 'RETIRED')
    assert.equal(base.value.versions[1].sourceVersion, 'v2')
  }
  assert.deepEqual(decodeCatalogEnvelope(fixture.trace_tag_variant), base)
  assert.deepEqual(decodeCatalogEnvelope({ ...fixture.payload, schemaMajor: 2 }), {
    ok: false, error: { kind: 'UnsupportedVersion', field: 'schemaMajor', actual: 2 },
  })
  const missing = { ...fixture.payload, versions: [{ ...fixture.payload.versions[0], versionId: null }] }
  assert.equal(decodeCatalogEnvelope(missing).error.field, 'versions[0].versionId')
  const unexpected = { ...fixture.payload, versions: [{ ...fixture.payload.versions[0], ownerEmail: 'synthetic@example.invalid' }] }
  assert.equal(decodeCatalogEnvelope(unexpected).ok, false)
  console.log('CAP-166 learner acceptance: 5 checks passed.')
} finally {
  await rm(temp, { recursive: true, force: true })
}
