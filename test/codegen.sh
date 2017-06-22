#!bin/sh
for file in ./*
do
    if [[ $file = *java ]];
    then
        echo "Test file :" $file
        java -cp ../bin Tiger -codegen $file
    fi
    
done

for file in ./*
do
    if [[ $file = *j ]];
    then
        echo "generate bytecode:" $file
        java -jar ../tool/jasmin.jar $file
    fi
    
done
