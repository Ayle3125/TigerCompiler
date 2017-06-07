#!/bin/bash
java -jar JFlex.jar Lexer.flex
mv *.java ../src/lexer
java -jar java-cup-11a.jar -parser Parser Parser.cup
mv *.java ../src/parser

