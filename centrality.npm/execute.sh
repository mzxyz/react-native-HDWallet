#!/bin/bash
# Script that logs users in artifactory, runs custom package steps and
# publishes package.

set -e

cd /workdir

# Run additional project packaging script (for quality control / pre-publish etc)
[[ -f ./package.sh ]] && ./package.sh || echo "No custom packaging steps found at $(pwd)/package.sh"

# publish
./centrality.npm/npm-publish.sh
