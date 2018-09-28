package sandbox

import cats.data._

object Postfix2 {

type CalcState[A] = State[List[Int], A]

val plusOp: ((Int, Int) => Int) = (a, b) => { a + b }
val timesOp: ((Int, Int) => Int) = (a, b) => { a * b }

def operator(f: (Int, Int) => Int): State[List[Int], Unit] = {
  State.modify[List[Int]] {
    case h1::(h2::t) =>
      val ans = f(h1,h2)
      ans::t
    case x =>
      ???
      x
  }
}

def operand(i: Int): State[List[Int], Unit] = {
  State.modify[List[Int]] { stack => i :: stack }
}

/**
 * i originally wrote this with a try to see if the string was an int, and in the
 * catch would farm out to operators. after reading the suggestion, i re-arranged to
 * the much cleaner current version.
 **/
def evalOne(sym: String): State[List[Int], Unit] = {
  sym match {
    case "+" => operator(plusOp)
    case "*" => operator(timesOp)
    case num => operand(num.toInt) // could throw exception
  }
}

def evalAll(input: List[String]): CalcState[Int] =
  input.foldLeft(State.pure[List[Int], Unit](())) { (a, b) =>
    a.flatMap(_ => evalOne(b))
  }.inspect(_.head)
}

object Main2 extends App {
  import sandbox.Postfix2._
  val program = evalAll(List("1", "2", "+", "3", "*"))
  println(program.runA(Nil).value)
}
