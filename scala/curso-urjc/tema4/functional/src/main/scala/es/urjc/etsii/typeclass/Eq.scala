package es.urjc.etsii.typeclass

import es.urjc.etsii.Student

trait Eq[A] {
  def eq(x: A, y: A): Boolean
}

object Eq {

  implicit val eqInt: Eq[Int] = new Eq[Int] {
    def eq(x: Int, y: Int) = x == y
  }

  implicit val eqStudent: Eq[Student] = new Eq[Student] {
    def eq(st1: Student, st2: Student) =
      (st1.name == st2.name) && (st1.last == st2.last)
  }
}
