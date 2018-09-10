package sandbox

import sandbox.MonadicTree._

object MonadicTree {
  sealed trait Tree[+A]
  final case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
  final case class Leaf[A](value: A) extends Tree[A]

  // non-monad question: why are these useful?
  def branch[A](left: Tree[A], right: Tree[A]): Tree[A] = Branch(left, right)
  def leaf[A](value: A): Tree[A] = Leaf(value)


  /**
   * The exercise solution shows how you can solve the following:
   *   Write a function that takes a tree and converts every leaf to a 2-deep tree where the values
   *   are the values of the original leaf offset by -11, -9, 9, and 11, respectively
   * The method below also solves that problem. Some/many might argue that it's easier to
   * comprehend (get it!) than the for-comprehension version, because it's not entirely
   * intuitive at first what for-comprehension on a tree _means_.
   *
   * If this were the only thing one were doing with the Tree type, it seems like a lot
   * less work to write this, than to write the Monad instance for Tree (in particular the
   * tailRecM function). However, if you were going to be doing more, or possibly more
   * complicated (or composite) tree operations, then you'd start to appreciate the Monad and
   * associated for-comprehension.
   */
  def modTree(tree: Tree[Int]): Tree[Int] = {
    tree match {
      case Branch(l, r) => Branch(modTree(l), modTree(r))
      case Leaf(v)      => Branch(Branch(Leaf(v-11),Leaf(v-9)), Branch(Leaf(v+9),Leaf(v+11)))
    }
  }
}

object Main extends App {
  println(modTree(Branch(Leaf(100), Leaf(200))))
}
