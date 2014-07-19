package net.ikenna.wot

import org.slf4j.LoggerFactory
import akka.event.{ NoLogging, LoggingAdapter }

trait WotLogger {
  val defaultLogger = LoggerFactory.getLogger(this.getClass)
  val akkaLogger: LoggingAdapter = NoLogging

}
