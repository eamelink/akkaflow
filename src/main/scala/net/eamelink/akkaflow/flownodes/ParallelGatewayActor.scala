package net.eamelink.akkaflow.flownodes

import scala.collection.JavaConverters.asScalaBufferConverter

import org.activiti.bpmn.model.ParallelGateway

import akka.actor._
import net.eamelink.akkaflow.{ IncomingToken, UnconditionalTokenEmitter }
import net.eamelink.akkaflow.token.Token

class ParallelGatewayActor(val node: ParallelGateway) extends Actor with ActorLogging with UnconditionalTokenEmitter {
  // Map from SequenceFlowRef to tokens
  var tokenBuffers: Map[String, Seq[Token]] =
    node.getIncomingFlows().asScala.map(_.getId -> Nil).toMap

  def receive = {
    case IncomingToken(token, sequenceFlowRef) => {
      log.info("Received a token in Parallel Gateway")
      tokenBuffers += sequenceFlowRef -> (tokenBuffers(sequenceFlowRef) :+ token)

      // Emit tokens if we
      val headTokenOptions = tokenBuffers.map(_._2.headOption)

      if (headTokenOptions forall (_.isDefined)) {
        log.info("We have a token on every incoming sequence flow!")
        val tokens = headTokenOptions.map(_.get)
        tokenBuffers = tokenBuffers mapValues (_.tail)
        emitTokens(tokens.toSeq, sender)
      }
    }
  }

}
