import assert from 'node:assert/strict'
import { mkdtemp, readFile, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join, resolve } from 'node:path'
import { pathToFileURL } from 'node:url'
import { spawnSync } from 'node:child_process'

const web = resolve(import.meta.dirname, '..')
const fixture = JSON.parse(await readFile(join(web, '../fixtures/coverstory/CAP-166/synthetic-v1.json'), 'utf8'))
const temp = await mkdtemp(join(tmpdir(), 'coverstory-catalog-decoder-'))
try {
  const tsc = join(web, 'node_modules/.bin/tsc')
  const source = join(web, 'src/coverstory/catalog/decodeCatalogEnvelope.ts')
  const compile = spawnSync(tsc, ['--ignoreConfig', source, '--target', 'ES2022', '--module', 'ES2022', '--moduleResolution', 'bundler', '--strict', '--skipLibCheck', '--outDir', temp], { encoding: 'utf8' })
  assert.equal(compile.status, 0, `${compile.stdout}${compile.stderr}`)
  await writeFile(join(temp, 'package.json'), '{"type":"module"}')
  const { decodeCatalogEnvelope } = await import(pathToFileURL(join(temp, 'decodeCatalogEnvelope.js')).href)

  assert.equal(fixture.classification, 'SYNTHETIC_PERSISTED_CATALOG_ENVELOPE')
  assert.equal(fixture.database_persisted, false)
  assert.equal(fixture.consumer_data, false)
  assert.equal(fixture.payment_or_policy_effects, false)
  assert.equal(fixture.payload.schemaMajor, 1)
  assert.equal(fixture.payload.versions.length, 3)
  assert.deepEqual(fixture.trace_tag_variant.versions, fixture.payload.versions)
  assert.equal(fixture.trace_tag_variant.traceTag, 'trace-166-fixture-only')
  assert.equal(fixture.payload.versions.filter((row) => row.status === 'PUBLISHED').length, 2)
  assert.equal(fixture.payload.versions[0].status, 'RETIRED')
  assert.throws(() => decodeCatalogEnvelope(fixture.payload), /TODO\(CAP-166\)/)
  console.log('CAP-166 instructor fixture checks: 11 passed; decoder remains learner TODO; database/API/UI integration is not exercised.')
} finally {
  await rm(temp, { recursive: true, force: true })
}
