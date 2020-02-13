import java.io.BufferedReader;
import java.util.*; 
import java.io.File;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.swing.*;
import java.awt.*;

class Disk{
    static final int NUM_SECTORS = 1024;
    StringBuffer sectors[] = new StringBuffer[NUM_SECTORS];


    Disk(){
        for(int i = 0; i < NUM_SECTORS; i++){
            sectors[i] = new StringBuffer();
        }
    }

    void write(int sector, StringBuffer data){
        try{
            Thread.sleep((int)(600/JavaSim.speedMult));
            sectors[sector].delete(0, sectors[sector].length());
            sectors[sector].insert(0, data);
        }

        catch(InterruptedException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }

    void read(int sector, StringBuffer data){
        try{
            Thread.sleep((int)(600/JavaSim.speedMult));
            data.delete(0, data.length());
            data.insert(0, sectors[sector]);
        }

        catch(InterruptedException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
}

class Printer {
    File printerName;

    Printer(int i){
        String name = String.format("PRINTER%d", i);
        printerName = new File(name);
    }

    void print(StringBuffer data){
        try{
            Thread.sleep((int)(2750/JavaSim.speedMult));


            FileWriter fileWriter = new FileWriter(printerName, true);
            BufferedWriter writer = new BufferedWriter(fileWriter); 
            writer.write(data.toString());
            writer.newLine();
            writer.flush();
        }

        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }


    }
    
    
}

// user threads
// go to disk mangr 
// request / release

class UserThread extends Thread{
	String fileName;
    StringBuffer line;
    int id;
	UserThread(String file) {
        id = Integer.parseInt(file.substring(4));
		fileName = "../inputs/"+ file;
	    line = new StringBuffer();
    }

    public void run() {
        processUserCommands(fileName);
    }

    void processUserCommands(String file){
        try {
            System.out.println("usercommands");
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(file));
            line.delete(0, line.length());
            line.insert(0, reader.readLine());
            while (!(line.toString().equals("null"))) {
                System.out.println(line);

                if (line.toString().startsWith(".save")){
                    System.out.println("found save");
                    String saveFile = line.substring(7, line.length());

                    int d = JavaSim.diskManager.request();

                    if (JavaSim.visible){
                        gui.updateDiskHolds(id-1, d+1);
                        gui.updateGUI();
                    }

                    int start = JavaSim.diskManager.openSec[d];
                    line.delete(0, line.length());
                    line.insert(0, reader.readLine());

                    while(!(line.toString().startsWith(".end"))){
                        System.out.println("saving lines");
                        System.out.println(line);

                        JavaSim.diskManager.write(d, line);
                        if(JavaSim.visible) {gui.updateLabel(0, d, "writing: "+line);}

                        line.delete(0, line.length());
                        line.insert(0, reader.readLine());
                    }


                    JavaSim.diskManager.release(d);
                    if(JavaSim.visible){
                        gui.updateLabel(0, d, "");
                        gui.updateDiskHolds(id-1, 0);
                        gui.updateGUI();
                    }

                    int len = JavaSim.diskManager.openSec[d] - start;
                    FileInfo newEntry = new FileInfo(d, start, len);
                    JavaSim.diskManager.add(saveFile, newEntry);

                    line.delete(0, line.length());
                    line.insert(0, reader.readLine());
                }
                
                else if (line.toString().startsWith(".print")){
                    String printFile = line.substring(8, line.length());
                    PrintJobThread newPrint = new PrintJobThread(printFile);
                    newPrint.start();

                    line.delete(0, line.length());
                    line.insert(0, reader.readLine());
                }

                else {
                    System.out.println("defaulting");

                    
                }
            }    
            System.out.println("null line");
            reader.close();
        }

        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
    }

}
    

class FileInfo {
    int diskNumber;
    int startingSector;
    int fileLength;

    FileInfo(int d, int s, int f){
        diskNumber = d;
        startingSector = s;
        fileLength = f;
    }
 }
    

class DirectoryManager {
    private Hashtable<String, FileInfo> T = new Hashtable<String, FileInfo>();

    void enter(String key, FileInfo file){
        T.put(key, file);
    }

    FileInfo lookup(String key){
        return T.get(key);
    }
}


class DiskManager extends ResourceManager{

    DirectoryManager dm;
    static int openSec[];

    DiskManager(int numberOfItems){
        super(numberOfItems);
        dm = new DirectoryManager();
        openSec = new int[numberOfItems];
        for (int i = 0; i < numberOfItems; i++){
            openSec[i] = 0;
        }
    }

    void write(int DiskNum, StringBuffer data){
        int open = openSec[DiskNum];
        JavaSim.disks[DiskNum].write(open, data);
        openSec[DiskNum] += 1;
    }

