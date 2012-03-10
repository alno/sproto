package org.sproto.mongo

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.sproto._
import org.sproto.mongo.MongoReadProtocol._
import org.sproto.mongo.MongoWriteProtocol._
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject

trait Helpers { self: Spec with ShouldMatchers =>

  def dbList(elems: Any*) = {
    val l = new BasicDBList
    elems.foreach(x => l.add(x.asInstanceOf[AnyRef]))
    l
  }

  def dbObject(elems: (String, Any)*) = {
    val o = new BasicDBObject
    elems.foreach(x => o.put(x._1, x._2.asInstanceOf[AnyRef]))
    o
  }

  def shoudBeWritedAndConvertedToDBObject[T](obj: T, res: BasicDBObject)(implicit cw: CanWrite[T, MapWriter[MongoWriter]]) {
    it("should be writed to MongoWriter") {

      val w = new MongoWriter
      write(obj, w)
      w.result should equal(res)
    }

    it("should be converted to BasicDBObject") {
      (to(obj): BasicDBObject) should equal(res)
    }
  }

  def shoudBeReadAndConvertedFromDBObject[T](src: BasicDBObject, obj: T)(implicit ct: CanRead[T, MapReader[MongoReader]]) {
    it("should be read from MongoReader") {
      (read(new MongoReader(src)): T) should equal(obj)
    }
  }

  case class ObjSimple(a: String, b: Int, c: Boolean)

  case class ObjWithSub(x: Double, y: ObjSimple)

  case class ObjWithSets(a: Set[String], b: List[Int])

  case class ObjWithOpt(lon: Long, opt: Option[String])

  case class RecursiveObj(a: Option[RecursiveObj])

}
