## Chapter 8 Case Study: Testing Asynchronous Code

In this case study, we show how to unit test asynchronous code with synchronous tests.
We are trying to measure and aggregate the uptime of servers.

### 8.1 Abstracting over Type Constructors

If you want to test code that returns `F[X]`, but to simplify your tests want to stub out
a thing that just returns `X`, you can create a parent trait that takes a type parameter (the `F`)
and abstracts over that return type, because `cats` provides the `Id` type which is an identity
wrapper around a type.

### 8.2 Abstracting over Monads

Since the different clients now return `F[X]` of two different `F` types, the service must
also, and then it can't `traverse` because it needs evidence of `Applicative`ness, so you have
to add that as a type constraint in the service (the client doesn't care about it still).
However, after totally re-writing your program, your test now works as written :) Said another way,
it's nice to have the power and generality of things like `Traverse`ables and `Applicative`s in
library code (the thing we're testing) because it lets us write nicer client code (the test).

### 8.3 Summary

