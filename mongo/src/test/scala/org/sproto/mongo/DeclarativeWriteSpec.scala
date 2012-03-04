package org.sproto.mongo

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.sproto._
import org.sproto.mongo.MongoWriteProtocol._
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject

@RunWith(classOf[JUnitRunner])
class DeclarativeWriteSpec extends Spec with ShouldMatchers {
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

  describe("Object") {
    val w = new MongoWriter

    case class Aaa(a: String, b: Int)

    describe("with specialized writing") {

      implicit val canWriteAaa = canWriteAsProduct((p: Aaa) => Aaa.unapply(p).get)("a", "b")

      it("should be writed to any") {
        write(Aaa("ddd", 23), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa("ddd", 23)): BasicDBObject) should equal(dbObject("a" -> "ddd", "b" -> 23))
      }

      describe("with subobjects") {

        case class Bbb(x: Double, y: Aaa)

        implicit val canWriteBbb = canWriteAsProduct((p: Bbb) => Bbb.unapply(p).get)("x", "y")

        it("should be writed to any") {
          write(Bbb(1.0, Aaa("ddd", 23)), w)
        }

        it("should be converted to BasicDBObject") {
          (to(Bbb(1.0, Aaa("ddd", 23))): BasicDBObject) should equal(dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23)))
        }

      }

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[String, W], cwi: CanWrite[Int, W]): CanWrite[Aaa, MapWriter[W]] = canWriteAsProduct((p: Aaa) => Aaa.unapply(p).get)("a", "b")

      it("should be writed to any") {
        write(Aaa("ddd", 23), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa("ddd", 23)): BasicDBObject) should equal(dbObject("a" -> "ddd", "b" -> 23))
      }

      describe("with subobjects") {

        case class Bbb(x: Double, y: Aaa)

        implicit def canWriteBbb[W](implicit cwd: CanWrite[Double, W], cwa: CanWrite[Aaa, W]): CanWrite[Bbb, MapWriter[W]] = canWriteAsProduct((p: Bbb) => Bbb.unapply(p).get)("x", "y")

        it("should be writed to any") {
          write(Bbb(1.0, Aaa("ddd", 23)), w)
        }

        it("should be converted to BasicDBObject") {
          (to(Bbb(1.0, Aaa("ddd", 23))): BasicDBObject) should equal(dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23)))
        }

      }

    }

  }

  describe("Object with lists and sets") {
    val w = new MongoWriter

    case class Aaa(a: Set[String], b: List[Int])

    describe("with specialized writing") {

      implicit val canWriteAaa: CanWrite[Aaa, MapWriter[MongoWriter]] = canWriteAsProduct((p: Aaa) => Aaa.unapply(p).get)("a", "b")

      it("should be writed to any") {
        write(Aaa(Set("ddd"), List(23, 11)), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa(Set("ddd"), List(23, 11))): BasicDBObject) should equal(dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)))
      }

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[Set[String], W], cwi: CanWrite[List[Int], W]): CanWrite[Aaa, MapWriter[W]] = canWriteAsProduct((p: Aaa) => Aaa.unapply(p).get)("a", "b")

      it("should be writed to any") {
        write(Aaa(Set("ddd"), List(23, 11)), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa(Set("ddd"), List(23, 11))): BasicDBObject) should equal(dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)))
      }

    }

  }

  describe("Recursive object") {
    val w = new MongoWriter

    case class Aaa(a: Option[Aaa])

    describe("with specialized writing") {

      implicit object canWriteAaa extends CanWrite[Aaa, MapWriter[MongoWriter]] {

        def write(that: Aaa, to: MapWriter[MongoWriter]) {
          that.a.foreach(writeField("a", _, to))
        }

      }

      it("should be writed to any") {
        write(Aaa(Some(Aaa(None))), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa(Some(Aaa(None)))): BasicDBObject) should equal(dbObject("a" -> dbObject()))
      }

    }

  }

}
