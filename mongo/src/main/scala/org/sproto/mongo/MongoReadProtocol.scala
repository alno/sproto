package org.sproto.mongo

import org.sproto._
import com.mongodb.DBObject
import com.mongodb.BasicDBList

case class MongoReader(value: Any)

class MongoObjectReader(val value: DBObject) extends MapReader[MongoReader] {

  def hasField(name: String) =
    value.containsField(name)

  def readField[T](name: String)(implicit canRead: CanRead[T, MongoReader]) =
    canRead.read(new MongoReader(value.get(name)))

}

class MongoListReader(val value: BasicDBList) extends SeqReader[MongoReader] {

  private var index = 0

  def hasElement =
    value.size > index

  def readElement[T](implicit canRead: CanRead[T, MongoReader]) = {
    val v = value.get(index)
    index += 1
    canRead.read(new MongoReader(v))
  }

}

trait MongoReadProtocolLow {

  implicit def canConvertFrom[T](implicit cr: CanRead[T, MongoReader]) = new CanConvertFrom[Any, T] {

    def convertFrom(src: Any) =
      cr.read(new MongoReader(src))

  }

}

trait MongoReadProtocol extends ReadProtocol with ReadMapSupport with ReadOptionAsNoField with ReadSeqSupport with MongoReadProtocolLow {

  implicit def toObjectReader(r: MongoReader) =
    new MongoObjectReader(r.value.asInstanceOf[DBObject])

  implicit def toListReader(r: MongoReader) =
    new MongoListReader(r.value.asInstanceOf[BasicDBList])

  def canReadDirect[T] = new CanRead[T, MongoReader] {

    def read(from: MongoReader) =
      from.value.asInstanceOf[T]

  }

  def canReadConv[T, C](f: C => T) = new CanRead[T, MongoReader] {

    def read(from: MongoReader) =
      f(from.value.asInstanceOf[C])

  }

  implicit val canReadString = canReadDirect[String]
  implicit val canReadShort = canReadConv[Short, Number](_.shortValue)
  implicit val canReadInt = canReadConv[Int, Number](_.intValue)
  implicit val canReadLong = canReadConv[Long, Number](_.longValue)
  implicit val canReadFloat = canReadConv[Float, Number](_.floatValue)
  implicit val canReadDouble = canReadConv[Double, Number](_.doubleValue)
  implicit val canReadNumber = canReadDirect[Number]
  implicit val canReadBoolean = canReadDirect[Boolean]

}

object MongoReadProtocol extends MongoReadProtocol
