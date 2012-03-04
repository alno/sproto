package org.sproto.mongo

import org.sproto._
import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList

class MongoWriter {

  var result: Any = null

}

class MongoObjectWriter extends MapWriter[MongoWriter] {

  val result = new BasicDBObject

  def writeField[T](name: String, value: T)(implicit canWrite: CanWrite[T, MongoWriter]) {
    val anyWriter = new MongoWriter // TODO Reusing?
    canWrite.write(value, anyWriter)
    result.put(name, anyWriter.result.asInstanceOf[AnyRef])
  }

}

class MongoListWriter extends SeqWriter[MongoWriter] {

  val result = new BasicDBList

  def writeElement[T](value: T)(implicit canWrite: CanWrite[T, MongoWriter]) {
    val anyWriter = new MongoWriter // TODO Reusing?
    canWrite.write(value, anyWriter)
    result.add(anyWriter.result.asInstanceOf[AnyRef])
  }

}

trait MongoWriteProtocolLowest {

  implicit def canConvert[T](implicit cw: CanWrite[T, MongoWriter]) = new CanConvertTo[T, Any] {

    def convert(that: T) = {
      val w = new MongoWriter
      cw.write(that, w)
      w.result
    }

  }

}

trait MongoWriteProtocol extends WriteProtocol with MongoWriteProtocolLowest {

  implicit def canConvertToObj[T](implicit cw: CanWrite[T, MongoObjectWriter]) = new CanConvertTo[T, BasicDBObject] {

    def convert(that: T) = {
      val w = new MongoObjectWriter
      cw.write(that, w)
      w.result
    }

  }

  implicit def canConvertToList[T](implicit cw: CanWrite[T, MongoListWriter]) = new CanConvertTo[T, BasicDBList] {

    def convert(that: T) = {
      val w = new MongoListWriter
      cw.write(that, w)
      w.result
    }

  }

  implicit def toObjectWriter(w: MongoWriter) = {
    val r = new MongoObjectWriter
    w.result = r.result
    r
  }

  implicit def toListWriter(w: MongoWriter) = {
    val r = new MongoListWriter
    w.result = r.result
    r
  }

  def canWriteDirect[T] = new CanWrite[T, MongoWriter] {
    def write(that: T, to: MongoWriter) {
      to.result = that
    }
  }

  implicit val canWriteString = canWriteDirect[String]
  implicit val canWriteInt = canWriteDirect[Int]
  implicit val canWriteDouble = canWriteDirect[Double]
  implicit val canWriteNumber = canWriteDirect[Number]

}

object MongoWriteProtocol extends MongoWriteProtocol
