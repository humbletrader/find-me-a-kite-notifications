#!/bin/bash


rm -r ./mail

mkdir mail

export spring_profiles_active=dev
mvn clean spring-boot:run

chmod u+x ./mail/sendEmails.sh

./mail/sendEmails.sh

