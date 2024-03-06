#!/bin/bash
# Has not been tested on Mac

open -n /Applications/stenbergconverter.app/Contents/MacOS/stenbergconverter --args $1 $2 > OUTPUT 2> ERRORS

if [ -s ERRORS ]
then
    echo FAIL > FAIL
else
    echo PASS > PASS
fi

while IFS= read -r line; do
    open "$line"
done < OUTPUT
