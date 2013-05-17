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

class ServiceTaskSpec extends FunSpec with BeforeAndAfter {
  implicit val timeout = Timeout(1.seconds)
  implicit var system: ActorSystem = _

  before {
    system = ActorSystem("bpmn")
  }

  after {
    system.shutdown()
  }

  describe("A process with a service task") {

    val process1 = ProcessParser.parseProcess(
      <process id="myProcess" name="My process" isExecutable="true">
        <startEvent id="startevent1" name="Start"></startEvent>
        <sequenceFlow id="flow1" sourceRef="startevent1" targetRef="servicetask1"></sequenceFlow>
        <serviceTask id="servicetask1" name="Service Task"></serviceTask>
        <sequenceFlow id="flow2" sourceRef="servicetask1" targetRef="endevent1"></sequenceFlow>
 		<endEvent id="endevent1" name="End"></endEvent>
      </process>)

    it("executes the service task") {
      val processDefActor = system.actorOf(Props(classOf[ProcessDefActor], process1), name = "process1")
      val processInstanceRefFuture = processDefActor ? ProcessDefActor.StartProcess()
      // TODO, test this feature for real
      assert(processInstanceRefFuture.isInstanceOf[Future[_]])
    }

  }
}