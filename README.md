imp
===

Roadmap
=======
Milestone 1: Stand up a TCP proxy with configuration files.
* server should accept and connect and be thread-efficient.
* no preconnects - simply proxy on demand to start with
* capturing IMAP handshake and message retrieval using Wireshark
* establish testing regime that uses 2 robot scripts and the proxy

Milestone 2: Implement an IMAP codec using Netty 4.x with unit tests.
Codec for Client requests
* model all commands using apache-james as a guide
* implement encoder and decoder with unit tests for each command
* install codecs in accept and connect pipelines

Codec for Server responses
* model all responses using apache-james as a guide
* implement encoder and decoder with unit tests for each response
* install codecs in accept and connect pipelines

* replay the testing regime from milestone 1 with new codecs installed.

Usage
=====

* Download from github.
* Depends on Java 1.7 and maven3 to install.

* mvn clean install
* cd com.nowucca.imp.proxy
* mvn -Pproxy package

This will run a TCP proxy on port 8080 proxying to cnn.com port 80.
You can test it by pointing a browser to http://localhost:8080 and see the cnn front page come up.
