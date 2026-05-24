#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cp "$SCRIPT_DIR/dailygames-deploy.service" /etc/systemd/system/
cp "$SCRIPT_DIR/dailygames-deploy.timer" /etc/systemd/system/
cp "$SCRIPT_DIR/testdailygames-deploy.service" /etc/systemd/system/
cp "$SCRIPT_DIR/testdailygames-deploy.timer" /etc/systemd/system/

systemctl daemon-reload
systemctl enable --now dailygames-deploy.timer
systemctl enable --now testdailygames-deploy.timer

echo "Timers installed and started."
systemctl status dailygames-deploy.timer testdailygames-deploy.timer
