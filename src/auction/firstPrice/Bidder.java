package auction.firstPrice;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Created by Owner on 2015/01/07.
 */
public class Bidder extends Agent {
    private int fee = 30;
    private static final int MAX_PRICE = 1000;
    private int price = 0;

    public void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("bidder");
        sd.setName("JADE-auction");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new OfferPriceServer());
    }

    private class OfferPriceServer extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println("I received price information from " + msg.getSender().getName());
                System.out.println("Current price is " + msg.getContent());
                price = Integer.parseInt(msg.getContent());
                ACLMessage reply = msg.createReply();

                if (price + fee < MAX_PRICE) {
                    price += fee;
                    reply.setPerformative(ACLMessage.PROPOSE);
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                reply.setContent(Integer.toString(price));
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Buyer-agent "+getAID().getName()+" terminating.");
    }
}
