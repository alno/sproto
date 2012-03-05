package org.sproto.mongo

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.sproto._
import org.sproto.mongo.MongoReadProtocol._
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject

@RunWith(classOf[JUnitRunner])
class ReadSpec extends Spec with ShouldMatchers with Helpers {

  def r[T](v: Any)(implicit cr: CanRead[T, MongoReader]): T =
    read(new MongoReader(v))

  describe("String") {
    it("should be read as is") {
      (r("qwerty"): String) should equal("qwerty")
    }
    it("should be converted as is") {
      from[String, Any]("qwerty") should equal("qwerty")
    }
  }

  describe("Array") {
    it("of strings should be readed from any") {
      (r(dbList("aaa", "bbb")): List[String]) should equal(List("aaa", "bbb"))
    }
    it("of strings should be converted from any") {
      (from[List[String], BasicDBList](dbList("aaa", "bbb")): List[String]) should equal(List("aaa", "bbb"))
    }
    it("of integers should be readed to any") {
      (r(dbList(1, 2, 3)): List[Int]) should equal(List(1, 2, 3))
    }
    it("of integers should be converted to any") {
      (from[List[Int], BasicDBList](dbList(1, 5)): List[Int]) should equal(List(1, 5))
    }
  }

  describe("Object") {

    describe("with specialized writing") {

      implicit object canReadAaa extends CanRead[ObjSimple, MapReader[MongoReader]] {

        def read(from: MapReader[MongoReader]) =
          ObjSimple(readField("a", from), readField("b", from), readField("c", from))

      }

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> "ddd", "b" -> 23, "c" -> true), ObjSimple("ddd", 23, true))

      describe("with subobjects") {

        implicit object canReadBbb extends CanRead[ObjWithSub, MapReader[MongoReader]] {

          def read(from: MapReader[MongoReader]) =
            ObjWithSub(readField("x", from), readField("y", from))

        }

        shoudBeReadAndConvertedFromDBObject(dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23, "c" -> false)), ObjWithSub(1.0, ObjSimple("ddd", 23, false)))

      }

    }

    describe("with universal writing") {

      implicit def canReadAaa[R](implicit crs: CanRead[String, R], cri: CanRead[Int, R], crb: CanRead[Boolean, R]) = new CanRead[ObjSimple, MapReader[R]] {

        def read(from: MapReader[R]) =
          ObjSimple(readField("a", from), readField("b", from), readField("c", from))

      }

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> "ddd", "b" -> 23, "c" -> false), ObjSimple("ddd", 23, false))

      describe("with subobjects") {

        implicit def canReadBbb[R](implicit crd: CanRead[Double, R], cra: CanRead[ObjSimple, R]) = new CanRead[ObjWithSub, MapReader[R]] {

          def read(from: MapReader[R]) =
            ObjWithSub(readField("x", from), readField("y", from))

        }

        shoudBeReadAndConvertedFromDBObject(dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23, "c" -> true)), ObjWithSub(1.0, ObjSimple("ddd", 23, true)))

      }

    }

  }

  describe("Object with lists and sets") {

    describe("with specialized writing") {

      implicit object canReadAaa extends CanRead[ObjWithSets, MapReader[MongoReader]] {

        def read(from: MapReader[MongoReader]) =
          ObjWithSets(readField("a", from), readField("b", from))

      }

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)), ObjWithSets(Set("ddd"), List(23, 11)))

    }

    describe("with universal writing") {

      implicit def canReadAaa[R](implicit crs: CanRead[Set[String], R], cri: CanRead[List[Int], R]) = new CanRead[ObjWithSets, MapReader[R]] {

        def read(from: MapReader[R]) =
          ObjWithSets(readField("a", from), readField("b", from))

      }

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)), ObjWithSets(Set("ddd"), List(23, 11)))

    }

  }

  describe("Object with optional field") {

    describe("with specialized writing") {

      implicit object canReadAaa extends CanRead[ObjWithOpt, MapReader[MongoReader]] {

        def read(from: MapReader[MongoReader]) =
          ObjWithOpt(readField("lon", from), readField("opt", from))

      }

      describe("with Some in content") {
        shoudBeReadAndConvertedFromDBObject(dbObject("lon" -> 11, "opt" -> "aaa"), ObjWithOpt(11, Some("aaa")))
      }

      describe("with None in content") {
        shoudBeReadAndConvertedFromDBObject(dbObject("lon" -> 56), ObjWithOpt(56, None))
      }

    }

    describe("with universal writing") {

      implicit def canReadAaa[R](implicit crs: CanRead[Long, R], cri: CanRead[String, R]) = new CanRead[ObjWithOpt, MapReader[R]] {

        def read(from: MapReader[R]) =
          ObjWithOpt(readField("lon", from), readField("opt", from))

      }

      describe("with Some in content") {
        shoudBeReadAndConvertedFromDBObject(dbObject("lon" -> 11, "opt" -> "aaa"), ObjWithOpt(11, Some("aaa")))
      }

      describe("with None in content") {
        shoudBeReadAndConvertedFromDBObject(dbObject("lon" -> 56), ObjWithOpt(56, None))
      }

    }

  }

  describe("Recursive object") {

    describe("with specialized writing") {

      implicit object canReadAaa extends CanRead[RecursiveObj, MapReader[MongoReader]] {

        def read(from: MapReader[MongoReader]) =
          RecursiveObj(readField("a", from))

      }

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> dbObject()), RecursiveObj(Some(RecursiveObj(None))))

    }

  }

}
