## Chapter 9 Case Study: Map-Reduce

### 9.1 Parallelizing `map` and `fold`

We can easily parallelize `map` because there are no dependencies between transformations
for different elements of the container. If we parallelize `foldLeft`, the results will only
be consistent if the function we are reducing with is *associative*. This lets us chop up a
long chain into lots of small chains, process each chain in turn, and then reduce those results.
Additionally, we'll need to seed every small chain, as `foldLeft` requires, and so will want
an *identity* element for the reduction, one that can be combined with another value `v` and
will return `v` back.

All that to say, we will `map` into a `Monoid`, and then reduce will be fine.

### 9.2 Implementing `foldMap`

```
def foldMap[A, B: Monoid](as: Vector[A])(f: A => B): B =
  // B.combineAll(as.map(f)) // cheating, I guess
  as.foldLeft(Monoid[B].empty)(_ |+| f(_))
```

### 9.3 Parallelising `foldMap`

Scala provides some simple tools for distributing work among threads, like the parallel collections
library, but we'll dive in a little ourselves and use `Future`s.

#### 9.3.1 `Future`s, Thread Pools, and `ExecutionContext`s

`Future`s run in a thread pool, determined by the implicit `ExecutionContext` parameter. There is
a `scala.concurrent.ExecutionContext.Implicits.global` that you can import that gives you one thread
per CPU. If you use `map` or `flatMap` to chain operations on `Future`s, they will be scheduled
to run after their dependent tasks. You can block and wait for a result with `Await.result`.

#### 9.3.2 Dividing Work

You can get the number of CPUs on a machine with the Java `Runtime.getRuntime.availableProcessors`
call. We can split a list into a list of lists using `grouped`, which takes a parameter for the size
of each group.

#### 9.3.3 Implementing `parallelFoldMap`

```
def parallelFoldMap[A, B: Monoid](values: Vector[A])(f: A => B): Future[B] =
  Future.sequence(values.grouped(10).map(vs => Future { foldMap(vs, f) })).map(bs => foldMap(bs)(f))
```

#### 9.3.4 `parallelFoldMap` with more Cats

```
def parallelFoldMap[A, B: Monoid](values: Vector[A])(f: A => B): Future[B] =
  values.groupd(10).traverse(vs => Future(vs.foldMap(f))).map(_.combineAll) // roughly, copied from solutions
```

Basically, here, recall that `map` then `sequence` is `traverse`.

### 9.4 Summary

Lots of data science tasks can be represented with monoids:

* Bloom filters (set membership estimation)
* HyperLogLog (set cardinality estimation)
* stochastic gradient descent
* t-digest (quantile estimation)

