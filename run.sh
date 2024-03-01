#!/bin/bash
# Has not been tested on Mac

java -jar optimisingmusicnotation.jar $1 $2 > OUTPUT 2> ERRORS

if [ -s ERRORS ]
then
    echo FAIL > FAIL
else
    echo PASS > PASS
fi

while IFS= read -r line; do
    open "$line"
done
