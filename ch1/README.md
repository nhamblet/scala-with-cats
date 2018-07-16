## Chapter 1 Introduction

Much of the Cats functionality comes in through type classes, a method for library
extension without traditional inheritance.

### 1.1 Anatomy of a Type Class

A type class consists of 3 components:
1. the type class
2. instances for particular types
3. interface methods exposed to the user

#### 1.1.1 The Type Class

In Cats, a type class is a trait with at least one type parameter.

#### 1.1.2 Type Class Instances

Instances provide implementations for the types we care about.

`implicit` instances of the type class (trait) are the method to accomplish this in scala.

#### 1.1.3 Type Class Interfaces

The interface exposes functionality to users. They are generic methods that
accept instances of the typeclass as implicit parameters.

Interface Objects are a good way to make interfaces, just an `object` with member `def`s.

Interface Syntax is the other method of creation, using extension methods to extend types.
This means creating an `object` (typically ending with the name "Syntax"), and then an
`implicit class` with a `def` that takes the `implicit` type class instance.
This syntax has previously been referred to as "type enrichment" or "pimping", but that's
no longer standard (try "bedazzler pattern"!).

`implicitly` is a good built-in for recovering implicit instances.

### 1.2 Working with Implicits

#### 1.2.1 Packaging Implicits

Implicit definitions must be in a containing object or trait.

#### 1.2.2 Implicit Scope

The implicit scope is roughly
* local or inherited definitions
* imported definitions
* definitions in the companion object of the type class or paramater type

The compiler will fail if it finds multiple implicit candidates.

#### 1.2.3 Recursive Implicit Resolution

The compiler can combine implicits when searching for candidates.

You can create instances with concrete instance `val`s, or with an `implicit` method
that constructs instances from other type class instances.

Note that to have an `implicit def` used during implicit resolution, the parameters
must have the `implicit` keyword also. Without the `implicit` keyword on the parameters,
you are defining an _implicit conversion_, which is frowned upon in modern Scala.

### 1.3 [Exercise: Printable Library](s3_printable.scala)

### 1.4 Meet Cats

Cats provides the `cats.Show` type class to mimic the `Printable` class of the previous
sections' exercise. It has method `def show(value: A): Show`.

#### 1.4.1 Importing Type Classes

The type classes are in the `cats` package. Their companion objects have `apply` methods
that try to find instances of a type, but you have to have instances...

#### 1.4.2 Importing Default Instances

`cats.instances` has default instances. It has sub-packages for a handful of specific types
(int, string, list, option, and the catch-all: all).

#### 1.4.3 Importing Interface Syntax

Type classes also have syntax objects in the `cats.syntax` package. For example, `cats.syntax.show._`
provides the extension method (bedassler pattern) conveniences.

#### 1.4.4 Importing All The Things!

`cats._` gives you all the type classes, and `cats.instances.all._` gives you all the instances.
Similarly, `cats.syntax.all._` gives you the extension method conveniences, and
`cats.implicits._` has all the type classes instances _and_ syntax.

So mostly you can `import cats._` and `import cats.implicits._` and be good to go.

#### 1.4.5 Defining Custom Instances

You can always provide your own instances of the cats type classes.

There are conveniences for `Show`, that take a function `A => String` and produce a `Show[A]` for you.

#### 1.4.6 Exercise: [Cat Show](s4_catShow.scala)

### 1.5 Example: Eq

`cats.Eq` is designed to support *type-safe equality*, addressing issues with the build-in `==`.
In particular, `==` can be called for any pair of objects, even those of different types, which can
lead to bugs.

#### 1.5.1 Equality, Librery, and Fraternity

`trait Eq[A] { def eqv(a: A, b: A): Boolean }`

There are a few conveniences in `cats.syntax.eq`, in particular, `===` and `=!=` operators that rely on `eqv`.

#### 1.5.2 Comparing Ints

`cats.instances.int._` provides `Eq[Int]`

#### 1.5.3 Comparing Options

`cats.instances.options._` provides `Eq[Option]`

Note that `Some` is a different type from `Option`. `cats.syntax.option._` provides `.some` and `none[T]`
methods as conveniences for creating option-y literals.

#### 1.5.4 Comparing Custom Types

`Eq.instance[T]` takes a function `(T, T) => Boolean` and produces an `Eq[T]`.

#### 1.5.5 Exercise: Equality, Liberty, and Felinity

[s1_5_5.scala](s1_5_5.scala)


