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

}
