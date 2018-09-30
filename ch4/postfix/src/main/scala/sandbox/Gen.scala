package sandbox

import scala.util.Random

object Gen {

  def randomNum(): String =
    Random.nextInt(100).toString

  def randomOp(): String =
    if (Random.nextFloat <= 0.5) "+"
    else                         "*"

  def apply(targetLen: Int, maxDepth: Int): List[String] =
    Gen.apply(targetLen, maxDepth, 0, Vector.empty[String]).toList

  def apply(targetLen: Int, maxDepth: Int, curDepth: Int, ops: Vector[String]): Vector[String] = {
    if (ops.size >= targetLen) {
      if (curDepth > 1) {
        val op = Gen.randomOp()
        Gen.apply(targetLen, maxDepth, curDepth - 1, ops :+ op)
      } else {
        ops
      }
    } else if (curDepth >= maxDepth) {
      val op = Gen.randomOp()
      Gen.apply(targetLen, maxDepth, curDepth - 1, ops :+ op)
    } else {
      if (curDepth >= 2) {
        if (Random.nextFloat() <= (curDepth * 1.0f / (maxDepth*1.0f + 1.0f))) {
          val op = Gen.randomOp()
          Gen.apply(targetLen, maxDepth, curDepth - 1, ops :+ op)
        } else {
          val n = Gen.randomNum()
          Gen.apply(targetLen, maxDepth, curDepth + 1, ops :+ n)
        }
      } else {
        val n = Gen.randomNum()
        Gen.apply(targetLen, maxDepth, curDepth + 1, ops :+ n)
      }
    }
  }
}
