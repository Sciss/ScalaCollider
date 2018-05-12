/*
 *  GE.scala
 *  (ScalaCollider)
 *
 *  Copyright (c) 2008-2018 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.synth

import de.sciss.synth.ugen.{BinaryOpUGen, ChannelProxy, ChannelRangeProxy, Clip, Constant, Flatten, Fold, Impulse, LinExp, LinLin, MulAdd, Poll, UnaryOpUGen, Wrap}

object GEOps {
  private def getRate(g: GE, name: String): Rate =
    g.rate.getOrElse(throw new UnsupportedOperationException(s"`$name` input rate must be defined"))
}
final class GEOps(private val g: GE) extends AnyVal {
  import GEOps.getRate

  /** Creates a proxy that represents a specific output channel of the element.
    *
    * @param index  channel-index, zero-based. Indices which are greater than or equal
    *               to the number of outputs are wrapped around.
    *
    * @return a monophonic element that represents the given channel of the receiver
    */
  def out(index: Int): GE = ChannelProxy(g, index)

  def out(range: Range): GE = {
    val until = if (range.isInclusive) range.end + range.step else range.end
    ChannelRangeProxy(g, from = range.start, until = until, step = range.step)
  }

  def mulAdd(mul: GE, add: GE): GE = MulAdd(g, mul, add)

  def flatten: GE = Flatten(g)

  def poll: Poll = poll()

  /** Polls the output values of this graph element, and prints the result to the console.
    * This is a convenient method for wrapping this graph element in a `Poll` UGen.
    *
    * @param   trig     a signal to trigger the printing. If this is a constant, it is
    *                   interpreted as a frequency value and an `Impulse` generator of that frequency
    *                   is used instead.
    * @param   label    a string to print along with the values, in order to identify
    *                   different polls. Using the special label `"$auto"` (default) will generated
    *                   automatic useful labels using information from the polled graph element
    * @param   trigId   if greater then 0, a `"/tr"` OSC message is sent back to the client
    *                   (similar to `SendTrig`)
    *
    * @see  [[de.sciss.synth.ugen.Poll]]
    */
  def poll(trig: GE = 10, label: String = "$auto", trigId: GE = -1): Poll = {
    val trig1 = trig match {
      case Constant(freq) => Impulse((g.rate getOrElse audio) max control, freq, 0) // XXX good? or throw an error? should have a maxRate?
      case other          => other
    }
    val label1 = if (label != "$auto") label else {
      val str = g.toString
      val i   = str.indexOf('(')
      if (i >= 0) str.substring(0, i)
      else {
        val j = str.indexOf('@')
        if (j >= 0) str.substring(0, j)
        else str
      }
    }
    Poll(trig1.rate, trig = trig1, in = g, label = label1, trigId = trigId)
  }

  import UnaryOpUGen._

  @inline private def unOp(op: UnaryOpUGen.Op): GE = op.make(g)

  // unary ops
  def unary_-   : GE  = unOp(Neg       )
  def unary_!   : GE  = unOp(Not       )
  def unary_~   : GE  = unOp(BitNot    )
  def abs       : GE  = unOp(Abs       )
  // def toFloat: GE = ...
  // def toInteger: GE = ...
  def ceil      : GE  = unOp(Ceil      )
  def floor     : GE  = unOp(Floor     )
  def frac      : GE  = unOp(Frac      )
  def signum    : GE  = unOp(Signum    )
  def squared   : GE  = unOp(Squared   )
  def cubed     : GE  = unOp(Cubed     )
  def sqrt      : GE  = unOp(Sqrt      )
  def exp       : GE  = unOp(Exp       )
  def reciprocal: GE  = unOp(Reciprocal)
  def midiCps   : GE  = unOp(Midicps   )
  def cpsMidi   : GE  = unOp(Cpsmidi   )
  def midiRatio : GE  = unOp(Midiratio )
  def ratioMidi : GE  = unOp(Ratiomidi )
  def dbAmp     : GE  = unOp(Dbamp     )
  def ampDb     : GE  = unOp(Ampdb     )
  def octCps    : GE  = unOp(Octcps    )
  def cpsOct    : GE  = unOp(Cpsoct    )
  def log       : GE  = unOp(Log       )
  def log2      : GE  = unOp(Log2      )
  def log10     : GE  = unOp(Log10     )
  def sin       : GE  = unOp(Sin       )
  def cos       : GE  = unOp(Cos       )
  def tan       : GE  = unOp(Tan       )
  def asin      : GE  = unOp(Asin      )
  def acos      : GE  = unOp(Acos      )
  def atan      : GE  = unOp(Atan      )
  def sinh      : GE  = unOp(Sinh      )
  def cosh      : GE  = unOp(Cosh      )
  def tanh      : GE  = unOp(Tanh      )
  def rand      : GE  = unOp(Rand      )
  def rand2     : GE  = unOp(Rand2     )
  def linRand   : GE  = unOp(Linrand   )
  def bilinRand : GE  = unOp(Bilinrand )
  def sum3Rand  : GE  = unOp(Sum3rand  )
  def distort   : GE  = unOp(Distort   )
  def softClip  : GE  = unOp(Softclip  )
  def coin      : GE  = unOp(Coin      )

  // def even : GE              = UnOp.make( 'even, this )
  // def odd : GE               = UnOp.make( 'odd, this )
 def rectWindow : GE  = unOp(RectWindow )
 def hannWindow : GE  = unOp(HannWindow )
 def welchWindow: GE  = unOp(WelchWindow)
 def triWindow  : GE  = unOp(TriWindow  )
 def ramp       : GE  = unOp(Ramp       )
 def sCurve     : GE  = unOp(Scurve     )

  // def isPositive : GE        = UnOp.make( 'isPositive, this )
  // def isNegative : GE        = UnOp.make( 'isNegative, this )
  // def isStrictlyPositive : GE= UnOp.make( 'isStrictlyPositive, this )
  // def rho : GE               = UnOp.make( 'rho, this )
  // def theta : GE             = UnOp.make( 'theta, this )

  import BinaryOpUGen._

  // binary ops
  @inline private def binOp(op: BinaryOpUGen.Op, b: GE): GE = op.make(g, b)

  def +       (b: GE): GE = binOp(Plus    , b)
  def -       (b: GE): GE = binOp(Minus   , b)
  def *       (b: GE): GE = binOp(Times   , b)
  // def div(b: GE): GE = ...
  def /       (b: GE): GE = binOp(Div     , b)

