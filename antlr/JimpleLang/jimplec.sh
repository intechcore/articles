#!/usr/bin/env bash


SCRIPT_DIR="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

java -Xmx500M -cp $SCRIPT_DIR/target/jimple-jar-with-dependencies.jar org.jimple.compiler.JimpleCompilerCli $@
