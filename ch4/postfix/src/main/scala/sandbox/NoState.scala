package sandbox

object NoStatePostfix {

val plusOp: ((Int, Int) => Int) = (a, b) => { a + b }
val timesOp: ((Int, Int) => Int) = (a, b) => { a * b }

def operator(stack: List[Int], f: (Int, Int) => Int): List[Int] = {
  stack match { 
    case h1::(h2::t) =>
      val ans = f(h1,h2)
      ans::t
    case _ =>
      ???
  }
}

def operand(stack: List[Int], i: Int): List[Int] = {
  i :: stack
}

/**
 * i originally wrote this with a try to see if the string was an int, and in the
 * catch would farm out to operators. after reading the suggestion, i re-arranged to
 * the much cleaner current version.
 **/
def evalOne(stack: List[Int], sym: String): List[Int] = {
  sym match {
    case "+" => operator(stack, plusOp)
    case "*" => operator(stack, timesOp)
    case num => operand(stack, num.toInt) // could throw exception
  }
}

def evalAll(input: List[String]): Int = {
  val finalStack = input.foldLeft(List.empty[Int]) { (a, b) =>
    evalOne(a, b)
  }
  finalStack.head
}

}

object NoStateMain extends App {
  import sandbox.NoStatePostfix._
  val programValue = evalAll(List("1", "2", "+", "3", "*"))
  println(programValue)
}
