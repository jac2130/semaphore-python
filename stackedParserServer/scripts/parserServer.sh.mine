#!/bin/sh

# $1 model $2 port $3 projectroot

cd $3
pwd
echo "Compiling Server Program"
#javac -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" $3/mst/DependencyEnglish2OProjParser.java
echo "Running Server Program"
java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" -Xms8g -Xmx8g mst.DependencyEnglish2OProjParser $1 $2
