package es.urjc.etsii.typeclass

import es.urjc.etsii.Student

trait Show[A] {
  def show(value: A): String
}

object Show {

  implicit val intShow = new Show[Int] {
    def show(value: Int) = s"Int#$value"
  }

  implicit val stuShow = new Show[Student] {
    def show(st: Student) = s"Student(${st.name}, ${st.last})"
  }
}
