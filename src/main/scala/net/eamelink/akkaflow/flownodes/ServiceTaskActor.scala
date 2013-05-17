package net.eamelink.akkaflow.flownodes

import org.activiti.bpmn.model.ServiceTask

import akka.actor._
import net.eamelink.akkaflow.{ IncomingToken, UnconditionalTokenEmitter }

class ServiceTaskActor(val node: ServiceTask) extends Actor with ActorLogging with UnconditionalTokenEmitter {
  def receive = {
    case IncomingToken(token, _) => {
      println("Running service task")
      emitTokens(Seq(token), sender)
    }
  }
}
