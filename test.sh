#!/bin/sh
set -e

java -jar mjc.jar $1 -dSw --target=$2
java -jar jasmin.jar *.j -g
java Main > test_mjc.out
javac -d ./ $1
java Main > test_javac.out
diff test_mjc.out test_javac.out
