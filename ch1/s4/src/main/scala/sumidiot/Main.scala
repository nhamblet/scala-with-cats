package sumidiot

import cats._
import cats.implicits._


final case class Cat(name: String, age: Int, color: String)
object Cat {
  implicit val showableCat = new Show[Cat] {
    def show(c: Cat): String = {
      val name  = c.name.show
      val age   = c.age.show
      val color = c.color.show
      s"$name is a $age year-old $color cat"
    }
  }
}


object Main extends App {
  val cat = Cat("Cosmo", 12, "Orange")
  println(cat.show)
}
