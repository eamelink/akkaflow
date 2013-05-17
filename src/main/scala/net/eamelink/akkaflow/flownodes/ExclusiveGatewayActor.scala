package net.eamelink.akkaflow.flownodes

import org.activiti.bpmn.model.ExclusiveGateway

import akka.actor._
import net.eamelink.akkaflow.{ ExclusiveTokenEmitter, IncomingToken }

class ExclusiveGatewayActor(val node: ExclusiveGateway) extends Actor with ActorLogging with ExclusiveTokenEmitter {
  def receive = {
    case IncomingToken(token, _) => {
      log.info("Received token in Exclusive Gateway")
      emitTokens(Seq(token), sender)
    }
  }
}
