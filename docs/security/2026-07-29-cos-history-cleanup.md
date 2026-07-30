# Local Tencent COS history cleanup

- Cleanup time: `2026-07-29T23:32:27-0700` (`2026-07-30T06:32:27Z`)
- Scope: this local repository only
- Removed path: `pm-file-storage/src/main/resources/application.yaml`
- Pre-rewrite `main`: `239c459851fb5b5cdd73aebfdcffda1bd13a4708`
- Rewritten `main`: `f421f2e46f41872f974437286ae18710a8475cb0`
- Pre-rewrite feature HEAD: `51b4ea2a8ec2bb5ba4d4c4db43b8ad1ea4e23548`
- Rewritten feature base (`REWRITTEN_BASE`): `f405fb18409f702a7711cfcdadf79e037c6d6fae`

## Verification

The cleanup used `git filter-branch --index-filter` for commit refs and rewrote 17 local Codex tree refs that were not committish. Backup refs and unreachable objects were then removed with:

```bash
git for-each-ref --format='delete %(refname)' refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now
```

The following checks passed:

- `git log --all --format=%H -- pm-file-storage/src/main/resources/application.yaml` returned no commits.
- A scan of every reachable tree returned zero entries for the removed path.
- The one recorded pre-cleanup sensitive blob was absent after pruning.
- The pre-document scan covered 586 reachable text blobs; the final post-document scan covered 587. Both returned zero Tencent key-ID matches and zero non-placeholder secret-key matches using the Task 4 per-line matching semantics.
- `git fsck --full --no-dangling` completed successfully.
- The configured `origin` URL remained unchanged, and no push or force-push was performed.

## Required external follow-up

GitHub history and every other clone remain unchanged by this local-only cleanup. A coordinated remote history rewrite would still be required to remove the file from those copies.

The owner of the Tencent Cloud account must revoke the previously exposed key and create a replacement. Local Git history cleanup does not revoke a cloud credential.
