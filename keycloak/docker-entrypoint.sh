#!/bin/bash
set -e

echo "Keycloak entrypoint script started..."

# Wait for database to be ready
while ! mysqladmin ping -h"mysql" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" --silent; do
  echo "Waiting for MySQL to be ready..."
  sleep 2
done

echo "MySQL is ready!"

# Execute original entrypoint
exec /opt/keycloak/bin/kc.sh "$@"
