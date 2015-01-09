package auction.firstPrice;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * Created by Owner on 2015/01/07.
 */
public class Auctioneer extends Agent {
    private static final String PRODUCT_NAME = "something";
    private AID[] bidderAgents;
    private int price = 0;
    private MessageTemplate mt; // The template to receive replies
    private int bestPrice = 0;
    private int repliesCnt = 0; // The counter of replies from seller agents
    private AID bestBidder; // The agent who provides the best offer

    public void setup() {
        System.out.println("Hallo! Auctioneer-agent "+getAID().getName()+" is ready.");
        System.out.println("Product name " + PRODUCT_NAME + " is for sale.");

        addBehaviour(new TickerBehaviour(this, 20000) {
            @Override
            protected void onTick() {

                //Bidder一覧を取得
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("bidder");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    System.out.println("Found the following bidder agents");
                    bidderAgents = new AID[result.length];
                    for(int i = 0; i < result.length; ++i) {
                        bidderAgents[i] = result[i].getName();
                        System.out.println(bidderAgents[i].getName());
                    }
                } catch(FIPAException fe) {
                    fe.printStackTrace();
                }

                myAgent.addBehaviour(new RequestPerformer());
            }
        });

    }

    public void takeDown() {
        System.out.println("Buyer-agent "+getAID().getName()+" terminating.");
    }

    public class RequestPerformer extends Behaviour {
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    //オークションの商品情報をBidderに通知(step 0)
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < bidderAgents.length; ++i) {
                        cfp.addReceiver(bidderAgents[i]);
                    }
                    cfp.setContent(Integer.toString(price));
                    cfp.setConversationId("Auction");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Auction"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                break;

                case 1:
                    //Bidderからの価格情報を受信
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        System.out.println("Reply received.");
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            int price = Integer.parseInt(reply.getContent());
                            if (bestBidder == null || price > bestPrice) {
                                // This is the best offer at present
                                bestPrice = price;
                                bestBidder = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= bidderAgents.length) {
                            System.out.println(bestBidder + ": " + bestPrice);
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return ((step == 2 && bestBidder == null) || step == 4);
        }
    }
}
