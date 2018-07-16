// 1.3 Exercise: Printable Library

// Exercise 1

trait Printable[A] {
  def format(a: A): String
}


// Exercise 2

object PrintableInstances {
  implicit val printableString = new Printable[String] {
    def format(s: String): String = s
  }
  implicit val printableInt = new Printable[Int] {
    def format(i: Int): String = i.toString
  }
}

// Exercise 3

object Printable {
  def format[A](a: A)(implicit ev: Printable[A]): String = ev.format(a)
  def print[A](a: A)(implicit ev: Printable[A]): Unit = println(format(a))
}


// Using the Library

final case class Cat(name: String, age: Int, color: String)
object Cat {
  implicit val printableCat = new Printable[Cat] {
    import PrintableInstances._
    // cheating
    def formatCheating(c: Cat): String = s"${c.name} is a ${c.age} year-old ${c.color} cat"

    // solution from book
    def format(c: Cat): String = {
      val name  = Printable.format(c.name)
      val age   = Printable.format(c.age)
      val color = Printable.format(c.color)
      s"$name is a $age year-old $color cat"
    }
  }
}

val cat = Cat("Cosmo", 12, "Orange")

Printable.print(cat)

// Better Syntax

// Exercises 1-3

object PrintableSyntax {
  implicit class PrintableOps[A](a: A) {
    def format(implicit ev: Printable[A]): String = ev.format(a)
    def print(implicit ev: Printable[A]): Unit = println(format)
  }
}

// Exercise 4

import Cat._
import PrintableSyntax._
cat.print // note that empty parens here causes a compile issue
