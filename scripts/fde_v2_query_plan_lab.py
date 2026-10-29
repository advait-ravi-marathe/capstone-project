#!/usr/bin/env python3
"""Assistant-supplied SQLite query-plan exercise; synthetic in-memory rows only."""
from __future__ import annotations
import argparse
import json
import sqlite3


def run_lab(fault: str = "none") -> dict:
    if fault not in {"none", "missing-tenant-filter"}:
        raise ValueError("unknown supplied fault")
    connection=sqlite3.connect(":memory:")
    connection.execute("CREATE TABLE documents(document_id TEXT PRIMARY KEY, tenant_id TEXT NOT NULL, active INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
    rows=[(f"doc-{i:04d}",f"tenant-{i % 20:02d}",int(i % 5 != 0),i) for i in range(1000)]
    connection.executemany("INSERT INTO documents VALUES(?,?,?,?)",rows)
    query=("SELECT document_id, updated_at FROM documents WHERE active=1 ORDER BY updated_at DESC LIMIT 10"
           if fault == "missing-tenant-filter" else
           "SELECT document_id, updated_at FROM documents WHERE tenant_id=? AND active=1 ORDER BY updated_at DESC LIMIT 10")
    parameters=() if fault == "missing-tenant-filter" else ("tenant-03",)
    plan_before=[row[3] for row in connection.execute("EXPLAIN QUERY PLAN "+query,parameters)]
    result_before=connection.execute(query,parameters).fetchall()
    connection.execute("CREATE INDEX idx_documents_tenant_active_updated ON documents(tenant_id,active,updated_at DESC)")
    plan_after=[row[3] for row in connection.execute("EXPLAIN QUERY PLAN "+query,parameters)]
    result_after=connection.execute(query,parameters).fetchall()
    returned_tenants=[connection.execute("SELECT tenant_id FROM documents WHERE document_id=?",(row[0],)).fetchone()[0] for row in result_after]
    expected=connection.execute("SELECT COUNT(*) FROM documents WHERE tenant_id=? AND active=1",("tenant-03",)).fetchone()[0]
    return {"fixture":"synthetic_in_memory_only","rows_loaded":len(rows),"fault":fault,
            "query":query,"parameter_count":len(parameters),"plan_without_index":plan_before,
            "plan_with_index":plan_after,"uses_named_index":any("idx_documents_tenant_active_updated" in line for line in plan_after),
            "returned_rows":len(result_after),"first_document":result_after[0][0] if result_after else None,
            "tenant_filter_enforced":fault == "none" and all(tenant=="tenant-03" for tenant in returned_tenants),
            "returned_tenants":returned_tenants,
            "cross_tenant_rows":sum(tenant!="tenant-03" for tenant in returned_tenants) if fault=="missing-tenant-filter" else 0,
            "expected_tenant_rows":expected if fault == "none" else None,
            "same_results_before_after_index":result_before==result_after,
            "elapsed_performance_claim":False}


def main() -> None:
    parser=argparse.ArgumentParser()
    parser.add_argument("--fault",choices=("none","missing-tenant-filter"),default="none")
    args=parser.parse_args()
    print(json.dumps(run_lab(args.fault),indent=2))


if __name__=="__main__": main()
