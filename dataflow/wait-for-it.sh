#!/usr/bin/env bash
# wait-for-it.sh: Wait for a service to be ready

set -e

host="$1"
shift
port="$1"
shift

echo "Waiting for $host:$port to be ready..."
while ! nc -z "$host" "$port"; do
  sleep 1
done

echo "$host:$port is available, executing command..."
exec "$@"
