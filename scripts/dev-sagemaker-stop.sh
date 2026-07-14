#!/usr/bin/env bash
echo "Stopping SageMaker dev services..."
pkill -f "sagemaker-proxy" 2>/dev/null || true
pkill -f "next-server" 2>/dev/null || true
pkill -f "next start" 2>/dev/null || true
pkill -f "bootRun" 2>/dev/null || true
sleep 1
echo "Done."
