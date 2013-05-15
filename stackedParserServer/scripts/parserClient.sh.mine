#!/bin/sh

# $1 input file $2 output file $3 port $4 projectroot

root=/usr0/dipanjan/work/fall2008/MSTServer/MSTParserStackedServer
cd $4

echo "Compiling Client Program"
javac -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst/DependencyClient.java
echo "Running Client Program"
java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst.DependencyClient $1 $2 $3
