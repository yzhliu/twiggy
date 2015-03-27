package me.yzhi.twiggy.system

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
 */
class Postoffice private {
  val yellowPages: YellowPages = _
  var app: AppContainer = _
  var appConf: String = _

  val system = ActorSystem("Postoffice")
  var receiver: ActorRef = _
  var sender: ActorRef = _

  def start(args: Array[String]) = {
    yellowPages.init()
    yellowPages.van.myNode match {
      // FIXME
      case Node.SCHEDULER =>
        app = AppContainer.create(args)
        // TODO
        assert(app != null)
      case _ =>
        // connect to the scheduler, which will send back a create_app request
        val task = new Task(opt=Task.MANAGE, request=true, time=0)
        val msg = new Message(task)
        msg.recver = yellowPages.van.scheduler.id
        send(msg)
    }

    receiver = system.actorOf(Props(new Receiver))
    sender = system.actorOf(Props(new Sender))

    yellowPages.van.myNode match {
      // FIXME
      case Node.SCHEDULER =>
        // add my node into app_
        // TODO: time = 0 ?
        val task = new Task(opt=Task.MANAGE, request=true, time=0)
        task.customer = app.name
        task.mngNode = new ManageNode(ManageNode.ADD)
        // TODO: *task.mutable_mng_node()->add_node() = myNode();
        manageNode(task)
        // TODO: init other nodes
      case _ =>
        // TODO
    }
  }

  def manageNode(tk: Task): Unit = {
    // CHECK(tk.has_mng_node());
    val mng = tk.mngNode
    mng.cmd match {
      case ManageNode.CONNECT =>
        // FIXME:
        require(yellowPages.van.myNode == Node.SCHEDULER)
        // TODO: CHECK_EQ(mng.node_size(), 1);
        // first add this node into app
        val add = tk
        require(app.name != null)
        add.customer = app.name
        val mngNode = new ManageNode(ManageNode.ADD)
        add.mngNode = mngNode
        manageNode(add)
        // create the app in this node
        val task = new Task(request=true, opt=Task.MANAGE, time=1)
        task.customer = app.name
        task.mngApp = new ManageApp(ManageApp.ADD)
        task.mngApp.conf = appConf
        //TODO: app_->port(mng.node(0).id())->submit(task);
        // check if all nodes are connected
        /*
        if (yellowPages.num_workers() >= FLAGS_num_workers &&
          yp().num_servers() >= FLAGS_num_servers) {
          nodes_are_ready_.set_value();
        }
        tk.customer = app.name // otherwise the remote node doesn't know
        */
        // how to find the according customer
      case ManageNode.ADD | ManageNode.UPDATE =>
        /* TODO
        auto obj = yp().customer(tk.customer());
        CHECK(obj) << "customer [" << tk.customer() << "] doesn't exists";
        for (int i = 0; i < mng.node_size(); ++i) {
          auto node = mng.node(i);
        yp().addNode(node);
        obj->exec().add(node);
        for (auto c : yp().childern(obj->name())) {
        auto child = yp().customer(c);
        if (child) child->exec().add(node);
        */
      case ManageNode.REPLACE =>
      case ManageNode.REMOVE =>
        // do nothing
    }
  }

  def send(msg: Message) {
    if (msg.valid && !msg.terminate) {
      val stat = yellowPages.van.send(msg, 0)
      for (s <- stat if s.ok) {
        // heartbeat_info_.increaseOutBytes(send_bytes);
        return
      }
      // TODO: log failure
    } else {
      // TODO: do not send, fake a reply mesage
    }
  }

  class Receiver extends Actor {
    // TODO
    override def receive: Receive = ???
  }

  class Sender extends Actor {
    // TODO
    override def receive: Receive = ???
  }
}

object Postoffice {
  val instance = new Postoffice()
}
