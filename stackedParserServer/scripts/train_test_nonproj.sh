#!/bin/sh

root=/usr0/dipanjan/work/fall2008/MSTServer/MSTParserStackedServer
train=${root}/data/wsj-02-21.part.CONLL
test=${root}/data/wsj-22.mrg.CONLL
model=${root}/models/wsj-02-21.part.CONLL.model
out=${root}/wsj-22_pred.part.CONLL
ord=2

cd ${root}
# Train
javac -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst/DependencyParser.java
java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" -Xms1g -Xmx1g mst.DependencyParser \
train separate-lab separate-lab-cutoff:0 iters:10 \
decode-type:proj order:$ord \
train-file:$train \
model-name:$model \
format:CONLL

# Test
#$jhome/javac -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst/DependencyParser.java
#$jhome/java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" -Xms8g -Xmx8g mst.DependencyParser \
#test separate-lab \
#model-name:$model \
#decode-type:proj order:$ord \
#test-file:$test \
#output-file:$out \
#format:CONLL

# Eval

#$jhome/java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" -Xms8g -Xmx8g mst.DependencyParser \
#eval gold-file:$test \
#output-file:$out \
#format:CONLL


