package net.eamelink.akkaflow.flownodes

import scala.concurrent.duration.DurationInt
import org.scalatest.{ BeforeAndAfter, FunSpec }
import akka.actor._
import akka.pattern.ask
import akka.testkit.TestProbe
import akka.util.Timeout
import net.eamelink.akkaflow.ProcessDefActor
import net.eamelink.akkaflow.util.ProcessParser
import scala.concurrent.Await
import scala.concurrent.Future
import akka.testkit.TestKit

object TestDelegate extends Function0[Unit] {
  var executions = 0
  override def apply() {
    executions += 1
  }
}

class ServiceTaskSpec(_system: ActorSystem) extends TestKit(_system) with FunSpec with BeforeAndAfter {
  implicit val timeout = Timeout(1.seconds)
  
  def this() = this(ActorSystem("bpmn"))

  describe("A process with a service task") {

    val className = TestDelegate.getClass.getName()
    val xml = 
      <process id="myProcess" name="My process" isExecutable="true">
        <startEvent id="startevent1" name="Start"></startEvent>
        <sequenceFlow id="flow1" sourceRef="startevent1" targetRef="servicetask1"></sequenceFlow>
        <serviceTask id="servicetask1" name="Service Task" activiti:class={className}></serviceTask>
        <sequenceFlow id="flow2" sourceRef="servicetask1" targetRef="endevent1"></sequenceFlow>
 		<endEvent id="endevent1" name="End"></endEvent>
      </process>
    
    val process1 = ProcessParser.parseProcess(
      xml)

    it("executes the service task") {
      val processDefActor = system.actorOf(Props(classOf[ProcessDefActor], process1), name = "process1")
      val processInstanceRefFuture = processDefActor ? ProcessDefActor.StartProcess()
      Await.ready(processInstanceRefFuture, 500.millis)
      
      awaitAssert(assert(TestDelegate.executions === 1))
    }

  }
}