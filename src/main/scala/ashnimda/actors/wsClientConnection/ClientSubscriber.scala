package ashnimda.actors.wsClientConnection

import akka.actor._
import akka.http.scaladsl.model.ws.Message

object ClientSubscriber{
  val props = Props[ClientSubscriber]
  val name = "ClientWorker"
}

object ClientSubscriberMessages{
  abstract class ClientSubscriberMsg
  
  case object ClientSubscriberStop extends ClientSubscriberMsg
  case class ClientSubscriberMessage(msg: String) extends ClientSubscriberMsg
}

class ClientSubscriber extends Actor with ActorLogging{
  import ClientSubscriberMessages._
  
  val receive:Receive = {
    case x:Message => {
      log.info("MSG IN MSG")
    }
    
    case ClientSubscriberStop => {
      log.info("stopped")
    }
    
    case any =>{
      log.info(any.toString())
      context.parent ! "msg"
    } 
  }
}