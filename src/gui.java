import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;

public class gui {
    final static boolean shouldFill = true;
    final static boolean shouldWeightX = false;
    final static boolean RIGHT_TO_LEFT = false;
    static int printer_count;
    static int user_count;
    static int disk_count;
    private static JLabel speed;
    static int[] disk_held;
    static int[] user_served;
    static boolean[] printing;
    static JLabel[] writes;
    static JLabel[] reads;
    
    private static OSDraw panel;
    private static JFrame frame;
    //private static boolean done;


    public static void updateLabel(int type, int i, String newText){
        if (type == 0){
            writes[i].setText(newText);
        }
        
        else{
            reads[i].setText(newText);
        }
    }

    private static void addLabels(int count, OSDraw panel){
        for (int i = 0; i < count; i++){
            //panel.setLayout(null);
            String name = String.format("", i);
            JLabel wlabel = new JLabel(name);
            wlabel.setBounds(590, 40 + (180*i),200,30); 
            panel.add(wlabel);
            writes[i] = wlabel;

            //name = String.format("", i);
            JLabel rlabel = new JLabel(name);
            rlabel.setBounds(590, 60 + (180*i),200,30); 
            panel.add(rlabel);
            reads[i] = rlabel;
        }
    }

    public static void updatePrinterStatus(int i, boolean b){
        printing[i] = b;
    }

    public static void updateDiskHolds(int i, int j){
        if (j==0) { 
            int k = disk_held[i];
            user_served[k-1] = j;
        }

        else{
            user_served[j-1] = i+1;
        }

        disk_held[i] = j;

    }

    public static void updateGUI(){
        panel.repaint();
        // frame.add(panel);
        // frame.setVisible(true);
    }

    public static void makeGUI(int pri, int disks, int users) {
        printer_count = pri;
        disk_count = disks;
        user_count = users;
        disk_held = new int[user_count];
        user_served = new int[disk_count];
        printing = new boolean[printer_count];
        writes = new JLabel[disk_count];
        reads = new JLabel[disk_count];
        //done = false;

        frame = new JFrame("Java OS Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        frame.pack();
        frame.setSize(1200, 800);
        panel = new OSDraw();

        JMenuBar mb = new JMenuBar();
        JButton halfDown = new JButton("0.5");
        JButton halfUp = new JButton("1.5");
        JButton normal = new JButton("1");
        JButton doub = new JButton("2");
        JButton quad = new JButton("4");

        speed = new JLabel("     Speed: 1x    ");

        halfDown.setActionCommand("0.5x");
        halfUp.setActionCommand("1.5x");
        normal.setActionCommand("1x");
        doub.setActionCommand("2x");
        quad.setActionCommand("4x");


  
        halfDown.addActionListener(new ButtonClickListener()); 
        halfUp.addActionListener(new ButtonClickListener()); 
        normal.addActionListener(new ButtonClickListener()); 
        doub.addActionListener(new ButtonClickListener()); 
        quad.addActionListener(new ButtonClickListener()); 




        mb.add(speed);
        mb.add(halfDown);
        mb.add(normal);
        mb.add(halfUp);
        mb.add(doub);
        mb.add(quad);
        frame.getContentPane().add(BorderLayout.NORTH, mb);

        addLabels(disk_count, panel);
        frame.add(panel);
        frame.setVisible(true);
    }
    
    static class OSDraw extends Panel{
        public void paint(Graphics g){
            Color yellow = new Color(255, 255, 0);

            // if(done){
            //     Graphics2D g2d = (Graphics2D)g;
            //     Font f = new Font("Dialog", Font.BOLD, 18);
            //     g2d.setFont(f);
            //     g2d.drawString("DONE", 400, 600);
            // }

            super.paint(g);
            for (int i = 0; i < gui.user_count; i++){
                Graphics2D g2d = (Graphics2D)g;
                g2d.setColor(yellow);     
                g2d.fillRect(10, (i*110)+10, 100, 100); 
                g2d.setColor(Color.BLACK);
                g2d.drawRect(10, (i*110)+10, 100, 100); 
                String name = "UserThread"+(i+1);
                Font f = new Font("Dialog", Font.BOLD, 12);
                g2d.setFont(f);
                g2d.drawString(name, 15, (i*110) +25);

                if (disk_held[i] != 0){
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(96, (i*110)+96, 20, 20);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("D"+disk_held[i], 100, (i*110) + 108);
                }
                
            }

            for (int i = 0; i < gui.printer_count; i++){
                Graphics2D g2d = (Graphics2D)g;
                if (printing[i]){
                    g2d.setColor(Color.RED);     
                    g2d.fillRect(930, (i*110)+10, 180, 100); 
                }

                else{
                    g2d.setColor(Color.GREEN);     
                    g2d.fillRect(930, (i*110)+10, 180, 100); 
                }

                g2d.setColor(Color.BLACK);
                g2d.drawRect(930, (i*110)+10, 180, 100); 
                String name = "Printer"+(i+1);
                Font f = new Font("Dialog", Font.BOLD, 12);
                g2d.setFont(f);
                g2d.drawString(name, 935, (i*110) +25);
                if(printing[i]){g2d.drawString("....printing....", 985, (i*110) + 75);}
            }

            for (int i = 0; i < gui.disk_count; i++){
                Graphics2D g2d = (Graphics2D)g;
                g2d.setColor(Color.BLUE);
                g2d.fillPolygon(new int[] {430, 500, 570}, new int[] {130 + (180*i), 20 + (180*i), 130 +(180 *i)}, 3);  
                g2d.setColor(Color.BLACK);
                g2d.drawPolygon(new int[] {430, 500, 570}, new int[] {130 + (180*i), 20 + (180*i), 130 +(180 *i)}, 3);  
                String name = "Disk"+(i+1);
                Font f = new Font("Dialog", Font.BOLD, 12);
                g2d.setFont(f);
                g2d.drawString(name, 485, (i*180) + 145);

                // if(user_served[i] != 0){
                //     g2d.setColor(yellow);
                //     g2d.fillRect(495, (i*180)+100, 20, 20);
                //     g2d.setColor(Color.BLACK);
                //     g2d.drawString("U"+user_served[i], 498, (i*180) + 112);     
                // }
            } 
    
        }
        
        OSDraw(){
            super();
            //setLayout(new GridLayout(0,1,0,50));
            setLayout(null);
 
        }
    }

    private static class ButtonClickListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
           String command = e.getActionCommand();  
           
           if( command.equals( "0.5x" ))  {
              speed.setText("     Speed: 0.5x    ");
              JavaSim.setSpeed(0.5);
           } else if( command.equals( "1x" ) )  {
              speed.setText("     Speed: 1x    "); 
              JavaSim.setSpeed(1.0);
           } else if( command.equals("1.5x"))  {
              speed.setText("     Speed: 1.5x    ");
              JavaSim.setSpeed(1.5);
           } else if(command.equals ("2x")) {
              speed.setText("     Speed: 2x    ");
              JavaSim.setSpeed(2);
           }else if(command.equals ("4x")) {
              speed.setText("     Speed: 4x    ");
              JavaSim.setSpeed(4);
         }   	
        }		
    }
  
}