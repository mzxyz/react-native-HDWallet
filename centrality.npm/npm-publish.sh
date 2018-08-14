#!/bin/bash
# Script that will publish npm package to private npm repository
# This is second script that expects credentials setup script ran before

set -e

# Extracting package name from package.json
declare -r PACKAGE_NAME=$(grep '"name": "*"' package.json | sed 's/  "name": "//g' | sed 's/",//g')

declare -r VERSION=$(grep '"version": "*"' package.json | sed 's/  "version": "//g' | sed 's/",//g')

# remove private flag from package.json
sed -i 's/"private": true,/"private": false,/g' package.json

# we don't want centrality.npm scripts in published package
echo "centrality.npm" >> .gitignore

npm --no-color pack

# publish package
curl -F package=@"${PACKAGE_NAME}-${VERSION}.tgz" "${GEMFURY_URL}"
