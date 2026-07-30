# Security

## Tencent COS credentials

The file-storage service requires these environment variables at runtime:

- `TENCENT_COS_SECRET_ID`
- `TENCENT_COS_SECRET_KEY`
- `TENCENT_COS_APP_ID`
- `TENCENT_COS_REGION`
- `TENCENT_COS_BUCKET_NAME`

If a key has been exposed, revoke it in Tencent Cloud before creating a
replacement. Supply replacement credentials through the runtime environment;
do not commit them.

`pm-file-storage/.env.example` contains variable names only and no secrets.
Real `.env*` files remain ignored by Git.

Cleaning local Git history does not remove sensitive data from remote clones or
GitHub. Those copies remain exposed until a coordinated history replacement is
completed.
