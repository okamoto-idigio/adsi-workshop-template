#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# --- Stop existing processes ---
echo "=== Stopping existing processes ==="
for port in 3000 3001 8080; do
  pid=$(cat /proc/net/tcp 2>/dev/null | awk -v p="$(printf '%04X' $port)" '$2 ~ ":"p {split($0,a," "); split(a[10],b," "); print b[1]}' 2>/dev/null | head -1)
  if [ -n "$pid" ] && [ "$pid" != "0" ]; then
    kill "$pid" 2>/dev/null || true
  fi
done
# Kill by name as fallback
pkill -f "next-server" 2>/dev/null || true
pkill -f "sagemaker-proxy" 2>/dev/null || true
pkill -f "bootRun" 2>/dev/null || true
sleep 2

# --- Environment ---
export SAGEMAKER=1
export NEXT_PUBLIC_SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="/codeeditor/default/absports/3000"

# --- Backend ---
echo "=== Starting Backend ==="
cd "$PROJECT_ROOT/backend"
rm -f data/attendance.lock.db
./gradlew bootRun > /tmp/backend-sagemaker.log 2>&1 &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

# Wait for backend
for i in $(seq 1 30); do
  if curl -sf http://localhost:8080/h2-console > /dev/null 2>&1; then
    echo "Backend ready."
    break
  fi
  sleep 1
done

# --- Frontend Build ---
echo "=== Building Frontend ==="
cd "$PROJECT_ROOT/frontend"
npx next build

# --- Next.js start ---
echo "=== Starting Next.js ==="
npx next start -H 127.0.0.1 -p 3001 > /tmp/next-sagemaker.log 2>&1 &
NEXT_PID=$!
echo "Next.js PID: $NEXT_PID"
sleep 3

# --- Proxy ---
echo "=== Starting SageMaker Proxy ==="
node scripts/sagemaker-proxy.mjs > /tmp/proxy-sagemaker.log 2>&1 &
PROXY_PID=$!
echo "Proxy PID: $PROXY_PID"
sleep 1

# --- Verify ---
echo ""
echo "=== Verification ==="
STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/absports/3000/ 2>/dev/null || echo "000")
echo "GET /absports/3000/ → HTTP $STATUS"

echo ""
echo "=== All services running ==="
echo "  Backend:  http://localhost:8080 (PID $BACKEND_PID)"
echo "  Next.js:  http://127.0.0.1:3001 (PID $NEXT_PID)"
echo "  Proxy:    http://localhost:3000 (PID $PROXY_PID)"
echo ""
echo "Open in browser: PORTS tab → port 3000 globe → replace 'ports' with 'absports'"
