#!/bin/bash

set -e

if [ -z "$DB_NAME" ]; then
  echo "Error: DB_NAME environment variable is not set."
  exit 1
fi

BACKUP_DIR=~/backup
mkdir -p "$BACKUP_DIR"
BACKUP_FILE="$BACKUP_DIR/$DB_NAME-$(date +%F).sql"

echo "Backing up $DB_NAME to $BACKUP_FILE"

pg_dump -d "$DB_NAME" > "$BACKUP_FILE"
tar -czf "$BACKUP_FILE.tar.gz" "$BACKUP_FILE"
rm "$BACKUP_FILE"

echo "Backup of $DB_NAME completed successfully"

echo "Deleting old backups..."

find "$BACKUP_DIR" -name "$DB_NAME-*.tar.gz" -mtime +30 -exec rm {} \;

echo "Old backups deleted successfully."
