#!/bin/sh

projectroot=/usr2/dipanjan/experiments/MSTServer
model=/usr2/dipanjan/experiments/MSTParserStacked/models/wsj-02-21.mrg.CONLL.model
input=/usr2/dipanjan/experiments/MSTServer/data/wsj-02-21.part.CONLL
output=/usr2/dipanjan/experiments/MSTServer/data/out.CONLL

#echo "Running Server"
#./parserServer.sh ${model} 12345 ${projectroot}
echo "Running Client"
./parserClient.sh ${input} ${output} 12345 ${projectroot}
