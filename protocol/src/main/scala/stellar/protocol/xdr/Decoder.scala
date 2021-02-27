package stellar.protocol.xdr

import java.io.EOFException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.Instant

import cats.Eval
import cats.data.{IndexedStateT, State}
import com.typesafe.scalalogging.LazyLogging
import okio.ByteString

import scala.util.Try

trait Decoder[U] extends LazyLogging {

  val decodeOld: State[Seq[Byte], U]

  private def decodeRaw[T](bs: Seq[Byte], len: Int)(decoder: Seq[Byte] => T): (Seq[Byte], T) = {
    if (bs.length < len) throw new EOFException("Insufficient data remains to parse.")
    val t = decoder(bs.take(len))
    logger.trace(s"Dropping {} to make {}", len, t)
    bs.drop(len) -> t
  }

  val int: State[Seq[Byte], Int] = State[Seq[Byte], Int] { bs =>
    decodeRaw(bs, 4) { in => ByteBuffer.wrap(in.toArray).getInt }
  }

  val long: State[Seq[Byte], Long] = State[Seq[Byte], Long] { bs =>
    decodeRaw(bs, 8) { in => ByteBuffer.wrap(in.toArray).getLong }
  }

  val instant: State[Seq[Byte], Instant] = long.map(Instant.ofEpochSecond)

  val bool: State[Seq[Byte], Boolean] = int.map(_ == 1)

  def bytes(len: Int): State[Seq[Byte], List[Byte]] = State[Seq[Byte], List[Byte]] { bs =>
    decodeRaw(bs, len) { _.take(len).toList }
  }

  def byteString(len: Int): State[Seq[Byte], ByteString] = bytes(len).map(bs => new ByteString(bs.toArray))

  val bytes: State[Seq[Byte], List[Byte]] = for {
    len <- int
    bs <- bytes(len)
  } yield bs

  def padded(multipleOf: Int = 4): State[Seq[Byte], Seq[Byte]] = for {
    len <- int
    bs <- bytes(len)
    _ <- bytes((multipleOf - (len % multipleOf)) % multipleOf)
  } yield bs

  val string: State[Seq[Byte], String] = padded().map(_.toArray).map(new String(_, StandardCharsets.UTF_8))

  def switch[T](zero: State[Seq[Byte], T], others: State[Seq[Byte], T]*): IndexedStateT[Eval, Seq[Byte], Seq[Byte], T] =
    switchInt(zero, others: _*).map(_._1)

  def switchInt[T](zero: State[Seq[Byte], T], others: State[Seq[Byte], T]*): State[Seq[Byte], (T, Int)] = int.flatMap {
    case 0 => zero.map(_ -> 0)
    case n => Try(others(n - 1).map(_ -> n)).getOrElse {
      throw new IllegalArgumentException(s"No parser defined for discriminant $n")
    }
  }

  def opt[T](parseT: State[Seq[Byte], T]): State[Seq[Byte], Option[T]] = bool.flatMap {
    case true => parseT.map(Some(_))
    case false => State.pure(None)
  }

  def arr[T](parseT: State[Seq[Byte], T]): State[Seq[Byte], List[T]] = int.flatMap(list(_, parseT))

  // $COVERAGE-OFF$
  // For debugging XDR only.
  def log[T](t: T): State[Seq[Byte], Unit] = State[Seq[Byte], Unit] { bs =>
    logger.debug("{}\n", t)
    bs -> ()
  }
  // $COVERAGE-ON$

  def list[T](qty: Int, parseT: State[Seq[Byte], T]): State[Seq[Byte], List[T]] = {
    (0 until qty).foldLeft(State.pure[Seq[Byte], List[T]](List.empty[T])) { case (state, _) =>
      for {
        ts <- state
        t <- parseT
      } yield ts :+ t
    }
  }

  def drop[T](parse: State[Seq[Byte], _])(t: T): State[Seq[Byte], T] = for {
    _ <- parse
  } yield t

  def widen[A, W, O <: W](s: State[A, O]): State[A, W] = s.map(w => w: W)
}
