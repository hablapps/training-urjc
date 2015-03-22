package es.urjc.etsii

import adt._

object ListaMonad extends App {

  def invAbs(x: Int): Lista[Int] = x match {
    case x if x < 0 => Lista()
    case 0 => Lista(0)
    case x if x > 0 => Lista(x, -x)
  }

  def neighbour(x: Int): Lista[Int] = Lista(x-1, x+1)


  // 1st version

  val ngh: Lista[Int] = neighbour(10)
  val inv: Lista[Int] = ngh.foldLeft(Lista[Int]())((acc, x) => acc ++ invAbs(x))
  val res1: Lista[Int] = inv.foldLeft(Lista[Int]())((acc, x) => acc ++ neighbour(x))

  println("1st version")
  println(res1)


  // 2nd version

  def bind[A, B](l: Lista[A])(f: A => Lista[B]): Lista[B] = l match {
    case Nada => Nada
    case Cons(x, xs) => f(x) ++ bind(xs)(f)
  }

  val res2 = bind(bind(neighbour(10))(ngh => invAbs(ngh)))(inv => neighbour(inv))

  println("\n2nd version")
  println(res2)


  // 3rd version (flatMap inside)

  val res3_1 = neighbour(10) flatMap { ngh =>
    invAbs(ngh) flatMap { inv =>
      neighbour(inv)
    }
  }

  val res3_2 = for {
    ngh <- neighbour(10)
    inv <- invAbs(ngh)
    res <- neighbour(inv)
  } yield res

  println("\n3rd version")
  println(res3_1)
  println(res3_2)


  // Final Intuition

  val fnl = for {
    n <- List(1, 2, 3, 4, 5)
    c <- List('a', 'b', 'c', 'd')
  } yield (n, c)

  println("\nFinal Intuition")
  println(fnl)
}
