---
layout: default
title: Configuration
---

Eventually, we're going to want to have some sort of configuration for a
Jav-a-Go-Go program. It will become tiring to bang out arguments for
preferences, paths, or API keys each time you invoke your favorite Jav-a-Go-Go
program.

One that really wears me down it the URI for the Maven repository for Cups
Maven.

Add the set argument and the command is not run, but the parameter is recorded.

`java go.go --set cups maven --uri=http://repo1.maven.org/maven2/`

Somewhere down the road you could use a parallel command to for configurations.

`java go.go --set cups config --libraries:~/.m2/repository/:/usr/lib/java/`

That would be available through the command line as...

`java go.go cups maven --config:libraries:~/.m2/repository/:/usr/lib/java/`

But, I don't know if that is better.

At some point you might want to remove a default.

`java go.go --clear cups maven --uri`

Configuration is stored in the first library, which is often the user library.

That is how we'll do configuration variables for now.
