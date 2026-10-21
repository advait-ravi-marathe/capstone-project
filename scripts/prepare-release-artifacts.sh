#!/usr/bin/env bash
set -euo pipefail

# Build an immutable A/B pair from pinned commits plus explicitly hashed instructor overlays.
# Usage: scripts/prepare-release-artifacts.sh RUN_DIR CANDIDATE_SHA A_OVERLAY.json B_OVERLAY.json
if [[ $# -ne 4 ]]; then echo "usage: $0 RUN_DIR CANDIDATE_SHA A_OVERLAY.json B_OVERLAY.json" >&2; exit 2; fi
run_dir=$(mkdir -p "$1" && cd "$1" && pwd)
candidate_sha=$2; overlay_a=$(realpath "$3"); overlay_b=$(realpath "$4")
baseline_sha=105bbc9dd448460f9bd5f7e6b9c67eafa99a16c3
repo=$(git rev-parse --show-toplevel)
[[ $(git -C "$repo" rev-parse "$candidate_sha^{commit}") == "$candidate_sha" ]]
git -C "$repo" diff --exit-code "$baseline_sha" "$candidate_sha" -- src/main/resources/db/migration >/dev/null || { echo "V001-V005 migration sources differ between artifacts" >&2; exit 1; }
temp=$(mktemp -d "${TMPDIR:-/tmp}/coverstory-pair.XXXXXX")
cleanup() { git -C "$repo" worktree remove --force "$temp/a" >/dev/null 2>&1 || true; git -C "$repo" worktree remove --force "$temp/b" >/dev/null 2>&1 || true; rm -rf "$temp"; }
trap cleanup EXIT
git -C "$repo" worktree add --detach "$temp/a" "$baseline_sha" >/dev/null
git -C "$repo" worktree add --detach "$temp/b" "$candidate_sha" >/dev/null
apply_overlay() {
  python3 - "$1" "$2" <<'PY'
import hashlib,json,pathlib,shutil,sys
root=pathlib.Path(sys.argv[1]).resolve(); items=json.loads(pathlib.Path(sys.argv[2]).read_text())
if not isinstance(items,list): raise SystemExit("overlay manifest must be a JSON array")
for item in items:
    source=pathlib.Path(item["source"]).resolve(); dest=(root/item["destination"]).resolve()
    if not source.is_file() or root not in dest.parents: raise SystemExit("invalid overlay path")
    if hashlib.sha256(source.read_bytes()).hexdigest()!=item["sha256"]: raise SystemExit(f"overlay checksum mismatch: {source}")
    dest.parent.mkdir(parents=True,exist_ok=True); shutil.copyfile(source,dest)
PY
}
apply_overlay "$temp/a" "$overlay_a"; apply_overlay "$temp/b" "$overlay_b"
build_one() {
  local tree=$1 name=$2
  (cd "$tree" && ./mvnw -o -Plocal-app -DskipTests clean package)
  local jar; jar=$(find "$tree/target" -maxdepth 1 -type f -name 'capstone-starter-*.jar' ! -name '*-plain.jar' -print -quit)
  [[ -n "$jar" ]]; cp "$jar" "$run_dir/$name.jar"
}
build_one "$temp/a" baseline; build_one "$temp/b" candidate
jar tf "$run_dir/baseline.jar" > "$temp/a.contents"; jar tf "$run_dir/candidate.jar" > "$temp/b.contents"
grep -q '^BOOT-INF/classes/assignments/delivery/recovery/ReleaseInspectionOnlyApplication.class$' "$temp/b.contents"
if grep -q '^BOOT-INF/classes/assignments/delivery/recovery/ReleaseInspectionOnlyApplication.class$' "$temp/a.contents"; then echo "baseline unexpectedly contains the B-only report capability" >&2; exit 1; fi
python3 - "$repo" "$run_dir" "$baseline_sha" "$candidate_sha" "$overlay_a" "$overlay_b" <<'PY'
import hashlib,json,pathlib,subprocess,sys
repo,run,base,candidate,oa,ob=sys.argv[1:]; root=pathlib.Path(repo); out=pathlib.Path(run)
def sha(p): return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def overlay(path): return [{**r,"sha256":sha(r["source"])} for r in json.loads(pathlib.Path(path).read_text())]
migrations=[{"name":p.name,"sha256":sha(p)} for p in sorted((root/"src/main/resources/db/migration").glob("V00[1-5]*.sql"))]
manifest={"schema_version":1,"baseline":{"path":"baseline.jar","sha256":sha(out/"baseline.jar"),"source_commit":base,"overlay":overlay(oa)},"candidate":{"path":"candidate.jar","sha256":sha(out/"candidate.jar"),"source_commit":candidate,"overlay":overlay(ob)},"capability_check":{"baseline_absent":"ReleaseInspectionOnlyApplication","candidate_present":"ReleaseInspectionOnlyApplication"},"build_recipe":"isolated detached git worktrees; checksum-verified overlays; ./mvnw -o -Plocal-app -DskipTests clean package","java_version":subprocess.check_output(["java","-version"],stderr=subprocess.STDOUT,text=True).splitlines()[0],"maven_version":subprocess.check_output([str(root/"mvnw"),"-o","-version"],cwd=root,text=True).splitlines()[0],"migration_sources":migrations}
out.joinpath("artifact-pair-manifest.json").write_text(json.dumps(manifest,indent=2)+"\n")
PY
echo "Prepared pair and $run_dir/artifact-pair-manifest.json"
