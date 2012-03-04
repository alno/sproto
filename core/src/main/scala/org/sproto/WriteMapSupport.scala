package org.sproto

trait MapWriter[Writer] {

  def writeField[T](name: String, value: T)(implicit cw: CanWrite[T, Writer])

}

trait WriteMapSupportLow {

  implicit def canWriteAsMap[T, W](implicit conv: W => MapWriter[W], cw: CanWrite[T, MapWriter[W]]) = new CanWrite[T, W] {

    def write(that: T, writer: W) =
      cw.write(that, conv(writer))

  }

}

trait WriteMapSupport extends WriteMapSupportLow {

  def writeField[T, W](name: String, that: T, to: MapWriter[W])(implicit cw: CanWrite[T, W]) =
    to.writeField(name, that)

  def canWriteAsProduct[W, P, T1, T2](dec: P => (T1, T2))(n1: String, n2: String)(implicit cw1: CanWrite[T1, W], cw2: CanWrite[T2, W]) = new CanWrite[P, MapWriter[W]] {

    def write(that: P, writer: MapWriter[W]) {
      val t = dec(that)
      writer.writeField(n1, t._1)
      writer.writeField(n2, t._2)
    }

  }

}
