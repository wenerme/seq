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
    * In memory for test
    * In grid
* [ ] Distributed Sequence Server
    by Hazelcast
* [ ] Server
    * [ ] gRPC
    * [ ] ØMQ

# Limitation
* Sequence number is a Java long, the range is `[1,2^63]`

# Motivation

* Learn and use gRPC
* Learn and use ØMQ
* Implement a sequence generator :smile:
* Play Hazelcast

<!--
Super fast
Ordered and Unordered
Used to handled exactly once delievery and Gurrenty ordered message.
-->
