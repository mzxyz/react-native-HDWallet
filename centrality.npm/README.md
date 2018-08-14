# Centrality NPM #

This repository holds NPM helper scripts. This scripts will help us publish
packages to private NPM repository hosted at gemfury.io.

# Usage #

Include this NPM repository as submodule to your project using following
command `git submodule add git@bitbucket.org:centralitydev/centrality.npm.git`.

Build server should run `./centrality.npm/docker-publish.sh` and provide
environment variable:

* GEMFURY_URL.

Version is picked from package.json.

If your project requires some extra steps, provide `package.sh` script in
project root folder.

Example project: [centralitycommon](https://jenkins.centrality.ai/jenkins/blue/organizations/jenkins/npm-common/activity)
Example project settings: [centralitycommon-settings](https://jenkins.centrality.ai/jenkins/job/npm-common/configure)

## Important ##

Package versioning uses [Semver](http://semver.org/). Make sure you follow it!
Also, each project should have CHANGELOG.md file with clear description what 
changed and how to upgrade.
