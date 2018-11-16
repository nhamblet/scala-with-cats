## Chapter 11 Case Study: CRDTs

CRDT stands for *Commutative Replicated Data Types*, which can be used to reconcile "eventually consistent" data.

### 11.1 Eventual Consistency

A "consistent" system is one where all machines have the same view of the data. Like a password change isn't
accepted until all machines have agreed to it. This tends to create high latency, and may cause uptime issues.

An "eventually consistent" system allows machines to have different views, but tries to catch up everybody over
time - more specifically, if all machines are online and no more changes occur, they will reach consistency.

### 11.2 The GCounter

The data structure we will consider is a *GCounter*, an increment-only counter (presumably "G" = "global")

#### 11.2.1 Simple Counters

Suppose machines A and B store an integer counter for the number of website visitors. A load balancer distributes
requests between the two machines, who can then easily maintain a counter of the requests they have seen, but
an inconsistent view on the total number of requests between the machines. If we try to have the machines
just add eachothers counters periodically, they'll end up double-counting, and the GCounter addresses this.

#### 11.2.2 GCounters

The main idea is for each machine to store a separate counter for each machine it knows about. Each machine
is only allowed to increment its own counter. When machines reconcile counts, they take the maximum reported
count for each machine.

### 11.2.3 Exercise: GCounter Implementation

### 11.3 Generalisation

How can we make the counter work with other data types, besides integers?

Our implementation uses addition (to increment one counter, or combine to get the total), the identity
element (to initialize), and the maximum operator (to merge counters).

The initialization, inrement, and total operations are all basically our standard addition monoid for
integers. With maximum, in the merge operation, we assume commutativity (so that order doesn't matter),
and also idempotency, which means we can repeat the operation without getting a new result (here, that
`a max a = a`).

An idempotent commutative monoid is also called a *bounded semilattice*.

So we have two monoids (`+` and `max`), we want both to be commutative, and we want the second to also
be idempotent.

As an example, we see that we could use `Set` instead of `Int`, with the union operator for all of the
operations.

#### 11.3.1 Implementation

`cats` doesn't directly provide commutative or idempotent monoid ([spire](https://github.com/non/spire)
has useful things though). We can choose to leave commutativity checks up to the user, but still will
define a `BoundedSemiLattice[A]` typeclass which extends `Monoid[A]`.

#### 11.3.2 Exercise: `BoundedSemiLattice` Instances

#### 11.3.3 Exercise: Generic `GCounter`

### 11.4 Abstracting GCounter to a Type Class

We now have a `GCounter` that works with any `BoundedSemiLattice`, but it still relies on a map from
strings to the type, where we might like to allow other key-value stores (e.g., a database).

We might make `GCounter[F[_,_], K, V]`, where `F` abstracts `Map`, and `K` and `V` are the types of
the keys (previously strings) and values (which we require to be `BoundedSemiLattice`). Since we'll
have to implement everything for each `F` we want, we won't get a lot of code re-use.

### 11.5 Abstracting a Key Value Store

We can capture a key-value store as a type class, and then make a `GCounter` for any key-value store.
Then we can add syntax methods and make `GCounter`s that much more generic. The implementations end
up being a bit boiler-plate, but can be improved with
[simulacrum](https://github.com/mpilquist/simulacrum) and [kind-projector](https://github.com/non/kind-projector).

### 11.6 Summary

