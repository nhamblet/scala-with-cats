## Chapter 5 Monad Transformers

It is not uncommon to nest monads, like an `Either[Error, Option[User]]` for database queries,
where things can go wrong (`Either`) or the user just doesn't exist (`Option`). Dealing with
this then translates into nested for-comprehensions.

### 5.1 Exercise: Composing Monads

It's impossible, in general, to write a composed monad, `M1[M2[A]]`, where `M1` and `M2` are
monads, but if you know a little about either, you can do it. For example, if `M2` is `Option`,
then you can implement the composite `flatMap`.

### 5.2 A Transformative Example

There are transformers for many monads in cats, using the `T` suffix on the name. So, `EitherT`
composes `Either` with other monads.

`OptionT[List, A]` transforms a `List[Option[A]]`, creating a monad representing that composite.

### 5.3 Monad Transformers in Cats

Each monad transformer is its own type, in `cats.data`.

#### 5.3.1 The Monad Transformer Classes

Each of the monads we've seen so far has a corresponding -`T` transformer, like `OptionT`.

#### 5.3.2 Building Monad Stacks

The transformer represents the _inner_ monad of the stack, and the first type parameter represents
the outer monad. The final type parameter represents the type parameter of the resulting monad.

It is common to define type aliases for intermediate stages of a monad stack, especially for things
like `Either` which have two two parameters anyway.

**Kind Projector** is a useful compiler plugin when building monad stacks. It makes it easier to
define partially applied type constructors, using `?` as a placeholder in a type with parameters.

#### 5.3.3 Constructing and Unpacking Instances

We can use the transformer's `apply` method, or usual `pure` syntax to create instances. To unpack
a stack you can use `.value` to return the untransformed stack, pulling one layer off the monad
stack, one at a time.

#### 5.3.4 Default Instances

#### 5.3.5 Usage Patterns

It is normal to create a "super stack" representing a large (e.g., `Future[Either[Error, A]]]`)
stack that is used throughout your application. This may be used as "glue code" between modules,
which may then unpack on receipt of such an object, and re-pack before sending to another module.
With this pattern, each module can make it's own decisions of which transfomers to use.

Given the generality, it's hard to find one-size-fits-all solutions.

### 5.4 Exercise: Monads: Transform and Roll Out

### 5.5 Summary

Monad transformers fold monads together so that you can use them without nested for-comprehensions.
They have the name of the monad they transform, with a `T` suffice (e.g., `OptionT`). The transformer
class represents the _inner_ monad type, and the first type parameter represents the outer monad
type.

