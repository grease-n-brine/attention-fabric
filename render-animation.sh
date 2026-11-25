#!/usr/bin/env sh
# Renders the demonstration animation to media/attention-fabric.gif.
# Usage: ./render-animation.sh [seed]
# To write full-resolution PNG frames instead, pass FRAMES=1 in the environment.
set -eu
ROOT="$(cd "$(dirname "$0")" && pwd)"
SEED="${1:-0}"
if [ "${FRAMES:-0}" = "1" ]; then
    MAIN=greasenbrine.fabric.visualization.FrameSequenceRecorder
    OUT="$ROOT/media/frames"
else
    MAIN=greasenbrine.fabric.visualization.AnimationRecorder
    OUT="$ROOT/media/attention-fabric.gif"
fi
mvn -q -f "$ROOT/fabric/pom.xml" -DskipTests compile exec:java \
    -Djava.awt.headless=true \
    -Dexec.mainClass="$MAIN" \
    -Dexec.args="$OUT $SEED"
