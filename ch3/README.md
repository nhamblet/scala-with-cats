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

