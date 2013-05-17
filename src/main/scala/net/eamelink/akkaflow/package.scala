package net.eamelink

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.concurrent.duration.DurationInt
import scala.util.Try
import org.activiti.bpmn.model.{ BpmnModel, EndEvent, FlowNode, Process, SequenceFlow, ServiceTask, StartEvent, UserTask }
import akka.actor.{ ActorRef, ActorSystem, Props, actorRef2Scala }
import akka.agent.Agent
import akka.pattern.ask
import akka.util.Timeout
import org.activiti.bpmn.model.SequenceFlow
import org.activiti.bpmn.model.Gateway
import net.eamelink.akkaflow.token.Token

package object akkaflow {
  
  case class Task(processInstanceId: String, nodeId: String, promise: Promise[Unit])
  case class CompleteUserTask(nodeRef: String)
  case class Continue()

  case class IncomingToken(token: Token, sequenceFlowRef: String)
  case class OutgoingToken(token: Token, sequenceFlowRef: String)
  case class CreateToken(sequenceFlowRef: String)
  case class DestroyToken(token: Token)

}




