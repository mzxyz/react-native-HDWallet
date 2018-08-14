#!/bin/bash
# Script prepares docker environment for publishing NPM packages

set -e

# This function cleans up all files after publish run.
# There was a problem where files created inside docker container were owned by
# root and Jenkins was unable to clean them up on project check out.
function cleanUp {
    sudo rm -rf *
}

trap cleanUp ERR

docker run --rm -t \
     -v $(pwd):/workdir \
     --entrypoint=bash \
     -e GEMFURY_URL="${GEMFURY_URL}" \
     -e GEMFURY_MIRROR_URL="${GEMFURY_MIRROR_URL}" \
     node:6.11.4 \
     /workdir/centrality.npm/execute.sh

cleanUp
