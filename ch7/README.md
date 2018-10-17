## Chapter 7 Foldable and Traverse

`Foldable` abstracts `foldLeft` and `foldRight`. `Traverse` is slightly higher level, but makes some folds easier.

### 7.1 Foldable

#### 7.1.1 Folds and Folding

Generally, to fold, you supply and *accumulator* value and a *binary* folding function which combines each
thing in a collection with the accumulating value.

Depending on the structure and operation, `foldLeft` and `foldRight` have different performance/recursion
characteristics. Generally think of `foldLeft` as applying the operator from start to finish, `foldRight`
going from finish to start. The two are equivalent if the operation is commutative (e.g., numeric addition).

#### 7.1.2 Exercise: Reflecting on Folds

`foldLeft` over an empty list with `::` yields the list in reverse, while `foldRight` copies the list in order.

#### 7.1.3 Exercise: Scaf-fold-ing Other Methods

You can implement `map`, `flatMap`, `filter`, and `sum` on `List` with folds (`foldRight`, in particular).

#### 7.1.4 Foldable in Cats

`Foldable`'s `foldLeft` looks like:

    trait Foldable[X[_]] {
      def foldLeft[A, B](as: X[A], b: B)(f: (B, A) => B)
    }

##### 7.1.4.1 Folding Right

The definition of `foldRight` uses `Eval`:

    trait Foldable[X[_]] {
      def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B]
    }

This makes `foldRight` *stack safe*, because we can defer operations with `Eval`. Note that `List`
and `Vector` have stack safe `foldRight` in the standard library, but `Stream` does not.

##### 7.1.4.2 Folding with Monoids

Foldable provides a few useful functions:

* `find`
* `exists`
* `forall`
* `toList`
* `isempty` and `nonEmpty`

There are also a few utilities which rely on `Monoid` instances:

* `combineAll` (alias of `fold`)
* `foldMap` first maps elements of the collection via a user-defined function, and then `fold`s the results
    (relying on a `Monoid` for the result type).

You can also compose `Foldable`s to traverse deeper structures:

    import cats.instances.vector._
    val ints = List(Vector(1, 2, 3), Vector(4, 5, 6))
    (Foldable[List] compose Foldable[Vector]).combineAll(ints)

##### 7.1.4.3 Syntax for Foldable

All the `Foldable` methods are available as syntax functions, the first argument becoming the receiver of
the method call (e.g., `List(1,2,3).foldMap(_.toString)`. Note that names which conflict with the built-ins,
like `List.foldLeft` will be compiled to the built-in version; the compiler will only use implicits if needed.

### 7.2 Traverse

`Traverse` uses `Applicatives` to avoid some of the work of accumulators and combinators in `Foldable`.

#### 7.2.1 Traversing with Futures

The built-in `Future.traverse` and `Future.sequence` methods provide `Future`-specific versions of the traverse
functionality, so are a good case study.

If you have a `List`, and want to iterate through it, applying a function that returns a `Future[X]`, you end
up with a `List[Future[X]]`. You can `foldLeft` and combine `Future`s manually to get a `Future[List[X]]`,
but that's exactly what `traverse` does for you: `Future.traverse(f)(l)` applies `f: A => Future[X]` to
`l: List[A]` and gives you a `Future[List[X]]`. The `Future.sequence` function is even simpler, it assumes that
you've already got a `List[Future[X]]` and want to flip it to a `Future[List[X]]`.

`Traverse` generalizes this to work with any `Applicative` (`Future`, `Option`, `Validated`...).

#### 7.2.2 Traversing with Applicatives

When manually writing the `fold` that would `traverse` above, the seed accumulator was the same as the
underlying `Applicative`'s (`Future`'s) `pure`. The combinator binary function is the `Applicative`'s
underlying `Semigroupal.combine`. This gives us a way to traverse over other applicatives besides `Future`.

##### 7.2.2.1 Exercise: Traversinng with Vectors

Traversing over a `List` of `Vector`s, we'll get a `Vector` of `List`, where all the values are all the
combinations of one value from each of the original `Vector`s (the `Applicative` for `Vector` corresponds to
the Cartesian product of the vectors).

##### 7.2.2.2 Exercise: Traversing with Options

Traversing over a `List` of `Option`s, we'll get an `Option[List]` which is `Some` if all the original
`Option`s were `Some`, and `None` otherwise.

##### 7.2.2.3 Exercise: Traversing with Validated

Unlike `Option`, `Validated` "fails slow", so traversing over a list of them will aggregate up all the failures,
if there are any, or the success if they are all successes.

#### 7.2.3 Traverse in Cats

The `Traverse` typeclass generalizes the example above to work not just on `List`, but any instance of `Traverse`.
It is given by:

    trait Traverse[F[_]] {
      def traverse[G[_]: Applicative, A, B](inputs: F[A])(func: A => G[B]): G[F[B]]
      def sequence[G[_]: Applicative, B](inputs: F[G[B]]): G[F[B]] = traverse(inputs)(identity)
    }

### 7.3 Summary

`Foldable` abstracts `foldLeft` and `foldRight` from the standard library.

`Traverse` allows us to flip `F[G[A]]` into `G[F[A]]` when `F` is `Traverse`able and `G` is `Applicative`. It
frequently allows us to dramatically reduce the number of lines of code required, and is one of the most
powerful patterns in this book.

