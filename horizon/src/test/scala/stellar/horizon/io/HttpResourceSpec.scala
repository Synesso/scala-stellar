package stellar.horizon.io

import org.specs2.mutable.Specification
import stellar.horizon.AccountDetail
import stellar.protocol.{AccountId, Key}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

class HttpResourceSpec extends Specification {

  "http resource for account details" should {
    "parse successfully from testnet" >> {
      val f = new HttpResource().accountDetail(AccountId("GCZ74YQTSM4KNSA723R2BO5BJM2WH54ZEKGOWX2QJIQ6FMETKQXTYBWF"))
      val detail = Await.result(f, 10.seconds)
      println(detail)
      ok
    }
  }

}
