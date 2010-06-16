---
layout: default
title: Jav-a-Go-Go Concerns and Decisions
---

# Jav-a-Go-Go Concerns and Decisions

Here are the questions that I've asked myself in the course of development, and
the decisiosn I came to.

## Arguments for Nested, Dynamic Classes

This is the problem of the `ProjectModule` class in Mix. It was going to someday
be the case that the `ProjectModule` would want some form of commad line
argument. That day has come with Cups Remix, where I want to provide the
artifact string of the artifact being built in the project, which is also the
formula.

Although it is not pressing. The notion is that you could use the same formula
for minor version changes, so long as the formula knew which version to checkout
from the source.

When I first encountered this, thought about passing in objects to the mix
command, but that really departs from idea of Jav-a-Go-Go as a command line
parser and starts to make it act like a really poorly designed application
container. Really, I'll only need to give the project a couple of strings or
switches. Anything more complicated would have to be handled in a very command
line way, using a configuration file or similar.

The problem with strings is that the project is loaded dynamically, after the
validity of the command line has been checked. I'd imagine that the command line
would look something like this:

`java go.go mix --project:artifact=org.hibernate/hibernate-core/3.5.1-Final \
    --working-directory=/tmp/1819921.sandbox install`

Here there is a speical namespace for the project.

I had some false starts. The notion of the project defining commands, but that
does nothing, they aren't there when the command line is parsed. The notion of
having the ability to specify a class name in lieu of a command to have hidden
internal commands, but again that does nothing. I'm recording these here in case
they someday tigger a meaningful synapse, or else discourage me from thinking to
hard about them again.

I'd recalled that I'd thought about this before, and the nition I had then was
an unbounded namespace, where validity is checked by the command, not by the
framework. Simply a switch to say, if you see an argument here that is not
specified in the interface, wave it through. Now this makes even more sense if
you can declare that namespace, to say that the `project` namespace for `mix` is
open and will be validated by the `mix` command.

This means that the `mix` namespace is closed, however. When I first thunk this
thought, I thought it would be mix that would be flexible, but really it is
project.

Now that seems perfectly reasonable and easy enough to explain. Sometimes you
won't know if an argument is valid until your running the commands, so you can
have namespace for variable arguments. You can then use the same facilities that
bind argument to commands to bind arguments to other class participants.

Which means that we can now, quite simply annotate the members of the project as
we would a commnad, and for Cups Remix, there is a convention where the project
artifact is supplied as an argument, and it may or may not be an exceptional
condition for it to be rejected, but we're a little further down the road.
