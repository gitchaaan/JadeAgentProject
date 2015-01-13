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
    private static final String PRODUCT_NAME = "Apple";
    private AID[] bidderAgents;
    private MessageTemplate mt; // The template to receive replies
    private int bestPrice = 0;
    private int repliesCnt = 0; // The counter of replies from seller agents
    private AID bestBidder; // The agent who provides the best offer
    private AuctioneerGUI myGui;
    private boolean isReady = false;

    public void startAuction() {
        isReady = true;
    }

    public void setup() {
        myGui = new AuctioneerGUI(this);
        myGui.showGui();

        System.out.println("Hallo! Auctioneer-agent " + getAID().getName() + " is ready.");
        System.out.println("Product name " + PRODUCT_NAME + " is for sale.");
        myGui.setpNameField(PRODUCT_NAME);
        myGui.setStatusField("Auction is ready.");
        myGui.setPriceField(Integer.toString(0));

        addBehaviour(new Behaviour() {
            @Override
            public void action() {
                if(isReady) {
                    //Bidder一覧を取得
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("bidder");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following bidder agents");
                        bidderAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            bidderAgents[i] = result[i].getName();
                            System.out.println(bidderAgents[i].getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    myAgent.addBehaviour(new RequestPerformer());
                }
            }

            @Override
            public boolean done() {
                return isReady;
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
                    myGui.setStatusField("Auction is starting.");
                    //オークションの商品情報をBidderに通知(step 0)
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < bidderAgents.length; ++i) {
                        cfp.addReceiver(bidderAgents[i]);
                    }
                    cfp.setContent(Integer.toString(bestPrice));
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
                    int refusedBidderCnt = 0; //入札を続けるかどうか判定

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
                        } else if (reply.getPerformative() == ACLMessage.REFUSE) {
                            refusedBidderCnt++;
                        }
                        repliesCnt++;
                        /**
                         * 最高価格提示Bidderが決まるまでstep0→step1を繰り返す
                         */
                        if (repliesCnt >= bidderAgents.length) {
                            if (refusedBidderCnt >= bidderAgents.length - 1) {
                                System.out.println(bestBidder + ": " + bestPrice);
                                myGui.setBidderField(bestBidder.getName());
                                myGui.setPriceField(Integer.toString(bestPrice));
                                step = 2;
                            } else {
                                step = 0;
                            }
                        } else {
                            block();
                        }
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            myGui.setStatusField("Auction is finished");
            return ((step == 2 && bestBidder == null) || step == 4);
        }
    }
}