//  @deprecated("Use 'mod' now, because '%' is misleading, as the server implements modulus different from scala.math.",
//    since = "1.24.0")
  /** An alias for `mod`. */
  def %       (b: GE): GE = binOp(Mod     , b)
  def mod     (b: GE): GE = binOp(Mod     , b)
  def sig_==  (b: GE): GE = binOp(Eq      , b)
  def sig_!=  (b: GE): GE = binOp(Neq     , b)
  def <       (b: GE): GE = binOp(Lt      , b)
  def >       (b: GE): GE = binOp(Gt      , b)
  def <=      (b: GE): GE = binOp(Leq     , b)
  def >=      (b: GE): GE = binOp(Geq     , b)
  def min     (b: GE): GE = binOp(Min     , b)
  def max     (b: GE): GE = binOp(Max     , b)
  def &       (b: GE): GE = binOp(BitAnd  , b)
  def |       (b: GE): GE = binOp(BitOr   , b)
  def ^       (b: GE): GE = binOp(BitXor  , b)
  def lcm     (b: GE): GE = binOp(Lcm     , b)
  def gcd     (b: GE): GE = binOp(Gcd     , b)

  def roundTo (b: GE): GE = binOp(RoundTo , b)
  def roundUpTo (b: GE): GE = binOp(RoundUpTo, b)
  def trunc   (b: GE): GE = binOp(Trunc   , b)
  def atan2   (b: GE): GE = binOp(Atan2   , b)
  def hypot   (b: GE): GE = binOp(Hypot   , b)
  def hypotApx(b: GE): GE = binOp(Hypotx  , b)

  /** '''Warning:''' Unlike a normal power operation, the signum of the
    * left operand is always preserved. I.e. `DC.kr(-0.5).pow(2)` will
    * not output `0.25` but `-0.25`. This is to avoid problems with
    * floating point noise and negative input numbers, so
    * `DC.kr(-0.5).pow(2.001)` does not result in a `NaN`, for example.
    */
  def pow     (b: GE): GE = binOp(Pow     , b)

  def <<      (b: GE): GE = binOp(LeftShift , b)
  def >>      (b: GE): GE = binOp(RightShift, b)

  // def unsgnRghtShift(b: GE): GE = ...
  // def fill(b: GE): GE = ...

  def ring1   (b: GE): GE = binOp(Ring1   , b)
  def ring2   (b: GE): GE = binOp(Ring2   , b)
  def ring3   (b: GE): GE = binOp(Ring3   , b)
  def ring4   (b: GE): GE = binOp(Ring4   , b)
  def difSqr  (b: GE): GE = binOp(Difsqr  , b)
  def sumSqr  (b: GE): GE = binOp(Sumsqr  , b)
  def sqrSum  (b: GE): GE = binOp(Sqrsum  , b)
  def sqrDif  (b: GE): GE = binOp(Sqrdif  , b)
  def absDif  (b: GE): GE = binOp(Absdif  , b)
  def thresh  (b: GE): GE = binOp(Thresh  , b)
  def amClip  (b: GE): GE = binOp(Amclip  , b)
  def scaleNeg(b: GE): GE = binOp(Scaleneg, b)
  def clip2   (b: GE): GE = binOp(Clip2   , b)
  def excess  (b: GE): GE = binOp(Excess  , b)
  def fold2   (b: GE): GE = binOp(Fold2   , b)
  def wrap2   (b: GE): GE = binOp(Wrap2   , b)
  def firstArg(b: GE): GE = binOp(Firstarg, b)

 def rangeRand(b: GE): GE = binOp(Rrand   , b)
 def expRand  (b: GE): GE = binOp(Exprand , b)

  def clip(low: GE, high: GE): GE = {
    val r = getRate(g, "clip")
    if (r == demand) g.max(low).min(high) else Clip(r, g, low, high)
  }

  def fold(low: GE, high: GE): GE = {
    val r = getRate(g, "fold")
    if (r == demand) throw new UnsupportedOperationException("`fold` not supported for demand rate UGens")
    Fold(r, g, low, high)
  }

  def wrap(low: GE, high: GE): GE = {
    val r = getRate(g, "wrap")
    if (r == demand) throw new UnsupportedOperationException("`wrap` not supported for demand rate UGens")
    Wrap(r, g, low, high)
  }

  def linLin(inLow: GE, inHigh: GE, outLow: GE, outHigh: GE): GE = {
    val r = getRate(g, "linLin")
    if (r == demand) {
      (g - inLow) / (inHigh - inLow) * (outHigh - outLow) + outLow
    } else {
      LinLin(/* rate, */ g, inLow, inHigh, outLow, outHigh)
    }
  }

  def linExp(inLow: GE, inHigh: GE, outLow: GE, outHigh: GE): GE = {
    val r = getRate(g, "linExp")
    if (r == demand) {
      (outHigh / outLow).pow((g - inLow) / (inHigh - inLow)) * outLow
    } else {
      LinExp(g.rate, g, inLow, inHigh, outLow, outHigh) // should be highest rate of all inputs? XXX
    }
  }

  def expLin(inLow: GE, inHigh: GE, outLow: GE, outHigh: GE): GE =
    (g / inLow).log / (inHigh / inLow).log * (outHigh - outLow) + outLow

  def expExp(inLow: GE, inHigh: GE, outLow: GE, outHigh: GE): GE =
    (outHigh / outLow).pow((g / inLow).log / (inHigh / inLow).log) * outLow
}