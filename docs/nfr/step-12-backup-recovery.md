# Step 12 — NFR-REL-02 + NFR-REL-03 + DR Plan: Backup & Recovery

**NFR IDs:** NFR-REL-02, NFR-REL-03, DR Plan
**Effort:** ~3 hrs | **Dependencies:** Step 8 (Docker for PostgreSQL)
**Commit:** `feat(ops): add DB backup + DR scripts (NFR-REL-02, NFR-REL-03)`

---

## SRS Requirements

> **NFR-REL-02:** Database backups: daily full, hourly incremental; 30-day online retention, 1-year archive.
> **NFR-REL-03:** Point-in-time recovery (PITR) capability.
> **DR Plan:** RTO: 4 hours, RPO: 1 hour.

## Current State

No backup scripts, no WAL archiving, no recovery procedures documented.

## Implementation

### New File: `scripts/backup/pg_backup.sh`

Daily full backup script:

```bash
#!/bin/bash
# TaskHive PostgreSQL Daily Full Backup
# Schedule via cron: 0 2 * * * /path/to/pg_backup.sh

set -euo pipefail

DB_NAME="${DB_NAME:-taskhive}"
DB_USER="${DB_USERNAME:-postgres}"
BACKUP_DIR="${BACKUP_DIR:-/backups}"
RETENTION_DAYS=30
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/full/taskhive_full_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}/full"

echo "[$(date)] Starting full backup of ${DB_NAME}..."
pg_dump -U "$DB_USER" -d "$DB_NAME" -Fc | gzip > "$BACKUP_FILE"
echo "[$(date)] Backup created: $BACKUP_FILE ($(du -h "$BACKUP_FILE" | cut -f1))"

# Clean up old backups (beyond retention)
find "${BACKUP_DIR}/full" -name "*.sql.gz" -mtime +${RETENTION_DAYS} -delete
echo "[$(date)] Cleaned backups older than ${RETENTION_DAYS} days"
```

### New File: `scripts/backup/pg_restore.sh`

Recovery script:

```bash
#!/bin/bash
# TaskHive PostgreSQL Restore
# Usage: ./pg_restore.sh <backup_file>

set -euo pipefail

BACKUP_FILE="$1"
DB_NAME="${DB_NAME:-taskhive}"
DB_USER="${DB_USERNAME:-postgres}"

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: $0 <backup_file.sql.gz>"
    exit 1
fi

echo "[$(date)] Restoring ${DB_NAME} from ${BACKUP_FILE}..."
echo "WARNING: This will overwrite the current database!"
read -p "Continue? (y/N) " confirm
[ "$confirm" = "y" ] || exit 0

dropdb -U "$DB_USER" "$DB_NAME" --if-exists
createdb -U "$DB_USER" "$DB_NAME"
gunzip -c "$BACKUP_FILE" | pg_restore -U "$DB_USER" -d "$DB_NAME"
echo "[$(date)] Restore complete"
```

### New File: `scripts/backup/wal_archive_config.md`

PostgreSQL WAL configuration for PITR:

```
# Add to postgresql.conf for PITR support:
wal_level = replica
archive_mode = on
archive_command = 'cp %p /backups/wal/%f'
max_wal_senders = 3
```

### New File: `docs/disaster-recovery.md`

Document containing:
- RTO/RPO targets
- Backup schedule table
- Step-by-step recovery procedure
- Contact list for incidents
- Quarterly DR drill checklist

### Docker Compose Addition

Add backup volume and cron service to `docker-compose.yml`:

```yaml
  backup:
    image: postgres:16-alpine
    volumes:
      - ./scripts/backup:/scripts
      - backup_data:/backups
      - postgres_data:/var/lib/postgresql/data:ro
    entrypoint: /bin/sh
    command: -c "crond -f"
    depends_on:
      - postgres

volumes:
  backup_data:
```

## Verification

```bash
# Test backup
./scripts/backup/pg_backup.sh
ls -la /backups/full/

# Test restore
./scripts/backup/pg_restore.sh /backups/full/taskhive_full_*.sql.gz

# Verify data integrity after restore
psql -U postgres -d taskhive -c "SELECT count(*) FROM users;"
```
