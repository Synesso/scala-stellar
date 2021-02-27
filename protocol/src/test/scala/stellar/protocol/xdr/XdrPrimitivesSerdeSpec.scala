package stellar.protocol.xdr

import java.io.EOFException
import java.nio.charset.Charset
import java.time.Instant

import cats.data.State
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class XdrPrimitivesSerdeSpec extends Specification with ScalaCheck with Decoder[Int] {

  "round trip serialisation" should {
    "work for ints" >> prop { i: Int =>
      val (remainder, result) = int.run(Encode.int(i)).value
      remainder must beEmpty
      result mustEqual i
    }

    "work for longs" >> prop { l: Long =>
      val (remainder, result) = long.run(Encode.long(l)).value
      remainder must beEmpty
      result mustEqual l
    }

    "work for false" >> {
      val (remainder, result) = bool.run(Encode.bool(false)).value
      remainder must beEmpty
      result must beFalse
    }

    "work for true" >> {
      val (remainder, result) = bool.run(Encode.bool(true)).value
      remainder must beEmpty
      result must beTrue
    }

    "work for opaque bytes" >> prop { bs: Array[Byte] =>
      val (remainder, result) = bytes.run(Encode.bytes(bs)).value
      remainder must beEmpty
      result mustEqual bs.toSeq
    }

    "work for opaque bytes with explicit length" >> prop { bs: Array[Byte] =>
      val (remainder, result) = bytes(bs.length).run(Encode.bytes(bs.length, bs)).value
      remainder must beEmpty
      result mustEqual bs.toSeq
    }

    "work for strings" >> prop { s: String =>
      val bytes = Encode.string(s)
      val (remainder, result) = string.run(bytes).value
      bytes.length % 4 mustEqual 0
      remainder must beEmpty
      result mustEqual s
    }

    "work for optional ints" >> prop { i: Option[Int] =>
      val (remainder, result) = opt(int).run(Encode.optInt(i)).value
      remainder must beEmpty
      result mustEqual i
    }

    "work for optional longs" >> prop { l: Option[Long] =>
      val (remainder, result) = opt(long).run(Encode.optLong(l)).value
      remainder must beEmpty
      result mustEqual l
    }

    "work for optional strings" >> prop { s: Option[String] =>
      val (remainder, result) = opt(string).run(Encode.optString(s)).value
      remainder must beEmpty
      result mustEqual s
    }

    "work for instants" >> prop { i: Instant =>
      val (remainder, result) = instant.run(Encode.instant(i)).value
      remainder must beEmpty
      result mustEqual i
    }

    "work for a list of strings" >> prop { xs: List[String] =>
      val (remainder, result) = arr(string).run(Encode.arrString(xs)).value
      remainder must beEmpty
      result mustEqual xs
    }

    "work for a list of encodables" >> prop { xs: List[CompositeThing] =>
      val (remainder, result) = arr(CompositeThing.decodeOld).run(Encode.arr(xs)).value
      remainder must beEmpty
      result mustEqual xs
    }.set(minTestsOk = 5)

    "work for a composite of encodables" >> prop { c: CompositeThing =>
      val (remainder, result) = CompositeThing.decodeOld.run(c.encodeDiscriminated).value
      remainder must beEmpty
      result mustEqual c
    }

    "work for ignoring tail things" >> prop { (a: CompositeThing, b: CompositeThing) =>
      val encoded = a.encode ++ b.encode
      val (remainder, result) = CompositeThing.decodeOld
        .flatMap(drop(CompositeThing.decodeOld))
        .run(encoded).value
      remainder must beEmpty
      result mustEqual a
    }
  }

  "serialising xdr strings" should {
    "not pad with nulls if it is a multiple of 4" >> {
      Encode.string("1234") mustEqual Encode.int(4) ++ "1234".getBytes(Charset.forName("UTF-8"))
      Encode.string("12345678") mustEqual Encode.int(8) ++ "12345678".getBytes(Charset.forName("UTF-8"))
    }

    "pad with nulls if it is not a multiple of 4" >> {
      Encode.string("123") mustEqual Encode.int(3) ++ "123".getBytes(Charset.forName("UTF-8")) :+ 0
      Encode.string("12345") mustEqual Encode.int(5) ++ "12345".getBytes(Charset.forName("UTF-8")) ++ Seq(0, 0, 0)
      Encode.string("123456") mustEqual Encode.int(6) ++ "123456".getBytes(Charset.forName("UTF-8")) ++ Seq(0, 0)
      Encode.string("1234567") mustEqual Encode.int(7) ++ "1234567".getBytes(Charset.forName("UTF-8")) :+ 0
    }
  }

  "decoding" should {
    "fail if there are insufficient bytes for an int" >> {
      int.run(Seq(0, 0, 0)).value must throwA[EOFException]
    }
    "fail if there are insufficient bytes for a long" >> {
      long.run(Seq(0, 0, 0, 0, 0, 0, 0)).value must throwAn[EOFException]
    }
    "fail if there are insufficient bytes given the declared length" >> {
      bytes.run(Seq(0, 0, 0, 4, 1, 1, 1)).value must throwAn[EOFException]
    }
    "fail if the string has insufficient padding bytes" >> {
      string.run(Seq(0, 0, 0, 1, 99, 0)).value must throwAn[EOFException]
    }
    "switch between first of multiple decoders" >> {
      val encoded = Encode.int(0) ++ Encode.string("first")
      switch(string.map(_.reverse), string, string.map(_.length.toString))
        .run(encoded).value._2 mustEqual("tsrif")
    }
    "switch between other of multiple decoders" >> {
      val encoded = Encode.int(2) ++ Encode.string("first")
      switch(string.map(_.reverse), string, string.map(_.length.toString))
        .run(encoded).value._2 mustEqual("5")
    }
    "fail to switch beyond the end of the decoder list" >> {
      val encoded = Encode.int(99) ++ Encode.string("first")
      switch(string.map(_.reverse), string, string.map(_.length.toString))
        .run(encoded).value must throwAn[IllegalArgumentException]
    }

    "widen types correctly" >> {
      trait Foo
      case class Bar(i: Int) extends Foo with Encodable {
        override def encode: LazyList[Byte] = Encode.int(0) ++ Encode.int(i)
      }
      case class Baz(s: String) extends Foo with Encodable {
        override def encode: LazyList[Byte] = Encode.int(1) ++ Encode.string(s)
      }

      val decodeBar: State[Seq[Byte], Bar] = int.map(Bar)
      val decodeBaz: State[Seq[Byte], Baz] = string.map(Baz)
      // without `widen`, this would fail to compile with type mismatch
      val decode: State[Seq[Byte], Foo] = switch(widen(decodeBar), widen(decodeBaz))
      decode.run(Bar(99).encode).value._2 mustEqual Bar(99)
    }
  }

  implicit private val arbCompositeThing: Arbitrary[CompositeThing] = Arbitrary(genCompositeThing)
  private def genCompositeThing: Gen[CompositeThing] = for {
    b <- Gen.oneOf(true, false)
    s <- Gen.identifier
    bs <- Gen.option(Gen.containerOf[List, Byte](Gen.choose(0x00, 0xff).map(_.toByte)))
    ct <- Gen.option(genCompositeThing)
  } yield CompositeThing(b, s, bs, ct)

  case class CompositeThing(b: Boolean, s: String, bs: Option[List[Byte]], next: Option[CompositeThing]) extends Encodable {
    override def encode: LazyList[Byte] = Encode.optBytes(bs) ++ Encode.bool(b) ++ Encode.opt(next) ++ Encode.string(s)
  }

  object CompositeThing extends Decoder[CompositeThing] {
    val decodeOld: State[Seq[Byte], CompositeThing] = for {
      bs <- opt(bytes)
      b <- bool
      next <- opt[CompositeThing](CompositeThing.decodeOld)
      s <- string
    } yield CompositeThing(b, s, bs, next)
  }

  implicit private val arbInstant: Arbitrary[Instant] = Arbitrary(Gen.posNum[Long].map(Instant.ofEpochSecond))
  override val decodeOld: State[Seq[Byte], Int] = int
}
