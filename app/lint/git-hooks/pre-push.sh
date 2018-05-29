#!/bin/sh

echo "🔎🖅🔍"

./gradlew check --daemon

status=$?

if [ "$status" = 0 ] ; then
    echo "✔️"
    exit 0
else
    echo 1>&2 "❌: Found test errors. Please fix those before committing."
    exit 1
fi