    void read(int DiskNum, int sec, StringBuffer data){
        JavaSim.disks[DiskNum].read(sec, data); 
    }

    FileInfo lookup(String fileName){
        return dm.lookup(fileName);
    }

    void add(String fileName, FileInfo entry){
        dm.enter(fileName, entry);
    }
}

class PrinterManager extends ResourceManager{

    
    PrinterManager(int numberOfItems){
        super(numberOfItems);
    }

    void print(String filename, int printerNum){
        FileInfo fInfo = JavaSim.diskManager.lookup(filename);
        int d = fInfo.diskNumber;
        int s = fInfo.startingSector;
        int f = fInfo.fileLength;
        int end = s + f; 
        StringBuffer data = new StringBuffer();
        for (int i = s; i < end; i++){
            JavaSim.diskManager.read(d, i, data);
            if (JavaSim.visible){gui.updateLabel(1, d, "reading: "+data);}

            JavaSim.printers[printerNum].print(data);
            data.delete(0, data.length());
            if (JavaSim.visible){gui.updateLabel(1, d, "");}

        }
    }
}

class PrintJobThread extends Thread{
	String fileName;
	PrintJobThread(String file) {
		fileName = file;
    }

    public void run() {
        processUserCommands(fileName);
    }

    void processUserCommands(String file){
        int p = JavaSim.printerManager.request();

        if(JavaSim.visible){
            gui.updatePrinterStatus(p, true);
            gui.updateGUI();
        }

        JavaSim.printerManager.print(file, p);
        JavaSim.printerManager.release(p);

        if(JavaSim.visible){
            gui.updatePrinterStatus(p, false);
            gui.updateGUI();
        }
    }

}
    

class ResourceManager {
	boolean isFree[];
	ResourceManager(int numberOfItems) {
		isFree = new boolean[numberOfItems];
		for (int i=0; i<isFree.length; ++i)
			isFree[i] = true;
    }
    
	synchronized int request() {
		while (true) {
			for (int i = 0; i < isFree.length; ++i)
				if ( isFree[i] ) {
					isFree[i] = false;
					return i;  
                }

            try{
                this.wait(); 
            }

            catch (InterruptedException e){
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }   

    synchronized void release( int index ) {
		isFree[index] = true;
		this.notify(); 
	}
}

//java -jar JavaSim.jar -#Users <USERS> -#Disks -#Printers

public class JavaSim{

	static int NUM_USERS=4, NUM_DISKS=2, NUM_PRINTERS=3;
    static String userFileNames[];
	static UserThread users[];
	static Disk disks[];
	static Printer printers[];
	static DiskManager diskManager;
    static PrinterManager printerManager;
    static boolean visible = true;
    static double speedMult;

    
    void configure(String args[]) {//this sets userFileNames and NUMs above }
        JavaSim.NUM_USERS = Integer.parseInt(args[0]) * -1;
        JavaSim.userFileNames = new String[JavaSim.NUM_USERS];
        int i;
        
        for(i = 1; i < JavaSim.NUM_USERS + 1; i++){
            JavaSim.userFileNames[i-1] = args[i];
        }

        JavaSim.NUM_DISKS = Integer.parseInt(args[i]) * -1;
        i++;
        JavaSim.NUM_PRINTERS = Integer.parseInt(args[i]) * -1;
    }

    static void setSpeed(double i){
        speedMult = i;
    }

    
	JavaSim(String args[]) {
        configure(args);
        speedMult = 1.0;
        if (args[args.length-1].equals("-ng")){
            visible = false;
        }

        users = new UserThread[NUM_USERS];
        disks = new Disk[NUM_DISKS];
        printers = new Printer[NUM_PRINTERS];

        for (int i = 0; i < NUM_USERS; i++) {
            users[i] = new UserThread(userFileNames[i]);
        }
        for (int i = 0; i < NUM_DISKS; i++) {
            disks[i] = new Disk();
        }
        for (int i =0; i < NUM_PRINTERS; i++) {
            printers[i] = new Printer(i+1);
        }

        diskManager = new DiskManager(NUM_DISKS);
        printerManager = new PrinterManager(NUM_PRINTERS);
    }

    public static void main(String args[]){
        JavaSim os = new JavaSim(args);
        if (visible){
            gui.makeGUI(NUM_PRINTERS, NUM_DISKS, NUM_USERS);
        }

		for (UserThread user : users) {
			user.start();
        }
        
		for (UserThread user : users) {
            try{
                user.join();
            }

            catch(InterruptedException e){
                System.out.println(e);
                e.printStackTrace();
            }
        }
        
        System.out.println("Done");

	}
    
}