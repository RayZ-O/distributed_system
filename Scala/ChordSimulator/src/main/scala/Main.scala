import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Inbox

object Main {
    def main(args: Array[String]): Unit = {
        val system = ActorSystem("ChordSystem")
        if (args.length < 2) {
            println("usage: sbt \"run [num of peers][num of request][join method(optional)]\"")
        }
        val numOfPeers = args(0).toInt
        val numOfReqs = args(1).toInt
        val mode = if(args.length == 3) args(2) else "normal"

        val netBuilder = system.actorOf(Props(classOf[NetworkBuilder], numOfPeers, numOfReqs), "networkbuilder")
        val hopCounter = system.actorOf(Props(classOf[HopCounter], numOfPeers), "hopcounter")

        val inbox = Inbox.create(system)
        inbox.send(netBuilder, Build(mode))
    }
}
