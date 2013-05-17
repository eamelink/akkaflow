package net.eamelink.akkaflow.flownodes

import scala.concurrent.duration.DurationInt

import org.scalatest.{ BeforeAndAfter, FunSpec }

import akka.actor._
import akka.pattern.ask
import akka.testkit.TestProbe
import akka.util.Timeout
import net.eamelink.akkaflow.ProcessDefActor
import net.eamelink.akkaflow.flownodes.UserTaskActor.{ TaskCompleted, TaskCreated, TaskEvent }
import net.eamelink.akkaflow.util.ProcessParser

class UserTaskSpec extends FunSpec with BeforeAndAfter {
  implicit val timeout = Timeout(1.seconds)
  implicit var system: ActorSystem = _

  before {
    system = ActorSystem("bpmn")
  }

  after {
    system.shutdown()
  }

  describe("A process with a user task") {

    val process1 = ProcessParser.parseProcess(
      <process id="myProcess" name="My process" isExecutable="true">
        <startEvent id="startevent1" name="Start"></startEvent>
        <endEvent id="endevent1" name="End"></endEvent>
        <sequenceFlow id="flow1" sourceRef="startevent1" targetRef="usertask1"></sequenceFlow>
        <userTask id="usertask1" name="User Task"></userTask>
        <sequenceFlow id="flow2" sourceRef="usertask1" targetRef="endevent1"></sequenceFlow>
      </process>)

    it("sends a TaskCreated event over the EventStream") {
      val probe1 = TestProbe()
      system.eventStream.subscribe(probe1.ref, classOf[UserTaskActor.TaskEvent])

      val processDefActor = system.actorOf(Props(classOf[ProcessDefActor], process1), name = "process1")
      processDefActor ? ProcessDefActor.StartProcess()

      val task = probe1.expectMsgPF(500.millis) { case TaskCreated(task) => task }
    }

    it("sends a TaskCompleted event over the EventStream when the task is completed") {
      val probe1 = TestProbe()
      system.eventStream.subscribe(probe1.ref, classOf[UserTaskActor.TaskEvent])

      val processDefActor = system.actorOf(Props(classOf[ProcessDefActor], process1), name = "process1")
      processDefActor ? ProcessDefActor.StartProcess()

      val task = probe1.expectMsgPF(500.millis) { case TaskCreated(task) => task }

      task.promise.success(())

      val task2 = probe1.expectMsgPF(500.millis) { case TaskCompleted(task) => task }
      assert(task == task2)
    }
  }
}