package sandbox

object NoStatePostfix2 {

val plusOp: ((Int, Int) => Int) = (a, b) => { a + b }
val timesOp: ((Int, Int) => Int) = (a, b) => { a * b }

def operator(f: (Int, Int) => Int): List[Int] => List[Int] = stack => {
  stack match { 
    case h1::(h2::t) =>
      val ans = f(h1,h2)
      ans::t
    case _ =>
      ???
  }
}

def operand(i: Int): List[Int] => List[Int] =
  stack => i :: stack

/**
 * i originally wrote this with a try to see if the string was an int, and in the
 * catch would farm out to operators. after reading the suggestion, i re-arranged to
 * the much cleaner current version.
 **/
def evalOne(sym: String): List[Int] => List[Int] = {
  sym match {
    case "+" => operator(plusOp)
    case "*" => operator(timesOp)
    case num => operand(num.toInt) // could throw exception
  }
}

def evalAll(input: List[String]): Int = {
  val composite = input.foldLeft((s: List[Int]) => s) { (f, b) =>
    f andThen evalOne(b)
  }
  composite(List()).head
}

}

object NoStateMain2 extends App {
  import sandbox.NoStatePostfix2._
  val programValue = evalAll(List("1", "2", "+", "3", "*"))
  println(programValue)
}
