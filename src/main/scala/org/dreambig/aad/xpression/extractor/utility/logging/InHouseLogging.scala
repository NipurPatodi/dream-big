package org.dreambig.aad.xpression.extractor.utility.logging

import org.slf4s.LoggerFactory

/**
  * Created by npatodi030716 on 9/28/2016.
  */
trait InHouseLogging extends Serializable  {
   @transient protected lazy val log = LoggerFactory.getLogger(getClass.getName)
}
