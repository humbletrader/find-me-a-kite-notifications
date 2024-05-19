#!/bin/bash


rm -r ./mail

mkdir mail


export spring_profiles_active=dev
mvn clean spring-boot:run -Dspring-boot.run.arguments=after

chmod u+x ./mail/sendEmails.sh

./mail/sendEmails.sh

