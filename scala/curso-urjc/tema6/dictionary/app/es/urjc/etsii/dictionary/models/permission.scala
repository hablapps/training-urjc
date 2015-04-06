package es.urjc.etsii.dictionary.models

sealed trait Permission

case object Read extends Permission

case object ReadWrite extends Permission
