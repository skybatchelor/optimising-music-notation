java -jar optimisingmusicnotation.jar $1 $2 2> ERRORS

if [ -s ERRORS ]
then
    echo FAIL > FAIL
else
    echo PASS > PASS
fi
