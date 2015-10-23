import akka.actor.{ Actor, Props, ActorPath, ActorSelection }
import scala.math.BigInt
import scala.util.Random
import com.roundeights.hasher.Implicits._
import ChordUtil._

case object Build
case class Join(guider: NodeInfo)
case object JoinComplete
case object Start
case object Print

class NetworkBuilder(noNodes: Int, noRequests: Int) extends Actor {
    val numNodes = noNodes
    val numRequest = noRequests

    val m = 19
    import scala.collection.mutable.ArrayBuffer
    var nodes= ArrayBuffer.empty[Tuple2[Int, String]]
    var joinedCount = 0

    override def receive = {
        case Build =>
            Peer.setMExponent(m)
            val randomIds = Random.shuffle((0 until (math.pow(2, m).toInt)).toVector)
            for (i <- 0 until numNodes) {
                val id = randomIds(i)
                context.actorOf(Props(classOf[Peer], id, numRequest), s"peer$id")
                nodes += ((id, s"peer$id"))
            }
            val firstPeer = context.actorSelection(nodes(0)._2)
            firstPeer ! Join(NodeInfo(nodes(0)._1, null))
            joinedCount += 1

        case JoinComplete =>
            if (joinedCount < numNodes) {
                // report build progress
                if (joinedCount % 1000 == 0) {
                    println(s"$joinedCount peers joined")
                }
                val random = Random.nextInt(joinedCount)
                val peer = context.actorSelection(nodes(joinedCount)._2)
                val path = ActorPath.fromString(self.path.toString + "/" + nodes(random)._2)
                peer ! Join(NodeInfo(nodes(random)._1, path))
                joinedCount += 1
            } else {
                println(s"Build $numNodes peers complete")
                // all nodes start to send request
                context.children foreach { _ ! Start }
            }

        case _ =>
    }
}


