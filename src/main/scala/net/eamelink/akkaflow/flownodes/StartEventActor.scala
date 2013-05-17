package net.eamelink.akkaflow.flownodes

import org.activiti.bpmn.model.StartEvent

import akka.actor._
import net.eamelink.akkaflow.UnconditionalTokenEmitter
import net.eamelink.akkaflow.flownodes.NodeActor._

class StartEventActor(val node: StartEvent) extends Actor with ActorLogging with UnconditionalTokenEmitter {
  def receive = {
    case Start => emitTokens(Nil, sender)
  }
}
