## Chapter 4 Monads

Informally, a monad is anything with a constructor and a `flatMap`. All the functors from
the previous chapter, `List`, `Option`, and `Future`, are also monads.

Monads are so useful, we have special syntax in scala: for-comprehensions.

### 4.1 What is a Monad?

A monad is a mechanism for sequencing computations. (Though the
[Haskell wiki](https://wiki.haskell.org/What_a_Monad_is_not) doesn't totally agree).
Note we actually said a similar thing about functors in the last chapter.

Functors aren't as powerful as monads, because functors allow an "extra complication"
once at the beginning (the thing `map` is applied to is wrapped), but never again. Monads
let you introduce more complication as you go, because you basically get to `flatMap`
over a function that produces a wrapped value.

Every monad is a functor, so you have both `flatMap` and `map`. Both are useful in
for comprehensions, in particular the final `yield` is the `map` step.

If we think of Lists as sets of intermediate results, `flatMap` becomes a construct that
calculates permutations and combinations.

#### 4.1.1 Definition of a Monad

Monads are captured by two operations:

* `pure` of type `A => F[A]`
* `flatMap` of type `(F[A], A => F[B]) => F[B]`

Note that other libraries and languages, notably Scalaz and Haskell, use `bind` or `>>=` for `flatMap`.

`pure` gives you a way to convert a plain value to a monadic value, while `flatMap` provides
the sequencing step.

Here's how it looks (mostly) in cats:

    ```
    trait Monad[F[_]] {
      def pure[A](value: A): F[A]
      def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]
    }
    ```

The Monad laws are:

1. *Left identity* `pure(a).flatMap(func) == func(a)`
2. *Right identity* `m.flatMap(pure) == m`
3. *Associativity* `m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))`.

#### 4.1.2 Exercise: Getting Func-y

You can define `map` in terms of `pure` and `flatMap`:

```
def map[A, B](value: F[A])(func: A => B): F[B] =
  flatMap(value)(func andThen pure)
```

### 4.2 Monads in Cats

#### 4.2.1 The Monad Type Class

`cats.Monad` provides the Monad type class. It extends `FlatMap` (providing `FlatMap`) and
`Applicative` (providing `pure`), which, itself, extends `Functor`. We'll see more about
`Applicative` in chapter 6.

There are actually more methods available for `Monad`, see the [scaladocs](https://typelevel.org/cats/api/cats/Monad.html).

#### 4.2.2 Default Instances

Cats provides instances of `monad` for all the standard library collections, in `cats.instances`.

#### 4.2.3 Monad syntax

`cats.syntax.flatMap`, `cats.syntax.functor`, and `cats.syntax.applicative` provide syntax for
`flatMap`, `map`, and `pure`, respectively.

### 4.3 The Identity Monad

It is powerful to write methods which abstract over different monads, with signatures like
`def f[F[_]: Monad](a: F[Int]): F[Int]`. However, then you _have to_ pass a monad in, which is frustrating
if you have a plain value (in this case an `Int`). That's exactly what the `Id` monad gives you. You
can then use a `Future` in production use of `f`, and `Id` in tests, for example.

#### 4.3.1 Exercise: Monadic Secret Identities

```
type Id[A] = A

def pure[A](value: A): Id[A] =
  value

def map[A, B](initial: Id[A])(func: A => B): Id[B] = func(initial)

def flatMap[A, B](initial: Id[A])(func: A => Id[B]): Id[B] = func(initial)
```

### 4.4 Either

In scala 2.12, `Either` became "right-biased" and is now a functor and monad. Prior to that, it didn't
have a `map` or `flatMap` method.

#### 4.4.1 Left and Right Bias

Prior to scala 2.12, you had to explicitly ask for `.right` in `for`-comprehensions with `Either`s,
but now you don't have to. Cats back-ports this functionality to scala 2.11 via `cats.syntax.either`.

#### 4.4.2 Creating Instances

In addition to using `Left` and `Right` directly, `cats.syntax.either` provides `asLeft` and `asRight`
extensions methods. These are "smart constructors" because they return values of type `Either`, vs
the specific `Left` and `Right` sub-types.

Cats also provides `Either.catchOnly`, which takes a type parameter for an error type to catch from
the expression (the method parameter), to convert into the `Left` of an `Either` if the error occurs.
It also provides `Either.catchNonFatal`, which is similar but more general.

Finally, there are methods `Either.fromTry` and `Either.fromOption` to convert from those types.

#### 4.4.3 Transforming Eithers

Cats also provides `orElse` and `getOrElse` to extract values from the right side of an `Either`,
or return a default. It also provides `ensure[X](msg: String)(X => Boolean): Either[String, X]`
and `recover` and `recoverWith`.

There are also `leftMap` and `bimap`, if you don't care for the right-bias, and `swap` to exchange
left for right.

#### 4.4.4 Error Handling

`Either` is typically used to implement fail-fast error handling. As soon as one computation in a
`flatMap` sequence fails, everything else is left un-done. It is typical for the left type to be
`Throwable` (though this is too general, and basically equivalent to `Try`), or an algebraic data
type (sealed trait where each extension is probably a case class or object).

#### 4.4.5 Exercise: What is Best?

### 4.5 Aside: Error Handling and MonadError

`MonadError` is a type class that lets you abstract over `Either`-like data types typically used
for error handling, like `Future`, `Try`, and `Either` (or `EitherT`, that we'll see in the next
chapter).

#### 4.5.1 The MonadError Type Class

Here's basically the type class:

```
trait MonadError[F[_], E] extends Monad[F] {
  def raiseError[A](e: E): F[A]
  def handleError[A](fa: F[A])(f: E => A): F[A]
  def ensure[A](fa: F[A])(e: E)(f: A => Boolean): F[A]
```

In the following sections, we assume

```
import cats.MonadError
import cats.instances.either._

type ErrorOr[A] = Either[String, A]
val monadError = MonadError[ErrorOr, String]
```

#### 4.5.2 Raising and Handling Errors

`monadError.raiseError("Bad")` returns `Left("Bad")`, of type `ErrorOr[Nothing]`. Note that the compiler
has determined the `Nothing` type argument, though you could `raiseError[Int]`, or something, if you had
the need. `raiseError` is, in some sense, like the "error" version of `pure`.

`handleError` lets you (possibly) recover from an error. If the `fa` is the error type, if applies the given
handling function to try to recover, otherwise, if the `fa` is the non-error type, it just passes through.
It is probably typical that the `A` ends up being another `F[_]`.

Finally, `ensure` lets you filter an `fa` and convert a success to an error if it doesn't pass the filter.

#### 4.5.3 Instances of MonadError

`Either`, `Future`, and `Try` all have instances of `MonadError` in cats. `Either` lets you specify the type
of error, while the others use `Throwable`.

### 4.6 The Eval Monad

`Eval` lets you abstract over different *models of evaluation*, the typical examples being "eager" and "lazy",
though also "memoized".

#### 4.6.1 Eager, Lazy, Memoized, Oh My!

*Eager* computations happen immediately, where *lazy* computations are only done when needed.
*Memoized* computations are done on first access, but then results are cached.

Scala `val`s are eager and memoized. If you let `val x = {println("hi"); math.random}`, it is immediately
evaluated (eager), so you see the "hi" show up, but then on subsequent calls you get the same numeric value
you got the first time (memoized), despite the apparent `.random`. `def` is lazy (one evaluated when needed)
and not memoized, while `lazy val` is lazy and memoized.

#### 4.6.2 Eval's Models of Evaluation

`Eval` has `Now`, `Later`, and `Always` as sub-types, with contructor methods on the `Eval` object of the
same name (but lowercase). You can extract the value with a `.value` call. The three types correspond to
`val`, `lazy val`, and `def`.

#### 4.6.3 Eval as a Monad

From an `Eval` instance you can chain `map` or `flatMap` calls, like you would for any functor or monad,
and nothing is actually executed until you call `.value`.

You can also add a `.memoize` in the middle of the chain, and results to that point in the chain will
be cached.

#### 4.6.4 Trampolining and Eval.defer

Recursive functions that recurse too deeply can overflow the stack. `Eval.defer` can help with this
(pushing the resources used into the heap).

#### 4.6.5 Exercise: Safer Folding using Eval

```
def foldRight[A, B](as: List[A], acc: B)(fn: (A, B) => B): B =
  as match {
    case head :: tail => fn(head, foldRight(tail, acc)(fn))
    case Nil => acc
  }

```

The above fails for `foldRight(Range(0, 50000).toList, BigInt(0))(_+_)`. We can make that work with

```
def foldRight[A, B](as: List[A], acc: Eval[B])(fn: (A, Eval[B]) => Eval[B]): Eval[B] =
  as match {
    case head :: tail => Eval.defer(fn(head, foldRight(tail, acc)(fn)))
    case Nil => acc
  }
```

if we change the expression to `foldRight(Range(0, 50000).toList, Eval.now(BigInt(0))) { case (i: Int, a: Eval[BigInt]) => a.map(_+i) } .value`.

Note that the conversion of the expression is canonical enough that you can actually get back to your
original type signature for `foldRight`, calling to the `Eval`-based version.

### 4.7 The Writer Monad

`cats.data.Writer` lets you carry a log around with computation, so that you can retrieve it with the
final computation.

#### 4.6.1 Creating and Unpacking Writers

`Writer[W, A]` has a log of type `W` and a result of type `A`. When you instantiate one you'll actually
see a `WriterT[Id, W, A]`, but we'll get back to that when we talk about monad transformers later.

To get a `pure` value, you need an implicit `Monoid[W]`, but can then otherwise convert a normal value
into a `Writer`-based value with an `empty` log. Alternatively, if you only have log messages, you can
get a `Writer[W, Unit]` with the `.tell` syntax method.

The value and log of a `Writer` are available with `.value` and `.written` methods, or you can get both
with `.run` (e.g., `val (log, v) = w.run`).

#### 4.7.2 Composing and Transforming Writers

The log is maintained when you `map` or `flatMap`, and for `flatMap` the logs are concatenated (so its
good to use something with an efficient append/concatenate, like `Vector`, for the `W` type). You can
also transform the log with `.mapWritten`. You can do both simultaneously with `.bimap` or `.mapBoth`,
or clear the log with `.reset`, or, if you were inclined, `.swap` the log and the result.

### 4.8 The Reader Monad

`cats.data.Reader` sequences operations that depend on some input. They are frequently used for dependency
injection - you can chain together a bunch of things that depend on an external config, and then bundle
them all up into one and supply the config.

#### 4.8.1 Creating and Unpacking Readers

You can build a `Reader[A, B]` with the apply method, passing in a function `A => B`. You can then apply
that function by extracting it with `.run`. So, `Reader(f).run(a)` is notionally equivalent to `f(a)`.
In fact, it's not terribly wrong to view `Reader[A, B]` as `Function1[A, B]`.

#### 4.8.2 Composing Readers

Since it's a monad, you can `flatMap` and thus compose readers, and in particular readers with the same
input type.

#### 4.8.3 Exercise: Hacking on Readers

#### 4.8.4 When to Use Readers?

Readers are one of many methods for depenedency injection, such as methods with multiple parameter lists,
implicit parameters and type classes, cake pattersn and DI frameworks. Readers are useful when:

* We are constructing a batch program that can be easily represented by a function
* We need to defer injection of parameters
* We want to test parts in isolation

When there are lots of dependencies, or you don't have a pure function, other techniques may be more
appropriate.

You may notice that `Reader` is actually implemented in terms of a thing called `Kleisli`, which represent
`A => F[B]`. `Reader` is the case where `F` is the `Id` monad. We'll see more about Kleisli in the next chapter.

### 4.9 The State Monad

`cats.data.State` lets you pass state around as part of a computation. This lets you model mutable state
in a purely functional way.

#### 4.9.1 Creating and Unpacking State

`State[S, A]` represents functions of type `S => (S, A)`, where `S` is the type of the state, `A` is the type
of the result. So instances of `State` are functions that update a state and compute a result. You run the
functions by supplying an initial state, using `run`, `runS`, or `runA` which return different combinations
of the state and the result, wrapped in an `Eval`, for which you can apply `.value` to get the value.

#### 4.9.2 Composing and Transforming State

As with `Reader`, `State`'s `map` and `flatMap` sequence operations, threading the state through each step.

The general model for using `State` is to represent each step as an instance, and then compose the steps
with standard monad operators (e.g., `flatMap`, for-comprehensions).

* `State.get.run(s0).value` returns `(s0, s0)`
* `State.set(s).run(s0).value` returns `(s1, ())` (ignoring `s0`)
* `State.pure(v).run(s0).value` returns `(s0, v)`
* `State.inspect(f).run(s0).value` returns `(s0, f(s0))`
* `State.modify(f).run(s0).value` returns `(f(s0),())`

These are typically chained together in a `for`-comprehension, like:

```
val prog: State[Int, (Int, Int, Int)] = for {
  a <- get[Int]
  _ <- set[Int](a + 1)
  b <- get[Int]
  _ <- modify[Int](_ + 1)
  c <- inspect[Int, Int](_ * 1000)
} yield (a, b, c)

val initialState = 1
prog.run(initialState).value // returns (3, (1, 2, 3000))
```

Note, in the above, that the arrows are pulling the value out of the expressions, so those values that are
unit can be ignored.

#### 4.9.3 Exercise: [Post-Order Calculator](postfix)

`State` allows us to implement simple interpreters for complex expressions, passing the values of
mutable registers along with the result.

Post-order calulators maintain a stack of observed values, and when an operator is encountered, they pop
values off the stack, apply the operator, and push the result back on the stack. They don't require any
parentheses.

We can represent this with `State`, where we parse each symbol into a `State` that transforms the stack.
They'll then be threaded together with `flatMap`.

### 4.10 Defining Custom Monads

You can define a custom monad by defining `flatMap`, `pure`, and `tailRecM`. The final one we haven't talked
about yet, but it has type `def tailRecM[A, B](a: A)(fn: A => M[Either[A, B]]): M[B]`, where `M` is the type
you are providing the instance for.

`tailRecM` is used by Cats to save stack space when there are nested calls to `flatMap`. It should recursively
call itself until the result of `fn` is a `Right`.

#### 4.10.1 Exercise: Branching out Further with Monads

### 4.11 Summary

Monads provide `flatMap` which allows for sequencing computations. `Option`, `Either`, `List`, and
`Future` are all examples.

