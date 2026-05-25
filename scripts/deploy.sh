#!/bin/bash
set -euo pipefail

log() {
    echo "[$(date -u '+%Y-%m-%dT%H:%M:%SZ')] $*"
}

ENV=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --env) ENV="$2"; shift 2 ;;
        *) echo "Unknown argument: $1" >&2; exit 1 ;;
    esac
done

if [[ "$ENV" != "dailygames" && "$ENV" != "testdailygames" ]]; then
    echo "Usage: $0 --env <dailygames|testdailygames>" >&2
    exit 1
fi

REPO=~/dailygames
TOKEN=$(cat ~/.github_token)

on_error() {
    local exit_code=$?
    local line=$1
    log "[$ENV] *** DEPLOY FAILED *** at line $line (exit code $exit_code)"
    log "[$ENV] Check the lines above for the error output from the failing command"
}
trap 'on_error $LINENO' ERR

if [[ "$ENV" == "dailygames" ]]; then
    # 1. Get latest release tag from GitHub
    log "[dailygames] Fetching latest release"
    TAG=$(GITHUB_TOKEN="$TOKEN" gh release list \
        --repo zwalsh/dailygames \
        --json tagName,isLatest \
        --jq '.[] | select(.isLatest) | .tagName')
    if [[ -z "$TAG" ]]; then
        log "[dailygames] No release found"
        exit 0
    fi
    SHA="${TAG#sha-}"
    log "[dailygames] Latest release: $TAG (SHA: $SHA)"

    # 2. Exit if already deployed
    DEPLOYED_FILE=~/deployed_commit
    if [[ -f "$DEPLOYED_FILE" ]] && [[ "$(cat "$DEPLOYED_FILE")" == "$TAG" ]]; then
        log "[dailygames] $TAG already deployed, nothing to do"
        exit 0
    fi

    # 3. Exit if CI status is not success
    STATUS=$(curl -s "https://api.github.com/repos/zwalsh/dailygames/commits/$SHA/status" \
        -H "Authorization: token $TOKEN" | jq -r '.state // empty') || true
    log "[dailygames] CI status for $SHA: ${STATUS:-<empty>}"
    if [[ "$STATUS" != "success" ]]; then
        log "[dailygames] CI not green (state=$STATUS), skipping deploy"
        exit 0
    fi

    log "[dailygames] Starting deploy of $TAG"

    # 4. Download release asset
    log "[dailygames] Downloading release asset"
    GITHUB_TOKEN="$TOKEN" gh release download "$TAG" \
        --repo zwalsh/dailygames \
        --pattern 'dailygames.tar' \
        --dir /tmp/ \
        --clobber

    # 5. Unpack into release directory
    RELEASE_DIR=~/releases/$TAG
    log "[dailygames] Unpacking to $RELEASE_DIR"
    mkdir -p "$RELEASE_DIR"
    tar -xf /tmp/dailygames.tar -C "$RELEASE_DIR"

    # 6. Update repo to release SHA so migration files are current
    log "[dailygames] Checking out $SHA for migrations"
    git -C "$REPO" fetch origin
    git -C "$REPO" checkout -f "$SHA"

    # 7. Run database migrations
    log "[dailygames] Running database migrations"
    "$REPO/db/migrate.sh"

    # 8. Atomically update the current symlink
    log "[dailygames] Updating current symlink to $RELEASE_DIR"
    ln -sfn "$RELEASE_DIR" ~/releases/current

    # 9. Restart the service
    log "[dailygames] Restarting dailygames service"
    sudo systemctl restart dailygames

    # 10. Record the deployed tag
    echo "$TAG" > ~/deployed_commit
    log "[dailygames] Deploy of $TAG complete"

    # 11. Prune old releases, keeping the 3 most recent
    log "[dailygames] Pruning old releases"
    ls -t ~/releases/ | grep -v '^current$' | tail -n +4 | xargs -I{} rm -rf ~/releases/{}

else
    log "[testdailygames] testdailygames deploys are not handled here; use test-deploy.sh"
fi
