import cats._
import cats.instances.int._
import cats.instances.string._
import cats.instances.option._
import cats.syntax.eq._

final case class Cat(name: String, age: Int, color: String)

implicit val catEq: Eq[Cat] =
  Eq.instance[Cat] {
    (c1, c2) => c1.name === c2.name && c1.age === c2.age && c1.color === c2.color
  }


val cat1 = Cat("Garfield", 38, "orange and black")
val cat2 = Cat("Heathcliff", 33, "orange and black")
val cat3 = Cat("Garfield", 38, "orange and black")

val optionCat1 = Option(cat1)
val optionCat2 = Option.empty[Cat]

