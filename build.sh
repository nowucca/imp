#!/bin/bash
cd com.nowucca.imp.util
mvn clean install || exit 1
cd ../com.nowucca.imp.core
mvn clean install || exit 1
cd ../com.nowucca.imp.server
mvn clean install || exit 1