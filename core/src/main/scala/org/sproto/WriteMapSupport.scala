package org.sproto

trait CanWriteInField[-That, -Writer] {

  def writeInField(name: String, that: That, mapWriter: MapWriter[Writer])

}

trait MapWriter[+Writer] {

  def writeField[T](name: String, value: T)(implicit cw: CanWrite[T, Writer])

}

trait WriteMapSupportLowest {

  implicit def canWriteInMap[T, W](implicit cw: CanWrite[T, W]) = new CanWriteInField[T, W] {

    def writeInField(name: String, that: T, mapWriter: MapWriter[W]) =
      mapWriter.writeField(name, that)

  }

}

trait WriteMapSupportLow extends WriteMapSupportLowest {

  implicit def canWriteAsMap[T, W](implicit conv: W => MapWriter[W], cw: CanWrite[T, MapWriter[W]]) = new CanWrite[T, W] {

    def write(that: T, writer: W) =
      cw.write(that, conv(writer))

  }

}

trait WriteMapSupport extends WriteMapSupportLow {

  def writeField[T, W](name: String, that: T, to: MapWriter[W])(implicit cwf: CanWriteInField[T, W]) =
    cwf.writeInField(name, that, to)

  def canWriteAsProduct[W, P, T1, T2](dec: P => (T1, T2))(n1: String, n2: String)(implicit cw1: CanWriteInField[T1, W], cw2: CanWriteInField[T2, W]) = new CanWrite[P, MapWriter[W]] {

    def write(that: P, writer: MapWriter[W]) {
      val t = dec(that)
      cw1.writeInField(n1, t._1, writer)
      cw2.writeInField(n2, t._2, writer)
    }

  }

  def canWriteAsProduct[W, P, T1, T2, T3](dec: P => (T1, T2, T3))(n1: String, n2: String, n3: String)(implicit cw1: CanWriteInField[T1, W], cw2: CanWriteInField[T2, W], cw3: CanWriteInField[T3, W]) = new CanWrite[P, MapWriter[W]] {

    def write(that: P, writer: MapWriter[W]) {
      val t = dec(that)
      cw1.writeInField(n1, t._1, writer)
      cw2.writeInField(n2, t._2, writer)
      cw3.writeInField(n3, t._3, writer)
    }

  }

}

trait WriteOptionAsNoField {

  implicit def canWriteOptionAsNoField[T, W](implicit cw: CanWrite[T, W]) = new CanWriteInField[Option[T], W] {

    def writeInField(name: String, that: Option[T], mapWriter: MapWriter[W]) =
      that.foreach(mapWriter.writeField(name, _))

  }

}
