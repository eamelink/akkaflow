package net.eamelink.akkaflow

import scala.collection.JavaConverters.collectionAsScalaIterableConverter
import org.activiti.bpmn.model.{ FlowNode, Process, SequenceFlow, StartEvent }
import akka.actor.{ Actor, ActorLogging, ActorRef, actorRef2Scala }
import net.eamelink.akkaflow.flownodes.NodeActor
import net.eamelink.akkaflow.flownodes.NodeActor._
import net.eamelink.akkaflow.token.Token
import akka.agent.Agent
import net.eamelink.akkaflow.ProcessDefActor.StartProcess

object ProcessInstanceActor {
  case object GetVariables
}

class ProcessInstanceActor(processInstanceId: String, process: Process) extends Actor with ActorLogging {
  import ProcessInstanceActor._

  /**
   * Map from tokens to the id of the flownode where the token
   * currently is.
   */
  var tokens: Map[Token, String] = Map()

  var variables: Map[String, Any] = Map()

  val flowNodeActors: Map[String, ActorRef] =
    process.getFlowElements().asScala.collect {
      case node: FlowNode => node.getId -> NodeActor(node, processInstanceId)
    }.toMap
    
  def receive = {
    case StartProcess(variables) => {
      this.variables = variables
      startNode foreach { startNode =>
        val actor = flowNodeActors(startNode.getId)
        context.system.eventStream.publish(ProcessDefActor.ProcessStarted(self))
        actor ! Start
      }
    }
    case OutgoingToken(token, sequenceFlowRef) => {
      val sequenceFlow = process.getFlowElement(sequenceFlowRef).asInstanceOf[SequenceFlow]
      val targetNode = sequenceFlow.getTargetRef
      val target = flowNodeActors(targetNode)
      tokens += token -> targetNode
      target ! IncomingToken(token, sequenceFlowRef)
    }
    case CreateToken(sequenceFlowRef) => {
      val sequenceFlow = process.getFlowElement(sequenceFlowRef).asInstanceOf[SequenceFlow]
      val targetNode = sequenceFlow.getTargetRef
      val target = flowNodeActors(targetNode)
      val token = createToken()
      tokens += token -> targetNode
      target ! IncomingToken(token, sequenceFlowRef)
    }
    case DestroyToken(token) => {
      tokens -= token
      if (tokens.isEmpty) {
        context.system.eventStream.publish(ProcessDefActor.ProcessFinished(self, variables))
        context.stop(self)
      }
    }

    // TODO, generalize to 'CompleteAsyncTask'?
    case m @ CompleteUserTask(nodeRef) => {
      val target = flowNodeActors(nodeRef)
      target ! m
    }

    case GetVariables => sender ! variables
  }

  def startNode = process.getFlowElements().asScala.find(_.isInstanceOf[StartEvent])

  def createToken() = Token(generateTokenId)

  var nextTokenId = 0
  def generateTokenId = {
    val id = nextTokenId
    nextTokenId += 1
    id.toString
  }
}
