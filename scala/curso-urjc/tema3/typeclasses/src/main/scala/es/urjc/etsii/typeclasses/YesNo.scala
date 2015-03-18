package es.urjc.etsii.typeclasses

import es.urjc.etsii.Student

trait YesNo[A] {
  def yesNo(x: A): Boolean
}
