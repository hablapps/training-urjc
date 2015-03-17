package es.urjc.etsii.typeclasses

import es.urjc.etsii.Student

trait YesNo[A] {
  def yesNo(x: A): Boolean
}

object YesNo {

  implicit val yesNoInt: YesNo[Int] = new YesNo[Int] {
    def yesNo(x: Int) = x match {
      case 0 => false
      case _ => true
    }
  }

  implicit val yesNoString: YesNo[String] = new YesNo[String] {
    def yesNo(x: String) = x match {
      case "" => false
      case _ => true
    }
  }
}
