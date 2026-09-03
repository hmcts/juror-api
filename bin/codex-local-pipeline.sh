#!/usr/bin/env bash

set -euo pipefail

mode="${1:-fast}"
if [[ $# -gt 0 ]]; then
  shift
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --base)
      [[ $# -ge 2 ]] || { echo "--base requires a value" >&2; exit 2; }
      shift 2
      ;;
    --no-fetch)
      shift
      ;;
    *)
      echo "Unsupported argument: $1" >&2
      exit 2
      ;;
  esac
done

case "${mode}" in
  checks-only|fast|full) ;;
  *)
    echo "Unsupported verification mode: ${mode}" >&2
    exit 2
    ;;
esac

git diff --check

# Keep this credential-free verification aligned with .github/workflows/ci.yml.
export JWT_SECRET_BUREAU='W3N1cGVyLXNlY3JldC1rZXktYnVyZWF1XVtzdXBlci1zZWNyZXQta2V5LWJ1cmVhdV1bc3VwZXItc2VjcmV0LWtleS1idXJlYXVd'
export JWT_SECRET_PUBLIC='W3N1cGVyLXNlY3JldC1rZXldW3N1cGVyLXNlY3JldC1rZXldW3N1cGVyLXNlY3JldC1rZXld'
export JWT_SECRET_HMAC='W3N1cGVyLXNlY3JldC1rZXktbG9naW5dW3N1cGVyLXNlY3JldC1rZXktbG9naW5dW3N1cGVyLXNlY3JldC1rZXktbG9naW5d'
export JWT_SECRET_ER_PORTAL='W3N1cGVyLXNlY3JldC1rZXktZXItcG9ydGFsXVtzdXBlci1zZWNyZXQta2V5LWVyLXBvcnRhbF1bc3VwZXItc2VjcmV0LWtleS1lci1wb3J0YWxd'
export SCHEDULER_SERVICE_SECRET='WW91clZlcnlWZXJ5VmVyeVNlY3JldEtleVRoYXRJc1NvU2VjcmV0SURvbnRFdmVuS25vd0l0'
export PNC_CHECK_SERVICE_SECRET='WW91clZlcnlWZXJ5VmVyeVNlY3JldEtleVRoYXRJc1NvU2VjcmV0SURvbnRFdmVuS25vd0l0'
export SCHEDULER_SERVICE_SUBJECT='external-api@juror-scheduler-api.hmcts.net'
export SCHEDULER_SERVICE_HOST='juror-scheduler-api.staging.platform.hmcts.net'
export SCHEDULER_SERVICE_PORT='443'
export PNC_CHECK_SERVICE_SUBJECT='juror-back-end'
export PNC_CHECK_SERVICE_HOST='juror-scheduler-execution.staging.platform.hmcts.net'
export PNC_CHECK_SERVICE_PORT='443'

diagnostics_dir="$(mktemp -d "${RUNNER_TEMP:-${TMPDIR:-/tmp}}/codex-gradle-diagnostics.XXXXXX")"
gradle_log="${diagnostics_dir}/gradle.log"
gradle_done="${diagnostics_dir}/complete"

(
  sleep 870
  if [[ ! -f "${gradle_done}" ]]; then
    echo "Gradle verification is approaching its 15-minute timeout." >&2
    if command -v jcmd >/dev/null 2>&1; then
      jcmd -l >"${diagnostics_dir}/jvm-processes.log" 2>&1 || true
      while read -r pid command _; do
        case "${command}" in
          *GradleDaemon* | *GradleWorkerMain*)
            jcmd "${pid}" Thread.print >"${diagnostics_dir}/thread-dump-${pid}.log" 2>&1 || true
            ;;
        esac
      done <"${diagnostics_dir}/jvm-processes.log"
    fi
  fi
) &
watchdog_pid=$!

gradle_command=(
  ./gradlew
  --no-daemon
  --console=plain
  --stacktrace
  --info
  integrationTest
)

set +e
if command -v timeout >/dev/null 2>&1; then
  timeout --signal=TERM --kill-after=2m 15m "${gradle_command[@]}" 2>&1 | tee "${gradle_log}"
  gradle_status=${PIPESTATUS[0]}
elif command -v gtimeout >/dev/null 2>&1; then
  gtimeout --signal=TERM --kill-after=2m 15m "${gradle_command[@]}" 2>&1 | tee "${gradle_log}"
  gradle_status=${PIPESTATUS[0]}
else
  echo "GNU timeout is unavailable; Gradle will run without the local 15-minute guard." >&2
  "${gradle_command[@]}" 2>&1 | tee "${gradle_log}"
  gradle_status=${PIPESTATUS[0]}
fi
set -e

touch "${gradle_done}"
kill "${watchdog_pid}" >/dev/null 2>&1 || true
wait "${watchdog_pid}" 2>/dev/null || true

if [[ "${gradle_status}" -eq 124 || "${gradle_status}" -eq 137 ]]; then
  echo "Gradle verification timed out. Last reported task:" >&2
  grep -E '^> Task ' "${gradle_log}" | tail -n 1 >&2 || true
  for diagnostic in "${diagnostics_dir}"/jvm-processes.log "${diagnostics_dir}"/thread-dump-*.log; do
    [[ -f "${diagnostic}" ]] || continue
    echo "--- ${diagnostic} ---" >&2
    cat "${diagnostic}" >&2
  done
fi

exit "${gradle_status}"
