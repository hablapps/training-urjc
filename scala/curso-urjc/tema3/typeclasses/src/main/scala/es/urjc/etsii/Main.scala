package es.urjc.etsii

import typeclasses._

object Main extends App {

  val michael = Student("Michael", "Scott", Some("funny guy"))
  val buffy = Student("Buffy", "Summers", Some("brave girl"))
  val george = Student("George", "Bluth", Some("magician"))

  def find[A: Eq](list: List[A], a1: A) = {
    val comparator = implicitly[Eq[A]]
    list.find(a2 => comparator.eq(a1, a2))
  }

  val res = find(List(michael, buffy, george), Student("Buffy", "Summers"))
  println("RESULT WAS: " + res)

  // def `if`[A: YesNo, B](cond: A)(`then`: => B, `else`: => B) {
  //   if (implicitly[YesNo[A]].yesNo(cond) `then` else `else`
  // }

  // `if`("hola")(`then` = true, `else` = false)
}
