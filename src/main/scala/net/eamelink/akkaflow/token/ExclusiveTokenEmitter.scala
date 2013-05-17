package net.eamelink.akkaflow

import scala.collection.JavaConverters.asScalaBufferConverter

import org.activiti.bpmn.model.{ Gateway, SequenceFlow }

import akka.actor._
import net.eamelink.akkaflow.token.{ Token, TokenEmitter }

/**
 * Emit a token on the first outgoing flow who's condition evaluates
 * to true, or the default condition otherwise.
 */
trait ExclusiveTokenEmitter extends TokenEmitter[Gateway] {
  def emitTokens(existingTokens: Seq[Token], to: ActorRef) = {
    val potentialTargets = node.getOutgoingFlows().asScala

    val target = potentialTargets.find(evaluateCondition(_))
      .orElse(defaultFlow)
      .getOrElse {
        // BOOM!
        throw new RuntimeException("Didn't find a suitable outgoing flow!")
      }

    sendAndDestroyTokens(existingTokens, Seq(target), to)
  }

  def evaluateCondition(flow: SequenceFlow) = flow.getConditionExpression match {
    case null => false // This is an unconditional flow
    case conditionExpression => {
      conditionExpression.toBoolean // TODO
    }
  }

  def defaultFlow = Option(node.getDefaultFlow).flatMap { sequenceFlowRef =>
    node.getOutgoingFlows().asScala.find(_.getId == sequenceFlowRef)
  }

}