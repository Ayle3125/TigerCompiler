#!bin/sh
for file in ./*
do
    if [[ $file = *java ]];
    then
        echo "Test file :" $file
        java -cp ../bin Tiger $file
    fi
    
done
