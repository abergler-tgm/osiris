#!/bin/sh
wget http://aayvazyan.bitnamiapp.com/jenkins/job/Osiris_Linker/lastSuccessfulBuild/artifact/linker/build/libs/*zip*/libs.zip
unzip -o libs.zip
rm libs.zip
rm "$(find . -maxdepth 1 -name 'Osiris-Linker*.jar' -print -quit)"
mv "$(find ./libs -maxdepth 1 -name 'Osiris-Linker*.jar' -print -quit)" .
rm -r ./libs
java -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y -jar "$(find . -maxdepth 1 -name 'Osiris-Linker*.jar' -print -quit)"