package ashnimda.actors.wsClientConnection

import akka.actor._

object ClientWorkerMessages{
  abstract class ClientWorkerMessage
  
  case object ClientWorkerCreatedPublisher
  case class ClientWorkerStart(originalSender: ActorRef)
}

object ClientWorker{
  val props = Props[ClientWorker]
  val name = "ClientWorker"
}

class ClientWorker extends Actor{
  import ClientWorkerMessages._
  import ashnimda.actors.wsSupervisor.WSSupervisorMessages._
  import ClientPublisherMessages._
  
  val init: Receive = {
    case ClientWorkerStart(os) => {
      val subscriberActor = context.actorOf(ClientSubscriber.props, ClientSubscriber.name)
      sender ! WSSupervisorClientWorkerAddedClientConnection(subscriberActor ,os)
      context.become(createdSubscriber(subscriberActor))
    }
  }
  
  def createdSubscriber(subscriber: ActorRef): Receive = {
    case ClientWorkerCreatedPublisher => context.become(clientWorkerConnected(subscriber, sender))
  }
  
  def clientWorkerConnected(subscriber: ActorRef, publisher: ActorRef): Receive = {
    case any:String => publisher ! WSPublish(any)
  }
  def receive:Receive = init
}