Drop `.sql` files here to have them run once, automatically, the first time the
`postgres` container starts against an empty data volume (same mechanism as
`../../mysql/init`). Nothing is required here for the container to work —
PostgreSQL is a standalone general-purpose database in this stack, with no
other service depending on it.
