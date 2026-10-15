export type LowSensitivityEvent = Readonly<{ eventId: string; eventType: 'PAGE_VIEW' | 'CONTROL_CLICK'; schemaVersion: 1; pageId: string; targetId?: string }>

/** Lossy best-effort analytics queue. It has no access to journey or payment commands. */
export class BoundedTelemetryQueue {
  readonly #events: LowSensitivityEvent[] = []
  constructor(readonly maxEvents = 20, readonly maxAttempts = 3) {
    if (!Number.isInteger(maxEvents) || maxEvents < 1 || !Number.isInteger(maxAttempts) || maxAttempts < 1) throw new RangeError('invalid telemetry queue limits')
  }

  enqueue(event: LowSensitivityEvent): boolean {
    if (this.#events.length >= this.maxEvents) return false
    this.#events.push(Object.freeze({ ...event }))
    return true
  }

  get size(): number { return this.#events.length }

  /** Caller schedules this in the background and never awaits it from checkout/resume. */
  async flush(send: (batch: readonly LowSensitivityEvent[]) => Promise<number>): Promise<{ attempts: number; sent: number; dropped: number }> {
    let attempts = 0
    let dropped = 0
    let sent = 0
    while (this.#events.length) {
      const batch = this.#events.slice(0, 10)
      let status = 0
      for (let tryNo = 1; tryNo <= this.maxAttempts; tryNo++) {
        attempts++
        try { status = await send(batch) } catch { status = 0 }
        if (status >= 200 && status < 300) break
      }
      if (status >= 200 && status < 300) { this.#events.splice(0, batch.length); sent += batch.length }
      else { this.#events.splice(0, batch.length); dropped += batch.length }
    }
    return { attempts, sent, dropped }
  }
}
