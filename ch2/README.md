## Chapter 2 Monoids and Semigroups

These first two typeclasses allow us to add or combine values.

Integers under addition, integers under multiplication, and strings under concatenation
are all sets with associative binary operations - and, moreover, there's an identity element,
which doesn't have an impact (0, 1, "", respectively).

### 2.1 Definition of a Monoid

A *monoid* for a type `A` is

* an operation `combine` with type `(A, A) => A`, and
* an element `empty` of type `A`

The `combine` method should be associative, `combine(a, combine(b, c)) == combine(combine(a, b), c)`,
and the `empty` should be an identity, `combine(a, empty) == combine(empty, a) = a`.

These laws aren't checked by the compiler, but you should not create unlawful instances because
you will get unpredictable results.

### 2.2 Definition of a semigroup

A *semigroup* is a monoid without an `empty`.

`cats` has a `NonEmptyList` type that is a semigroup but not a monoid.

Since every monoid is a semigroup, we can implement `Monoid[A]` by extending `Semigroup[A]`,
where `empty` comes in the `Monoid` typeclass, extending the `combine` from `Semigroup`.

### 2.3 Exercise: The Truth About Monoids

How many monoids are there for boolean?

### 2.4 Exercise: All Set for Monoids

What monoids and semigroups are there for sets?

### 2.5 Monoid in Cats

### 2.5.1 The Monoid Type Class

`cats.Monoid` is an alias for the type class in `cats.kernel.Monoid`, which extends
`cats.kernel.Semigroup`, aliased as `cats.Semigroup`.

### 2.5.2 Monoid Instances

With the right implicits in scope (e.g., `cats.instances.string._` for `String`), you can
use the `apply` method of the `Monoid` companion object to get instances for a given type.

There are also instances for many other types, including `Int` and `Option`

**Question** there are at least 2 monoid instances for `Int`, why is the default one based on sum?

### 2.5.3 Monoid Syntax

`cats.syntax.semigroup._` provides the infix operator `|+|` for `combine`.

### 2.5.4 Exercise: Adding All The Things

## 2.6 Applications of Monoids

### 2.6.1 Big Data

Chunking up a bunch of work and combining the pieces is exactly what a `Monoid` gives you, and
associativity lets us ignore how the batches are grouped.

See the Map Reduce case study.

### 2.6.2 Distributed Systems

"Commutative Replicated Data Types" (CRDT) are those where you can combine instances to get an
aggregate unified view of the data.

There's a CRDT case study in the book.

### 2.6.3 Monoids in the Small

## 2.7 Summary
