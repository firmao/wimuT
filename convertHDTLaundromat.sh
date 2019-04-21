#!/bin/bash

find "dirFilesLaundromatGz/" -type f \( -name '*.gz' -o -name '*.gz' \) > files.txt

file="files.txt"
COUNTER=0
f1=""
f2=""
while IFS= read line
do
	let COUNTER=COUNTER+1
    # display $line or do somthing with $line
	#echo "$line"
	
	if [ $COUNTER == 1 ] 
	then
		f1=$line
	fi
	
	if [ $COUNTER = 2 ] 
	then
		f2=$line
		echo "$f1"
		echo "$f2"
		IFS='/' read -ra NAMES <<< "$line"    #Convert string to array
		./rdf2hdt.sh -base "$f1" "$f2" "hdtOut/${NAMES[2]}".hdt
		let COUNTER=0
	fi
done <"$file"
