#!/bin/bash
# Runs the SQL init script using the SA_PASSWORD environment variable.
# The password is passed via -P flag — no shell interpolation of special chars.
set -e

echo "Waiting for SQL Server to accept connections..."
for i in $(seq 1 30); do
  /opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$SA_PASSWORD" -C -Q "SELECT 1" -b > /dev/null 2>&1 && break
  echo "Attempt $i: SQL Server not ready yet..."
  sleep 3
done

echo "Running database init script..."
/opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$SA_PASSWORD" -C -I -i /init/01_init.sql
echo "Database init complete."
