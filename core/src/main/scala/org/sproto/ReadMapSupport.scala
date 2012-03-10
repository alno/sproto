package org.sproto

trait CanReadFromField[+That, -Reader] {

  def readFromField(name: String, mapReader: MapReader[Reader]): That

}

trait MapReader[+Reader] {

  def hasField(name: String): Boolean
  def readField[That](name: String)(implicit cr: CanRead[That, Reader]): That

}

trait ReadMapSupportLowest {

  implicit def canReadFromMap[T, R](implicit cr: CanRead[T, R]) = new CanReadFromField[T, R] {

    def readFromField(name: String, mapReader: MapReader[R]) =
      mapReader.readField(name)

  }

}

trait ReadMapSupportLow extends ReadMapSupportLowest {

  implicit def canReadAsMap[T, R](implicit conv: R => MapReader[R], cr: CanRead[T, MapReader[R]]) = new CanRead[T, R] {

    def read(reader: R) =
      cr.read(conv(reader))

  }

}

trait ReadMapSupport extends ReadMapSupportLow with ReadMapSupportGen {

  def readField[T, R](name: String, from: MapReader[R])(implicit crf: CanReadFromField[T, R]): T =
    crf.readFromField(name, from)

}

trait ReadOptionAsNoField {

  implicit def canReadOptionAsNoField[T, R](implicit cr: CanRead[T, R]) = new CanReadFromField[Option[T], R] {

    def readFromField(name: String, mapReader: MapReader[R]) =
      if (mapReader.hasField(name))
        Some(mapReader.readField(name))
      else
        None

  }

}

object ReadMapSupport extends ReadMapSupport
