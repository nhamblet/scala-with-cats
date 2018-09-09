package sandbox

import cats.data._
import cats.implicits._

object Postfix {

type CalcState[A] = State[List[Int], A]

val plusOp: ((Int, Int) => Int) = (a, b) => { a + b }
val timesOp: ((Int, Int) => Int) = (a, b) => { a * b }

def operator(f: (Int, Int) => Int): CalcState[Int] = {
  State[List[Int], Int] {
    case h1::(h2::t) =>
      val ans = f(h1,h2)
      (ans::t, ans)
    case _ =>
      ???
  }
}

def operand(i: Int): CalcState[Int] = {
  State[List[Int], Int] { stack =>
    (i :: stack, i)
  }
}

/**
 * i originally wrote this with a try to see if the string was an int, and in the
 * catch would farm out to operators. after reading the suggestion, i re-arranged to
 * the much cleaner current version.
 **/
def evalOne(sym: String): CalcState[Int] = {
  sym match {
    case "+" => operator(plusOp)
    case "*" => operator(timesOp)
    case num => operand(num.toInt) // could throw exception
  }
}

def evalAll(input: List[String]): CalcState[Int] =
  input.foldLeft(0.pure[CalcState]) { (a, b) =>
    a.flatMap(_ => evalOne(b))
  }
}

object Main extends App {
  import sandbox.Postfix._
  val program = evalAll(List("1", "2", "+", "3", "*"))
  println(program.runA(Nil).value)
}
