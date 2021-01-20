package org.jboss.as.quickstarts.servlet;

import javax.inject.Inject;
import javax.jms.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/Servlet1")
public class Servlet1 extends HttpServlet {

    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory") // The intended connection factory
    private JMSContext context;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JMSProducer jmsProducer = null;
        try {
            Destination adminQueue = context.createQueue("testQueue");

            //Setup a message producer to send message to the queue the server is consuming from
            jmsProducer = context.createProducer();

            //Create a temporary queue that this client will listen for responses on then create a consumer
            //that consumes message from this temporary queue...for a real application a client should reuse
            //the same temp queue for each message to the server...one temp queue per client
            Destination tempDest = context.createTemporaryQueue();

            //Now create the actual message you want to send
            TextMessage txtMessage = context.createTextMessage();
            txtMessage.setText("MyProtocolMessage");

            //Set the reply to field to the temp queue you created above, this is the queue the server
            //will respond to
            txtMessage.setJMSReplyTo(tempDest);

            //Set a correlation ID so when you get a response you know which sent message the response is for
            //If there is never more than one outstanding message to the server then the
            //same correlation ID can be used for all the messages...if there is more than one outstanding
            //message to the server you would presumably want to associate the correlation ID with this
            //message somehow...a Map works good
            String correlationId = UUID.randomUUID().toString();
            txtMessage.setJMSCorrelationID(correlationId);
            jmsProducer.send(adminQueue,txtMessage);

            JMSConsumer replyConsumer = context.createConsumer(tempDest);

            Message replyMessage = replyConsumer.receive(5000);

            System.out.println(">> Received reply message "+((TextMessage)replyMessage).getText());


        }catch (Exception e){
            e.printStackTrace();
        }
        }
    }

