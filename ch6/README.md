## Chapter 6 Semigroupal and Applicative

Functors cannot represent all program flows, like form validation, where we want to return
all validation errors, not just the first error (`Either` fails fast, returning only the first error).
Another example is concurrent execution of multiple long running, independent tasks - since functor
and monad sequence operations, they assume the operations are dependent and so must be sequenced.

* `Semigroupal` captures the notion of composing pairs of contexts
* `Applicative` combines `Semigroupal` with `Functor`, to apply functions to parameters within a context.

Note that Cats formulates applicatives in a manner somewhat different from other approaches
(like scalaz and haskell). We'll discuss alternate formulations at the end of the chapter.

### 6.1 Semigroupal

`cats.Semigroupal` is for combining contexts. Its definition is

```
trait Semigroupal[F[_]] {
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
}
```

#### 6.1.1 Joining Two Contexts

As an example, `Semigroupal[Option].product(Some(123), Some("abc"))` produces `Option[(Int, String)]`.
If either side is `None` the whole result is `None`.

#### 6.1.2 Joining Three or More Contexts

The `tuple3` through `tuple22` methods on `Semigroupal` generalize `tuple2`, which is another name for
`product`, just varying in how many arguments the function takes.

There are also `map2` through `map22` methods which apply a function of multiple arguments in the
context provided by the semigroupal instance.

### 6.2 Apply Syntax

The `tupled` syntax method is shorthand for `Semigroupal.product`. In the case of `Option`, it converts
a pair of options to an option of a pair. `tupled` is provided in an overloaded manner, to work on any
number of arguments in the tuple, from 2 to 22. There's also a `.mapN` method that applies a function of
the appropriate number of arguments to the tuple it is applied to.

#### 6.2.1 Fancy Functors and Apply Syntax

The apply syntax also has `contramapN` and `imapN` methods, which work if there is an available
contravariant or invariant instance for the type in the tuple.

### 6.3 Semigroupal Applied to Different Types

**Future** semantics with semigroupal provide parallel, vs sequential, execution.

**List** actually produces the cartesian product of two lists, vs the zip of two lists. We'll see more
about why this happens shortly.

**Either** produces fail-fast semantics, though we hinted we'd like to not do this in the opening of
the chapter. We'll see an alternate solution shortly.

#### 6.3.1 Semigroupal Applied to Monads

Since `Monad` extends `Semigroupal`, the instances for `List` and `Either` are somewhat surprising above.

However, we can provide instances for `Semigroupal` in some area where we cannot create a `Monad`,
which we'll see more in the next section.

##### Exercise: The Product of Monads

If you implement `product` using `flatMap` or a `for`-comprehension, you can see why `List` ends us
giving cross-product, and `Either` produces a fail-fast `Semigroupal`.

### 6.4 Validated

Cats provides a data type called `Validated` that has an instance of `Semigroupal`, but _not_ `Monad`.

#### 6.4.1 Creating Instances of Validated

`Validated` has two sub-types, `Valid` and `Invalid`, roughly corresponding to `Right` and `Left`,
respectively. You can create instances with `apply` methods (e.g., `Validated.Valid(123)`), or smart
constructors which widen the return type to `Validated` (e.g., `Validated.valid[List[String], Int](123)`).
There are also `valid` and `invalid` extension methods in `cats.syntax.validated`, and the methods
`cats.syntax.applicative.pure` and `cats.syntax.applicativeError.raiseError`. Finally, there are a few
helper methods that convert from `Exception`, `Option`, `Either`, and `Try`:

* `Validated.catchOnly`
* `Validated.catchNonFatal`
* `Validated.fromTry`
* `Validated.fromEither`
* `Validated.fromOption`

See the [docs](https://typelevel.org/cats/api/cats/data/Validated.html).

#### 6.4.2 Combining Instances of Validated

`Validated` accumulates errors with a `Semigroupal` instance for the error type, so an instance needs to
be in scope. You can then use `Semigroupal` methods to accumulate errors manually. It is commong to use
`List` or `Vector` for the error type. `cats.data` provides `NonEmptyList` and `NonEmptyVector` to prevent
failing without at least one error.

#### 6.4.3 Methods of Validated

`Validated` provides `map`, `leftMap`, and `bimap`, as well as a suite of methods similar to those on
`Either`. You can't `flatMap` because `Validated` isn't a monad, but you can convert back and forth between
`Either` and `Validated` as needed, eith `toEither` and `toValidated`, or even `withEither` and
`withValidated`.

#### 6.4.4 Exercise: [Form Validation](form-validation)

### 6.5 Apply and Applicative

Semigroupal isn't that common in normal functional programming literature, it provides a subset of
what is more common, called "applicatives". They provide alternative encodings of the same notion
of joining contexts.

There are two applicative type classes in cats.

1. `cats.Apply` extends `Semigroupal` and `Functor` with an `ap` method for applying a parameter to
    a function in a context. `product` can then be defined in terms of `ap`.
2. `cats.Applicative` extends `Apply` with `pure`, that we saw with monads.

In the above, `Apply.ap` introduces the newest structure, it has the following signature:

    ```
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] // Semigroupal[F] and Functor[F]
    ```

#### 6.5.1 The Hierarchy of Sequencing Type Classes

A full hierarchy of cats type classes is [online](https://github.com/tpolecat/cats-infographic).

The more constraints a type has, the more guarantees we have about its behavior, but the fewer
behaviors we can model.

### 6.6 Summary

Monads and functors are the mostly widely used sequencing data types covered in this book. Semigroupal
and applicative are mostly commonly used for combining independent values.

