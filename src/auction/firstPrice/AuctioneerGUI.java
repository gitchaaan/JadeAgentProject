package auction.firstPrice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Owner on 2015/01/07.
 */
public class AuctioneerGUI extends JFrame {
    private Auctioneer myAgent;
    private JTextField pNameField, priceField, bidderField, statusField;

    AuctioneerGUI(Auctioneer a) {
        super(a.getLocalName());
        myAgent = a;

        setTitle("Auction Monitor");

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(4, 2));

        p.add(new JLabel("Product Name:"));
        pNameField = new JTextField(15);
        p.add(pNameField);

        p.add(new JLabel("Price:"));
        priceField = new JTextField(15);
        p.add(priceField);

        p.add(new JLabel("Bidder:"));
        bidderField = new JTextField(15);
        p.add(bidderField);

        p.add(new JLabel("Status:"));
        statusField = new JTextField(15);
        p.add(statusField);

        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Start Auction");
        addButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                myAgent.startAuction();
            }
        } );
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );
        setResizable(false);
    }

    public void setpNameField(String s) {
        pNameField.setText(s);
    }

    public void setPriceField(String s) {
        priceField.setText(s);
    }

    public void setBidderField(String s) {
        bidderField.setText(s);
    }

    public void setStatusField(String s) {
        statusField.setText(s);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int)screenSize.getWidth() / 5, (int)screenSize.getHeight() / 5);
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

}
