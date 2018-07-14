package demo

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.BrokerService
import org.awaitility.Awaitility
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import javax.jms.*

class X3MessagesAreRetried {

    val broker = BrokerService().apply {
        isPersistent = false
    }

    val connectionFactory = ActiveMQConnectionFactory("vm://localhost")

    @Before
    fun setUp() {
        broker.start()
    }

    @After
    fun tearDown() {
        broker.stop()
    }

    @Test
    fun messagesAreRetried() {

        val results = Collections.synchronizedList(ArrayList<String>())

        registerFlakyListener("HelloQueue", results)

        sendMessage("HelloQueue", "Hello World")

        Awaitility.await().until {
            results.size == 1
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

    fun registerFlakyListener(queue: String, results: MutableList<String>) {
        val connection = connectionFactory.createConnection()
        connection.start()

        // Create a Session
        val session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE)

        // Create the destination (Topic or Queue)
        val destination = session.createQueue(queue)

        // Create a MessageConsumer from the Session to the Topic or Queue
        val consumer = session.createConsumer(destination)

        var tryCount = 0

        consumer.messageListener = MessageListener {
            val tm = it as TextMessage

            it.acknowledge()

            tryCount++
            if (tryCount < 3) throw RuntimeException("temp failure")

            results.add(tm.text)
        }
    }
}