#!/usr/bin/env bash

echo "Welcome to the So Long Release tool"
echo "Handing over to mvn release:clean release:prepare release:perform"
mvn release:clean release:prepare release:perform