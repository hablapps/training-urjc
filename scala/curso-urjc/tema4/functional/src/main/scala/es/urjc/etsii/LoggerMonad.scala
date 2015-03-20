package es.urjc.etsii

import scala.language.implicitConversions

import es.urjc.etsii.adt._

object LoggerMonad extends App {

  // side-effect version

  def add(x: Int, y: Int): Int = {
    println(s"add($x, $y)")
    x + y
  }

  def mul(x: Int, y: Int): Int = { 
    println(s"mul($x, $y)")
    x * y
  }

  def square(x: Int): Int = {
    println(s"square($x)")
    x * x
  }

  println("side-effect version")
  val x = add(1, 2)
  val y = square(x)
  mul(4, y)


  // 1st version

  def add1(x: Int, y: Int): (Int, String) = (x + y, s"add($x, $y)")

  def mul1(x: Int, y: Int): (Int, String) = (x * y, s"mul($x, $y)")

  def square1(x: Int): (Int, String) = (x * x, s"square($x)")

  val (x1, s1) = add1(1, 2)
  val (y1, s2) = square1(x1)
  val (_, s3) = mul1(4, y1)

  println("\n1st version")
  println(s1 + "\n" + s2 + "\n" + s3)


  // 2nd version

  def bind[A, B](value: (A, String))(f: A => (B, String)) = {
    val (a, s1) = value
    val (b, s2) = f(a)
    (b, s1 + "\n" + s2)
  }

  val (_, s) = bind(bind(add1(1, 2))(x => square1(x)))(y => mul1(4, y))
  println("\n2nd version")
  println(s)

  // 3rd version

  case class Writer[A](runWriter: (A, String)) {

    def map[B](f: A => B): Writer[B] = {
      val (a, s) = runWriter
      (f(a), s)
    }

    def flatMap[B](f: A => Writer[B]): Writer[B] = {
      val (a, s1) = runWriter
      val (b, s2) = f(a).runWriter
      Writer((b, s1 + "\n" + s2))
    }
  }

  object Writer {
    implicit def tupleToWriter[A](tuple: (A, String)): Writer[A] = 
      new Writer(tuple)
  }

  def add3(x: Int, y: Int): Writer[Int] = (x + y, s"add($x, $y)")

  def mul3(x: Int, y: Int): Writer[Int] = (x * y, s"mul($x, $y)")

  def square3(x: Int): Writer[Int] = (x * x, s"square($x)")

  val writer = for {
    x <- add3(1, 2)
    y <- square3(x)
    z <- mul3(4, y)
  } yield (z)

  println("\n3rd version")
  println(writer.runWriter._2)
}
