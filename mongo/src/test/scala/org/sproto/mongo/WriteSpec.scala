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
class WriteSpec extends Spec with ShouldMatchers with Helpers {

  describe("String") {
    it("should be writen as is") {
      val w = new MongoWriter

      write("qwerty", w)

      w.result should equal("qwerty")
    }
    it("should be converted as is") {
      to("qwerty") should equal("qwerty")
    }
  }

  describe("Array") {
    it("of strings should be writed to any") {
      val w = new MongoWriter

      write(List("aaa", "bbb"), w)

      w.result should equal(dbList("aaa", "bbb"))
    }
    it("of strings should be converted to any") {
      (to(List("aaa", "bbb")): BasicDBList) should equal(dbList("aaa", "bbb"))
    }
    it("of integers should be writed to any") {
      val w = new MongoWriter

      write(List(1, 2, 3), w)

      w.result should equal(dbList(1, 2, 3))
    }
    it("of integers should be converted to any") {
      (to(List("aaa", "bbb")): BasicDBList) should equal(dbList("aaa", "bbb"))
    }
  }

  describe("Object") {

    describe("with specialized writing") {

      implicit object canWriteAaa extends CanWrite[ObjSimple, MapWriter[MongoWriter]] {

        def write(that: ObjSimple, to: MapWriter[MongoWriter]) {
          writeField("a", that.a, to)
          writeField("b", that.b, to)
          writeField("c", that.c, to)
        }

      }

      shoudBeWritedAndConvertedToDBObject(ObjSimple("ddd", 23, true), dbObject("a" -> "ddd", "b" -> 23, "c" -> true))

      describe("with subobjects") {

        implicit object canWriteBbb extends CanWrite[ObjWithSub, MapWriter[MongoWriter]] {

          def write(that: ObjWithSub, to: MapWriter[MongoWriter]) {
            writeField("x", that.x, to)
            writeField("y", that.y, to)
          }

        }

        shoudBeWritedAndConvertedToDBObject(ObjWithSub(1.0, ObjSimple("ddd", 23, false)), dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23, "c" -> false)))

      }

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[String, W], cwi: CanWrite[Int, W], cwb: CanWrite[Boolean, W]) = new CanWrite[ObjSimple, MapWriter[W]] {

        def write(that: ObjSimple, to: MapWriter[W]) {
          writeField("a", that.a, to)
          writeField("b", that.b, to)
          writeField("c", that.c, to)
        }

      }

      shoudBeWritedAndConvertedToDBObject(ObjSimple("ddd", 23, false), dbObject("a" -> "ddd", "b" -> 23, "c" -> false))

      describe("with subobjects") {

        implicit def canWriteBbb[W](implicit cwd: CanWrite[Double, W], cwa: CanWrite[ObjSimple, W]) = new CanWrite[ObjWithSub, MapWriter[W]] {

          def write(that: ObjWithSub, to: MapWriter[W]) {
            writeField("x", that.x, to)
            writeField("y", that.y, to)
          }

        }

        shoudBeWritedAndConvertedToDBObject(ObjWithSub(1.0, ObjSimple("ddd", 23, true)), dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23, "c" -> true)))

      }

    }

  }

  describe("Object with lists and sets") {

    describe("with specialized writing") {

      implicit object canWriteAaa extends CanWrite[ObjWithSets, MapWriter[MongoWriter]] {

        def write(that: ObjWithSets, to: MapWriter[MongoWriter]) {
          writeField("a", that.a, to)
          writeField("b", that.b, to)
        }

      }

      shoudBeWritedAndConvertedToDBObject(ObjWithSets(Set("ddd"), List(23, 11)), dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)))

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[Set[String], W], cwi: CanWrite[List[Int], W]) = new CanWrite[ObjWithSets, MapWriter[W]] {

        def write(that: ObjWithSets, to: MapWriter[W]) {
          writeField("a", that.a, to)
          writeField("b", that.b, to)
        }

      }

      shoudBeWritedAndConvertedToDBObject(ObjWithSets(Set("ddd"), List(23, 11)), dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)))

    }

  }

  describe("Object with optional field") {

    describe("with specialized writing") {

      implicit object canWriteAaa extends CanWrite[ObjWithOpt, MapWriter[MongoWriter]] {

        def write(that: ObjWithOpt, to: MapWriter[MongoWriter]) {
          writeField("lon", that.lon, to)
          writeField("opt", that.opt, to)
        }

      }

      describe("with Some in content") {
        shoudBeWritedAndConvertedToDBObject(ObjWithOpt(11, Some("aaa")), dbObject("lon" -> 11, "opt" -> "aaa"))
      }

      describe("with None in content") {
        shoudBeWritedAndConvertedToDBObject(ObjWithOpt(56, None), dbObject("lon" -> 56))
      }

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[Long, W], cwi: CanWrite[String, W]) = new CanWrite[ObjWithOpt, MapWriter[W]] {

        def write(that: ObjWithOpt, to: MapWriter[W]) {
          writeField("lon", that.lon, to)
          writeField("opt", that.opt, to)
        }

      }

      describe("with Some in content") {
        shoudBeWritedAndConvertedToDBObject(ObjWithOpt(11, Some("aaa")), dbObject("lon" -> 11, "opt" -> "aaa"))
      }

      describe("with None in content") {
        shoudBeWritedAndConvertedToDBObject(ObjWithOpt(56, None), dbObject("lon" -> 56))
      }

    }

  }

  describe("Recursive object") {

    describe("with specialized writing") {

      implicit object canWriteAaa extends CanWrite[RecursiveObj, MapWriter[MongoWriter]] {

        def write(that: RecursiveObj, to: MapWriter[MongoWriter]) {
          writeField("a", that.a, to)
        }

      }

      shoudBeWritedAndConvertedToDBObject(RecursiveObj(Some(RecursiveObj(None))), dbObject("a" -> dbObject()))

    }

  }

}
