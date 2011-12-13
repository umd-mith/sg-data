/* Usage:
 *   scala scripts/signed-urls.scala <Access Key ID> <Secret Key> data/images
 */

import javax.crypto.spec.{ SecretKeySpec => SKS }
import org.apache.commons.codec.binary.{ Base64 => B64 }

implicit def toFile(s: String) = new java.io.File(s)
implicit def toDate(s: String) = new java.text.SimpleDateFormat().parse(s)
implicit def toEpoch(d: java.util.Date) = (d.getTime / 1000).toString

DirectorySigner(
  Key(args(0), args(1)),
  Loc("mith-sga", "/incoming/mh/"),
  "01/01/2015 12:00 AM"
).apply(new java.io.File(args(2), "mh")).foreach {
  case (f, s) => println("- [%s](%s)".format(f, s))
}

DirectorySigner(
  Key(args(0), args(1)),
  Loc("mith-sga", "/incoming/ox/"),
  "01/01/2015 12:00 AM"
).apply(new java.io.File(args(2), "ox")).foreach {
  case (f, s) => println("- [%s](%s)".format(f, s))
}

case class Params(ps: (String, String)*) {
  override def toString = "?" + this.ps.map {
    case (k, v) => "%s=%s".format(k, java.net.URLEncoder.encode(v, "UTF-8"))
  }.mkString("&")
}

case class Key(id: String, secret: String)
case class Loc(bucket: String, base: String) {
  override def toString = bucket + base
}

trait Signer {
  def key: Key
  private val m = javax.crypto.Mac.getInstance("HmacSHA1")
  this.m.init(new SKS(this.key.secret.getBytes("UTF-8"), this.m.getAlgorithm))
  protected def sign(message: String) = new String(B64.encodeBase64(
    this.m.doFinal(message.getBytes("UTF-8"))
  ))
}

case class DirectorySigner(
  key: Key,
  loc: Loc,
  expires: java.util.Date
) extends Signer {
  val req = "GET\n\n\n%d\n/%s".format(this.expires.getTime / 1000, this.loc)
  def apply(dir: java.io.File) = for {
    f <- dir.listFiles.sorted
    if f.isFile && f.getName.endsWith("tif")
  } yield (
    f.getName, "https://s3.amazonaws.com/" + this.loc + f.getName + Params(
      "AWSAccessKeyId" -> this.key.id,
      "Expires" -> this.expires,
      "Signature" -> this.sign(
        "GET\n\n\n%s\n/%s%s".format(this.expires: String, this.loc, f.getName)
      )
    )
  ) 
}

