## Chapter 10 Case Study: Data Validation

We'd like to be able to support things like
* Integer values must be in some range
* String values must parse as Ints and correspond to valid record IDs
* A collection cannot be empty
* Strings must match password 'strongness' filters, or formats (e.g., email addresses)

With the following goals:
* Useful error messages for invalid data
* Combine small validators into larger ones
* We may transform data while validating it (e.g., String -> Int above)
* Accumulate all failures at once

### 10.1 Sketching the Library Structure

**Providing error messages**

We consider validation results to be in a context where there may be a value or an error message.

**Combine checks**

An applicative combinations of checks would feed the same value to multiple checks, and produce a context
around a pair of checks, where we want something more monoidal, combining the results (e.g., with `and`
and `or`). Instead of using `Monoid`, which would require two instances of the typeclass, we'll use
different `and` and `or` methods.

**Accumulating errors as we check**

If we represent errors as `List` or `NonEmptyList` (or, really, any monoid), we can accumulate error messages.

**Transforming data as we check it**

Since we're transforming while wrapping in a context, and might `map` or `flatMap`, we expect to use
a monad.

### 10.2 The Check Datatype

We begin with `type Check[E, A] = A => Either[E, A]`, but choose to use a `trait` so we can add methods
to the type.

Typeclasses are a way to unify disparate data types with a common interface, which isn't what we're trying
to do, so we represent `Check` as a (sealed) algebraic data type.

### 10.3 Basic Combinators

We represent _and_ and _or_ logic for `Check`s, representing those computations as types of checks, along with
a `Pure` that is sort of the "1-check" validator. So, `Check` becomes a sealed trait with children `Pure`,
`And`, and `Or`.

### 10.4 Transforming Data

If want to allow `map`ping a `Check` to transform a value, we are inspired to change `Check` to be
`A => Either[E, B]`. However, that raises issue with what to actually return from `And` and `Or`.

#### 10.4.1 Predicates

We separate _check_, which can transform data, from _predicate_ which combines values with logical operators.
So now `Predicate` replaces what we were calling `Check`.

#### 10.4.2 Checks

With `Predicate` pulled out, we let `Check[E, A, B]` capture more transforming implementations:
`Map[E, A, B, C]`, `FlatMap[E, A, B, C]`, and `AndThen[E, A, B, C]`.

#### 10.4.3 Recap


