imp
===

Roadmap
==================
Milestone 1: Stand up a TCP proxy with configuration files.
* server should accept and connect and be thread-efficient.
* no preconnects - simply proxy on demand to start with
* capturing IMAP handshake and message retrieval using Wireshark
* establish testing regime that uses 2 robot scripts and the proxy

Milestone 2: Implement an IMAP codec using Netty 4.x with unit tests.
* model all commands/messages using apache-james as a guide
* implement encoder and decoder with unit tests for each command/message
* install codecs in accept and connect pipelines
* replay the testing regime from milestone 1 with new codecs installed.