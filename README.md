# seq

Distributed Sequence Generator

---

Sequence logic based on SQL like [Oracle CREATE SEQUENCE](http://docs.oracle.com/cd/B28359_01/server.111/b28286/statements_6015.htm#SQLRF01314)

# Features
* Sequence
    * min,max,increment,cycle,cache semantic
    * [ ] order
        Distributed restrict linear increment sequence
* Persistence
    * Zookeeper
    * Redis
* [ ] Distributed Sequence Server
    by Hazelcast
* [ ] Server
    * [ ] gRPC
    * [ ] ØMQ

# Motivation

* Learn and use gRPC
* Learn and use ØMQ
* Implement a sequence generator :smile:
* Play Hazelcast