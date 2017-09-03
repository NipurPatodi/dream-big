package org.dreambig.aad.xpression.bean

/**
  * Created by npatodi030716 on 3/2/2017.
  */


case class SentimentIdx( xpression:String,rate:Int) extends Serializable{
  var upset:Int =rate match{ case 0 =>1 case _ =>0}
  var sad:Int =rate match{ case 1 =>1 case _ =>0}
  var neutral:Int =rate match{ case 2 =>1 case _ =>0}
  var happy:Int =rate match{ case 3 =>1 case _ =>0}
  var glad:Int =rate match{ case 4 =>1 case _ =>0}
}

object SentimentAggUtil {

  def apply(idx1: SentimentIdx, idx2:SentimentIdx):SentimentIdx={
    idx1.happy+=idx2.happy
    idx1.glad+=idx2.glad
    idx1.sad+=idx2.sad
    idx1.upset+=idx2.upset
    idx1.neutral+=idx2.neutral
    idx1
  }


}