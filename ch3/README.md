## Chapter 3 Functors

Functors let you represent a sequence of operations in a context. Examples include `List`
and `Option`. Functors themselves are ok, but the extensions into monads and applicatives
are even more commonly used, and are the topic of later chapters.

### 3.1 Examples of Functors

Informally, anything with a `.map` is a functor. Typically we first encounter this with
`List`, though `Option` and `Either` are also standard examples.

When you `.map` a function over a list, the values change but the structure remains the same
(even the length). Similarly for option, if it's a `Some` it stays a `Some`, same for `None`.
We should not think of `.map` as iteration, but as a way to sequence computations on values
(e.g., to `.map` and then `.map` again).

### 3.2 More Examples of Functors

For `List` and `Option` and `Either`, `.map` applies "eagerly", but this isn't always the case.

#### Futures

`Future` sequences asynchronous computations by queueing them and waiting for predecessors to complete.
With a `Future`, when we `.map`, we don't know when our function will be called, because the
computation could still be going, or already done, we just know that it'll be called when it's ready.

##### Referential Transparency

Scala's `Future` is not referentially transparent, its interaction with side-effects isn't great.

#### Functions (?!)

Single argument functions are also functors, if you fix the parameter type. That is, let
`MyFunc[A]` be `X => A` for some fixed type `X`. Then you can `.map` an instance of `MyFunc[A]`,
because if somebody gives you a function, say `f: A => B`, and a `MyFunc[A]`, say `g: X => A`,
then you can compose to get `g map f = g andThen f`.

We can think of `.map`ping over a function as queueing up operations, similar to `Future`, and they
call get applied, in order, when you give the final function an argument to apply to.

### 3.3 Definition of a Functor

A functor is a type `F[A]` that has an operation `map` with type `(A => B) => F[B]`. In cats, it is
represented by `cats.Functor` as

```
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}
```

Note that this relies on higher kinded types, which we'll talk about in the next section.

Funtors are supposed to satisy two laws:

1. identity: `fa.map(a => a) == fa`
2. composition: `fa.map(g(f(_))) = fa.map(f).map(g)`

### 3.4 Aside: Higher Kinds and Type Constructors

Kinds are like types for types. They describe the number of "holes" in a type - "normal" types have
no holes (no type parameters).

`List` is a type with one hole, you give it a type, like `Int`, and you get back a type, `List[Int]`.
Thus, `List` is a type constructor (it constructs types), and `List[Int]` is a type.

This looks a lot like functions and values. Functions take values and produce values. Type constructors
take types and produce types.

In Scala, type constructors are declared using underscore, as in the definition of `Functor` above.
Higher kinded types are taken to be an advanced features in Scala, so you have to explicitly import
`scala.language.higherKinds`, or add the `-language:higherKinds` scalac option.

### 3.5 Functors in Cats

#### 3.5.1 The Functor Type Class

Default instances are arranged by type in `cats.instances`, and there's a `Functor.apply` on the companion
object, so you can write `Functor[List].map(myList)(myFunc)`, for example.

Functor also provides `lift`, converting `A => B` to `F[A] => F[B]`. Mostly this re-arranges the arguments
to `map`.

#### 3.5.2 Functor Syntax

Functions are functors, in `cats.instances.function._`, where `map` is the same as `andThen`. This is
because of the syntax in `FunctorOps` which lets you `.map` a function over an `F[A]`. Note that types
with built-in `map` methods have a conflict, the compile won't prefer a `FunctorOps` `.map` for them.

#### 3.5.3 Instances for Custom Types

If you need to inject a dependency into an instance, you can't modify the `.map` signature, and must
account for the dependency when you create the instance, not when you `.map`.

#### 3.5.4 Exercise: Branching out with Functors

### 3.6 Contravariant and Invariant Functors

We have seen `.map` as a way of appending transformations to a chain. Now we'll see about pre-pending,
and building bi-directional chains, as represented by "contravariant" and "invariant" functors.

