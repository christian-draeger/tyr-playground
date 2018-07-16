package demo

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.BrokerService
import org.awaitility.Awaitility
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.jms.Session
import javax.jms.TextMessage
class X2CanReceiveMessages {

    private var receivedMessage = ""

    val broker = BrokerService().apply {
        isPersistent = false
        addConnector("tcp://localhost:61616")
    }

    val connectionFactory = ActiveMQConnectionFactory()

    @Before
    fun setUp() {
        broker.start()
    }

    @After
    fun tearDown() {
        broker.stop()
    }

    @Test
    fun canPollMessages() {
        sendMessage("HelloQueue", "Hello World")

        Awaitility.await().until {
            pollMessage("HelloQueue") == "Hello World"
        }
    }

    @Test
    fun canListenForMessages() {
        sendMessage("HelloQueue2", "Hello World")

        registerListener("HelloQueue2")

        Awaitility.await().until {
            receivedMessage.equals("Hello World")
        }
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

    fun pollMessage(queue: String): String? {
        val connection = connectionFactory.createConnection()
        connection.start()

        try {
            // Create a Session
            val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

            // Create the destination (Topic or Queue)
            val destination = session.createQueue(queue)

            // Create a MessageConsumer from the Session to the Topic or Queue
            val consumer = session.createConsumer(destination)

            val message = consumer.receive(1000)

            return message?.let {
                val textMessage = it as TextMessage

                println("Got message $textMessage")

                textMessage.text
            }

        } finally {
            connection.close()
        }
    }

    fun registerListener(queue: String) {
        val connection = connectionFactory.createConnection()
        connection.start()

        // Create a Session
        val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

        // Create the destination (Topic or Queue)
        val destination = session.createQueue(queue)

        // Create a MessageConsumer from the Session to the Topic or Queue
        val consumer = session.createConsumer(destination)

        consumer.setMessageListener {
            receivedMessage = (it as TextMessage).text
        }
    }
}