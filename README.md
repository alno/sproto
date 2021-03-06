SProto library
==============

SProto is library for Scala which allows to write/read data structures using different protocols.
It's like [sbinary](https://github.com/harrah/sbinary) or [sjson](https://github.com/debasishg/sjson) but with no hard-coded data format.

Status
------

[![Build Status](https://secure.travis-ci.org/alno/sproto.png?branch=master)](http://travis-ci.org/alno/sproto)

Installation
------------

For now you should compile it yourself (using SBT 0.1x.x), publish to local repository and then link from your project.

SBT linkage:

    libraryDependencies ++= Seq(
      "org.sproto" %% "sproto-core" % "0.1-SNAPSHOT",
      "org.sproto" %% "sproto-mongo" % "0.1-SNAPSHOT"
    )

Motivation
----------

It allows you to describe new protocols and new data structure handling rules independently.

Protocols
---------

Implemented protocols:

* MongoDB objects (based on [mongo-java-driver](https://github.com/mongodb/mongo-java-driver))

Planned:

* JSON objects
* Basic map/list objects
* JSON string with no intermediate objects
* MessagePack binaries

Concepts
--------

### Writing

**Writer** - something to there data can be written

**MapWriter** - something to there key-value pairs can be written

**SeqWriter** - something to there sequence of elements can be written

**WriteProtocol** - a set of rules for writing data to specific writers

### Reading

**Reader** - something from where data can be read

**MapReader** - something from there key-value pairs can be read

**SeqReader** - something from there sequence of elements can be read

**ReadProtocol** - a set of rules for reading data from specific readers

Contributors
------------

* Alexey Noskov ([alno](https://github.com/alno))

Feel free to add yourself when you fix bugs or add new features.

Copyright © 2012 Alexey Noskov, released under the MIT license

