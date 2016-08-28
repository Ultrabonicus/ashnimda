package ashnimda.actors.wsSupervisor

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import collection.immutable.HashSet
import ashnimda.actors.wsClientConnection.ClientWorker
import ashnimda.actors.wsClientConnection.ClientWorkerMessages._

object WSSupervisorMessages {
  case object WSSupervisorAddClientConnection
  case class WSSupervisorClientWorkerAddedClientConnection(subscriber: ActorRef, originalSender: ActorRef)
}

object WSSupervisor{
  val props = Props[WSSupervisor]
  val name = "WSSupervisor"
}

class WSSupervisor extends Actor with ActorLogging{
  import WSSupervisorMessages._
  
  log.info("Supervisor created")
  
  def workingState(clientWorkers: HashSet[ActorRef]):Receive = {
    case WSSupervisorAddClientConnection => {
      log.info("Supervisor creating new client worker " + sender)
      val newWorker = context.actorOf(ClientWorker.props)
      newWorker ! ClientWorkerStart(sender)
      log.debug(clientWorkers.toString())
      context.become(workingState(clientWorkers + newWorker))
    }
    case WSSupervisorClientWorkerAddedClientConnection(s, os)=>{
      log.info("Supervisor sending adding worker response")
      os ! (sender, s)
    }
    case any => {
      log.warning("Unexpected msg " + any.toString() + " from " + sender )
    }
  }
  def receive = workingState(HashSet.empty)
}