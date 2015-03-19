package es.urjc.etsii.typeclass

import es.urjc.etsii.Student

trait YesNo[A] {
  def yesNo(x: A): Boolean
}
