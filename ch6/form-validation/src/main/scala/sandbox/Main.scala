package sandbox

//import cats._
import cats.data.Validated
import cats.implicits._

case class User(name: String, age: Int)

object FormValidation {

  type FailFast[A] = Either[List[String], A]
  type FailSlow[A] = Validated[List[String], A]

  def getValue(params: Map[String, String], key: String): FailFast[String] =
    params.get(key).toRight(List(s"$key not provided"))

  def parseInt(str: String, name: String): FailFast[Int] =
    Either.catchOnly[NumberFormatException](str.toInt)
      .leftMap(_ => List(s"Parameter `$name` is not an integer"))

  def nonBlank(str: String, name: String): FailFast[String] =
    Right(str).ensure(List(s"Parameter `$name` cannot be blank"))(_.nonEmpty)

  def nonNegative(int: Int, name: String): FailFast[Int] =
    Right(int).ensure(List(s"Parameter `$name` cannot be negative"))(_ >= 0)

  def readName(params: Map[String, String]): FailFast[String] =
    getValue(params, "name").flatMap(s => nonBlank(s, "name"))

  def readAge(params: Map[String, String]): FailFast[Int] =
    getValue(params, "age")
      .flatMap(s => nonBlank(s, "age"))
      .flatMap(s => parseInt(s, "age"))
      .flatMap(s => nonNegative(s, "age"))

  def readValidName(params: Map[String, String]): FailSlow[User] =
    (
      readName(params).toValidated,
      readAge(params).toValidated
    ).mapN(User.apply)

}

object Main extends App {
  println("Hello " |+| "Cats!")
}
