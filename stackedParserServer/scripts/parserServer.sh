#!/bin/sh

<<<<<<< .mine
# $1 model $2 port $3 projectroot
=======
root=/Users/dipanjand/Documents/work/fall2008/ravine/MSTServer
cd $root
>>>>>>> .r17462

cd $3
pwd
echo "Compiling Server Program"
#javac -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" $3/mst/DependencyEnglish2OProjParser.java
echo "Running Server Program"
<<<<<<< .mine
java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" -Xms8g -Xmx8g mst.DependencyEnglish2OProjParser $1 $2
=======
java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst.DependencyEnglish2OProjParser $root/models/wsj-02-21.part.CONLL.model tmp/ 12345
>>>>>>> .r17462
