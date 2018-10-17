package sandbox

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import cats._
import cats.implicits._

/**
 * This code might mimic what we'd write first, but which we'll update for cleaner code
 */
object Version1 {

  /**
   * A trait so we can stub it in unit tests, e.g. the client below
   **/
  trait UptimeClient {
    def getUptime(hostname: String): Future[Int]
  }
  
  class TestUptimeClient(hosts: Map[String, Int]) extends UptimeClient {
    def getUptime(hostname: String): Future[Int] =
      Future.successful(hosts.getOrElse(hostname, 0))
  }
  
  class UptimeService(client: UptimeClient) {
    def getTotalUptime(hostnames: List[String]): Future[Int] =
      hostnames.traverse(client.getUptime).map(_.sum)
  }

}

/**
 * The better code
 **/
object Version2 {

  trait UptimeClient[F[_]] {
    def getUptime(hostname: String): F[Int]
  }

  trait RealUptimeClient extends UptimeClient[Future] {
    override def getUptime(hostname: String): Future[Int] = ???
  }

  class TestUptimeClient(hosts: Map[String, Int]) extends UptimeClient[Id] {
    override def getUptime(hostname: String): Int =
      hosts.getOrElse(hostname, 0)
  }

  /**
   * Having the UptimeClient abstract over the return type means we need to update
   * the UptimeService to handle it nicely
   **/
  class UptimeService[F[_]: Applicative](client: UptimeClient[F]) {
    def getTotalUptime(hostnames: List[String]): F[Int] =
      hostnames.traverse(client.getUptime).map(_.sum)
  }
}

object Main extends App {

  import Version2._

  def testTotalUptime() = {
    val hosts    = Map("host1" -> 10, "host2" -> 6)
    val client   = new TestUptimeClient(hosts)
    val service  = new UptimeService(client)
    val actual   = service.getTotalUptime(hosts.keys.toList)
    val expected = hosts.values.sum
    assert(actual == expected) // FAILs in V1 - compares apples (Int) and organges (Future[Int])
  }

  testTotalUptime()

}

