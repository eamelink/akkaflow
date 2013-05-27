package net.eamelink.akkaflow.flownodes

import org.activiti.bpmn.model.ServiceTask

import akka.actor._
import net.eamelink.akkaflow.{ IncomingToken, UnconditionalTokenEmitter }
import scala.collection.JavaConverters._

class ServiceTaskActor(val node: ServiceTask) extends Actor with ActorLogging with UnconditionalTokenEmitter {
  assert(node.getImplementationType == "class", s"Service task implementation type must be 'class', but is '${node.getImplementationType}'")
  val className = node.getImplementation
  
  val delegate = Class.forName(className).getField("MODULE$").get(null).asInstanceOf[Function0[Unit]]
  def receive = {
    case IncomingToken(token, _) => {
      delegate()
      emitTokens(Seq(token), sender)
    }
  }
}
