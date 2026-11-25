#!/usr/bin/env sh
# Runs all experiments and writes CSV results to ./results.
# Usage: ./run-experiments.sh [seeds]
set -eu
ROOT="$(cd "$(dirname "$0")" && pwd)"
SEEDS="${1:-10}"
mvn -q -f "$ROOT/fabric/pom.xml" -DskipTests compile exec:java \
    -Dexec.mainClass=greasenbrine.fabric.experiment.ExperimentRunner \
    -Dexec.args="$ROOT/results $SEEDS"
