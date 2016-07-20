#!/bin/bash

ARTIFACT_PATH="out/artifacts/DTT/DTT.jar"

echo "Press enter when you built the jar..."
read

echo ""
echo ""

java -jar $ARTIFACT_PATH $@
