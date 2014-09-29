#!/bin/bash
cd com.nowucca.imp.util
mvn clean  || exit 1
cd ../com.nowucca.imp.core
mvn clean  || exit 1
cd ../com.nowucca.imp.proxy
mvn clean  || exit 1