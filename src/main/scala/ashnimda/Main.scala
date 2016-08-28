package ashnimda

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.stream.scaladsl._
import akka.http.scaladsl.model.ws._
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.stream.FlowShape
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ashnimda.actors.wsSupervisor.WSSupervisor
import ashnimda.actors.wsSupervisor.WSSupervisorMessages._
import akka.stream.OverflowStrategy
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._
import akka.http.scaladsl.model.ws.TextMessage.Strict

trait GenericWSSupervisor extends GenericServices{
  val wsSupervisor:ActorRef
}

trait WSSupervisorActor extends GenericWSSupervisor{
  val wsSupervisor = asystem.actorOf(WSSupervisor.props, WSSupervisor.name)
}

trait WebServer extends WSService{
  
  val route = 
    pathSingleSlash {
      get {
        complete("1")
      }
    } ~
    path("ws") {
      get {
        onSuccess(wsServiceFuture) { x => handleWebSocketMessages(x) }
      }
    }
}

trait GenericServices {
  implicit def asystem: ActorSystem
  
  implicit def ec: ExecutionContext
  
  implicit def mat: Materializer
}

trait BasicServices extends GenericServices {
  implicit val asystem = ActorSystem("MySystem")
	
  implicit val mat: Materializer = ActorMaterializer()
  
  implicit val ec = mat.executionContext
}

trait WSService extends WSConnectionProvider with GenericServices{
  def wsServiceFuture:Future[Flow[Message,Message,Any]] = 
    createWSConnection
}

trait WSConnectionProvider extends GenericWSSupervisor with GenericServices{
  
  def createWSActors(wsSupervisor: ActorRef):Future[(ActorRef, ActorRef)]={
    implicit val timeout = Timeout(10.seconds)
    val future = wsSupervisor ? WSSupervisorAddClientConnection
    future.mapTo[(ActorRef, ActorRef)]
  }
  
  import scalaz.{\/,\/-,-\/}
  import ashnimda.actors.wsClientConnection.ClientSubscriberMessages
  import ashnimda.actors.wsClientConnection.ClientPublisher
  
  def createWSConnection(implicit asystem: ActorSystem, materializer: Materializer):Future[Flow[Message,Message,Any]] = {
    createWSActors(wsSupervisor).map(wsActors=>{
      println(wsActors._1, wsActors._2)
      val wsFlow:Flow[Message,Message,Any] = Flow.fromGraph(GraphDSL.create() { implicit b =>
      	import GraphDSL.Implicits._
      	val extractMsgFlow = Flow[Message]
          .map {
          case Strict(txt) => txt
          case streamed: TextMessage => "nope"
          case bm: BinaryMessage => "nope2"
        }.map(x=>{
          println(x.toString()) 
          x
        })
      	val extractFlow = b.add(extractMsgFlow)
      	val actorSink = b.add(Sink.actorRef(wsActors._2, ClientSubscriberMessages.ClientSubscriberStop))
      	val actorSource = b.add(Source.actorPublisher(ClientPublisher.props(wsActors._1)))
  	    
      	extractFlow ~> actorSink
      	FlowShape(extractFlow.in, actorSource.out)
      })
      wsFlow
    })
  }
  
}

object Main extends App with BasicServices with WSSupervisorActor with WebServer {
    
  Http().bindAndHandle(route, "localhost", 8080)
  
}
