package sandbox

import scala.collection.mutable.HashMap
import scala.util._

/**
 * Outputs on one run of Time.testTimes():
 *
(State2,1000,10): (0.966,0)
(State2,1000,100): (0.772,0)
(State2,10000,10): (8.059,0)
(State2,10000,100): (8.124,0)
(State2,10000,1000): (9.809,0)
(State2,100000,10): (0.0,1000)
(State2,100000,100): (0.0,1000)
(State2,100000,1000): (0.0,1000)
(State2,100000,10000): (0.0,1000)
(State1,1000,10): (1.719,0)
(State1,1000,100): (0.789,0)
(State1,10000,10): (8.016,0)
(State1,10000,100): (8.106,0)
(State1,10000,1000): (9.618,0)
(State1,100000,10): (0.0,1000)
(State1,100000,100): (0.0,1000)
(State1,100000,1000): (0.0,1000)
(State1,100000,10000): (0.0,1000)
(NoState2,1000,10): (0.265,0)
(NoState2,1000,100): (0.21,0)
(NoState2,10000,10): (2.163,0)
(NoState2,10000,100): (2.195,0)
(NoState2,10000,1000): (2.532,0)
(NoState2,100000,10): (0.0,1000)
(NoState2,100000,100): (0.0,1000)
(NoState2,100000,1000): (0.0,1000)
(NoState2,100000,10000): (0.0,1000)
(NoState1,1000,10): (0.152,0)
(NoState1,1000,100): (0.115,0)
(NoState1,10000,10): (1.327,0)
(NoState1,10000,100): (1.307,0)
(NoState1,10000,1000): (1.491,0)
(NoState1,100000,10): (15.005,0)
(NoState1,100000,100): (12.997,0)
(NoState1,100000,1000): (13.828,0)
(NoState1,100000,10000): (16.35,0)

  *
  * Note that the only thing that doesn't fail is the NoState1, and that it's the quickest anyway
  */

object Time {

  def time(f: List[String] => Int, ops: List[String]): Long = {
    val before = System.currentTimeMillis()
    f(ops)
    val after = System.currentTimeMillis()
    after - before
  }

  import sandbox.Postfix
  val runPf = (ops: List[String]) => {
    Postfix.evalAll(ops).runA(Nil).value
  }

  import sandbox.Postfix2
  val runPf2 = (ops: List[String]) => {
    Postfix2.evalAll(ops).runA(Nil).value
  }

  import sandbox.NoStatePostfix
  val runNs = (ops: List[String]) => {
    NoStatePostfix.evalAll(ops)
  }

  import sandbox.NoStatePostfix2
  val runNs2 = (ops: List[String]) => {
    NoStatePostfix2.evalAll(ops)
  }

  def update(m: HashMap[(String, Int, Int), (Long, Int)], n: String, tl: Int, md: Int, f: List[String] => Int, ops: List[String]) = {
    try {
      val t = time(f, ops)
      m((n, tl, md)) = { val x = m((n, tl, md)); (x._1 + t, x._2) }
    } catch {
      case _ : Throwable => m((n, tl, md)) = { val x = m((n, tl, md)); (x._1    , x._2 + 1) }
    }
  }

  def testTimes(n: Int = 1000) {
    val totalTimes = new HashMap[(String, Int, Int), (Long, Int)]()
    val shapes = Seq( (1000, 10), (1000, 100), (10000, 10), (10000, 100), (10000, 1000), (100000, 10), (100000, 100), (100000, 1000), (100000, 10000) )
    for {
      key <- Seq("State1", "State2", "NoState1", "NoState2")
      (tl, md) <- shapes
    } {
      totalTimes((key, tl, md)) = (0L, 0)
    }
    for ((tl, md) <- shapes) {
      println(s"Running $tl $md")
      for (i <- Range(0, n)) {
        val ops = Gen(tl, md)
        update(totalTimes, "State1", tl, md, runPf, ops)
        update(totalTimes, "State2", tl, md, runPf2, ops)
        update(totalTimes, "NoState1", tl, md, runNs, ops)
        update(totalTimes, "NoState2", tl, md, runNs2, ops)
      }
    }
    val avgTimes = totalTimes.mapValues(i => ((i._1*1.0) / (n*1.0), i._2))
    for ((k,v) <- avgTimes)
      println(s"$k: $v")
  }

}
