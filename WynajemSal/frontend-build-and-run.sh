#!/bin/bash

yes | docker-compose stop
yes | docker-compose kill
cd Frontend
mvn clean package -DskipTests
cd ..
yes | docker-compose stop
yes | docker-compose kill
docker-compose up