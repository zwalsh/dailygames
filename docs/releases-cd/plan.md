# CD Migration: GitHub Releases-Based Deployment

## Goal

Eliminate server-side compilation. Jenkins builds the tarball once, uploads it as a GitHub
release, and production pulls the pre-built artifact. Test deployments remain source-based but
become intentional (label-triggered) rather than automatic.

## Motivation

The current `deploy.sh` compiles the Kotlin codebase on the server for every deploy, which is
memory-intensive. This also means two separate compilations per main merge: Jenkins and
dailygames. Moving to pre-built releases removes the JDK/Gradle runtime requirement from the
server entirely for production, and reduces test builds to on-demand only.

## Architecture

```
Jenkins (any agent)                    Production server
──────────────────                     ─────────────────────────────────────────
assemble                               [timer fires every 1 min]
ktlintCheck                            deploy.sh --env dailygames
gradle build             ──────▶         gh release list --json → latest tag
gh release create ◀─────────────         curl CI status; skip if not green
setBuildStatus                           gh release download dailygames.tar
                                         tar -xf into ~/releases/$TAG/
                                         git checkout $SHA (for migrations)
                                         db/migrate.sh
                                         ln -sfn ~/releases/$TAG ~/releases/current
                                         sudo systemctl restart dailygames
                                         echo $TAG > ~/deployed_commit

Developer labels PR "deploy-test"      Test server (manual/intentional)
──────────────────────────────────     ────────────────────────────────────────
                                       test-deploy.sh
                                         Part 1: run migrations from latest release
                                         Part 2: gh pr list --label deploy-test
                                           find most-recently-updated PR
                                           check CI status = success for HEAD SHA
                                           git checkout $SHA
                                           ./gradlew assemble
                                           unpack, symlink, restart testdailygames
```

## Changes to Existing Files

### `Jenkinsfile`

Add a `release` stage between `test` and `setBuildStatus`, running only on `main`:

```groovy
stage('release') {
    when { expression { env.GIT_BRANCH == 'origin/main' } }
    steps {
        sh '''
            gh release create "sha-${GIT_COMMIT}" \
              build/distributions/dailygames.tar \
              --title "sha-${GIT_COMMIT}" \
              --notes "" \
              --latest \
              --repo zwalsh/dailygames
        '''
    }
}
```

The `gh` CLI must be available on agents. Jenkins uses the existing `GITHUB_TOKEN` credential
(which already has `Contents: write` scope).

`setBuildStatus` fires after `release`, so the asset exists before CI is marked green.

### `scripts/deploy.sh` (prod only, simplified)

Replace the checkout + build + unpack block with a download:

1. `gh release list` → get latest tag name and derive SHA
2. Read `~/deployed_commit`; exit 0 if tag already deployed
3. Query GitHub commit status API; exit 0 if not `success`
4. `gh release download` → `/tmp/dailygames.tar`
5. `mkdir -p ~/releases/$TAG && tar -xf /tmp/dailygames.tar -C ~/releases/$TAG`
6. `git checkout $SHA` (to get current migration files)
7. `db/migrate.sh`
8. `ln -sfn ~/releases/$TAG ~/releases/current`
9. `sudo systemctl restart dailygames`
10. `echo $TAG > ~/deployed_commit`
11. Prune old releases (keep 3 most recent)

The testdailygames path now just logs and exits; use `test-deploy.sh` instead.

### `scripts/testdailygames-deploy.service`

`ExecStart` updated from `deploy.sh --env testdailygames` to `test-deploy.sh`.

## Files Added

- `scripts/test-deploy.sh` — source-based deploy for testdailygames, label-gated
- `docs/releases-cd/plan.md` — this file

## Credential Changes

| Credential                                   | Where   | Change                                                                                                     |
|----------------------------------------------|---------|------------------------------------------------------------------------------------------------------------|
| `GITHUB_TOKEN` (Contents: write)             | Jenkins | Already present — used for `setBuildStatus`; `Contents: write` already in scope                           |
| `~/.github_token` on dailygames server       | Server  | No change — `Commit statuses: read` is sufficient; asset downloads use `gh` with the same token           |
| `~/.github_token` on testdailygames server   | Server  | Needs `Pull requests: read` added (for `gh pr list`) — regenerate the PAT                                  |

## Release Naming and Discovery

Releases are tagged `sha-$GIT_COMMIT` (the full 40-char SHA). `deploy.sh` uses
`gh release list --json tagName,isLatest` to find the latest release, then checks CI before
switching. This is safe because Jenkins only creates releases on `main` after a successful
build, and `setBuildStatus` fires after the upload.

## Rollout Order

1. Merge this PR to main. Jenkins creates the first release.
2. Confirm `gh release list --json tagName,isLatest` returns the expected tag on the prod server.
3. Run `deploy.sh --env dailygames` manually once to verify the end-to-end prod flow.
4. Regenerate the testdailygames server token to add `Pull requests: read`.
5. Run `test-deploy.sh` manually on the test server (with a labeled PR) to verify.
6. Update the systemd service files on both servers (`sudo bash scripts/install-timers.sh`).
7. Re-enable both timers.
