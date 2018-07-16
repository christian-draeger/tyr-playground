package demo

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.BrokerService
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.jms.Session

class X1SendingMessages {

    val broker = BrokerService().apply {
        isPersistent = false
    }

    val connectionFactory = ActiveMQConnectionFactory()

    @Before
    fun setUp() {
        broker.addConnector("tcp://localhost:61616")
        broker.start()
    }

    @After
    fun tearDown() {
        broker.stop()
    }

    @Test
    fun canSendMessages() {
        sendMessage("HelloQueue", "Hello World")
    }

    fun sendMessage(queue: String, text: String) {
        val connection = connectionFactory.createConnection()

        connection.start()

        // Create a Session
        val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

        // Create the destination (Topic or Queue)
        val destination = session.createQueue(queue)

        // Create a MessageProducer from the Session to the Topic or Queue
        val producer = session.createProducer(destination)

        // Create a messages
        val message = session.createTextMessage(text)

        // Tell the producer to send the message
        println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().name)
        producer.send(message)

        // Clean up
        session.close()
        connection.close()
    }
}