#### 3.6.1 Contravariant Functors and the `contramap` Method

`contramap` takes an `F[B]` and an `A => B`, and produces an `F[A]`, so `contramap` flips the direction
of the function arrow (an `A => B` gets lifted to a `F[B] => F[A]`).

`contramap` only makes sense for types that represent transformations. For example, the `Printable[A]` trait
in chapter 1, with method `format(value: A): String` (or `A => String`).

#### 3.6.2 Invariant functors and the `imap` method

The most intuitive examples of an invariant functor are type classes that represent encoding and decoding
as some data type. The `imap` signature is `F[A] => (A => B, B => A) => F[B]`. The example to have in mind
is `Codec[A]`, with methods `encode: A => String` and `decode: String => A`. Given a `Codec[A]`, and
functions `A => B` and `B => A`, we can construct a `Codec[B]` (to encode is roughly contravariant, and to
decode is covariant, which you see in the type signatures for those methods, because the parametric type
is in the argument/result place of the function, respectively).

If you wanted to capture errors during the encode/decode process, you'd start wanting the lenses and optics
patterns, which are outside the scope of this book, but well represented by the
[Monocle](https://github.com/julien-truffaut/Monocle) library.

### 3.7 Contravariant and Invariant in Cats

Summarizing the above, here's how these functors look in cats:

```
trait Contravariant[F[_]] {
  def contramap[A, B](fa: F[A])(f: B => A): F[B]
}

trait Invariant[F[_]] {
  def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]
}
```

#### 3.7.1 Contravariant in Cats

Cats provides instances for `Eq`, `Show`, and `Function1`, as well as the `cats.syntax.contravariant`
extension method `contramap: (B => A) => F[B]`, which can be applied to an `F[A]`.

#### 3.7.2 Invariant in Cats

Cats provides an instance of `Invariant` for `Monoid`, which is somewhat different from the codec example
above. Basically, though, if somebody gives you a `Monoid[A]` (with `empty: A` and `combine: (A, A) => A`),
and maps `a2b: A => B` and `b2a: B => A`, you can create a `Monoid[B]` where `empty` is `a2b(empty)`, and
`combine(b1, b2)` works by applying `b2a` to both arguments, to bring you back to `A`-world, applying
`A`'s combine, and then `a2b` to send you back to `B` world - `combine(b1, b2) = a2b(combine(b2a(b1), b2a(b2)))`.

### 3.8 Aside: Partial Unification

Returning to the compiler failure in 3.2, which prevented us from calling `.map` on a `Functor1`, despite
the instances being available, _unless_ we had `-Ypartial-unification` turned on.

#### 3.8.1 Unifying Type Constructors

The complication with `Function1` is that it takes 2 type parameters (input and output types), and `Functor`
wants a type that only has 1 type parameter. For `Function1`, the covariant functor wants the result type
to be flexible. Older versions of scala didn't support being able to identify this, while newer ones do,
with the `-Ypartial-unification` option.

#### 3.8.2 Left-to-Right Elimination

The compiler is able to achieve this by fixing parameters working left to right. So while `Function1` has two
parameters, to try to use it as a `Functor` it'll fix the first type (to the appropriate type given the
call context), and see if there's a `Functor` instance when all but the final type are fixed (in this case
it only has to fix one argument, but if there were multiple type parameters, they'd all have to be fixed).

This left-to-right process works for `Function1` and `Either`, as examples. Some types don't work well,
like the contravariant type for `Function1`.

### 3.9 Summary

Functors represent sequencing behaviors. There are three types:

* "regular", *covariant* functors provide `map` that applies a function in a context, and subsequent calls
    get chained in in sequence
* *contravariant* functors provide `contramap`, which "prepends" the functions
* *invariant* functors represent bidirectional transformations.

Regular functors are common, but it's rare to use them on their own. They provide the foundation for the
other type classes, like monads and applicatives, that we'll learn about in the next chapters.

