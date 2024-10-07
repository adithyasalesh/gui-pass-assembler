import javax.lang.model.util.ElementScanner6;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;
import java.util.HashSet;

class Passing implements ActionListener {
    JFrame f;
    JButton b1, b2;
    JTextArea t1, t2;
    String result="";
    JLabel label1, label2;
    StringBuilder objectCodeBuilder; 

    Passing() {
        f = new JFrame("PASS ASSEMBLER");
        t1 = new JTextArea();
        t2 = new JTextArea();
        label1 = new JLabel("Intermediate File Content");
        label2 = new JLabel("Symbol Table Content");
        b1 = new JButton("PASS1");
        b2 = new JButton("PASS2");

        t1.setBounds(30, 40, 600, 300);
        t2.setBounds(700, 40, 600, 300);
        label1.setBounds(30, 10, 200, 30);
        label2.setBounds(700, 10, 200, 30);
        b1.setBounds(40, 350, 100, 40);
        b2.setBounds(150, 350, 100, 40);



        f.add(label1);
        f.add(label2);
        f.add(t1);
        f.add(t2);
        f.add(b1);
        f.add(b2);

        f.setLayout(null);
        f.setVisible(true);
        f.setSize(1000, 500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        b1.addActionListener(this);
        b2.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == b1) {
            label1.setText("Intermediate File Content (Pass 1)");
            label2.setText("Symbol Table Content (Pass 1)");
            runPass1();
        } else if (e.getSource() == b2) {
            label1.setText("Final Code (Pass 2)");
            label2.setText("Object Code Records (Pass 2)");
            runPass2();
        }
    }

    public void runPass1() {
        JFileChooser fileChooser = new JFileChooser();
        File inputFile = null, optabFile = null;

        fileChooser.setDialogTitle("Select Input File (input.txt)");
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            inputFile = fileChooser.getSelectedFile();
        } else {
            t1.setText("No input file selected.");
            return;
        }

        fileChooser.setDialogTitle("Select OPTAB File (optab.txt)");
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            optabFile = fileChooser.getSelectedFile();
        } else {
            t1.setText("No OPTAB file selected.");
            return;
        }
         HashSet<String> symtabSet = new HashSet<>();

        try {
            BufferedReader input = new BufferedReader(new FileReader(inputFile));
            BufferedReader optab = new BufferedReader(new FileReader(optabFile));
            BufferedWriter symtab = new BufferedWriter(new FileWriter("symtab.txt"));
            BufferedWriter intermediate = new BufferedWriter(new FileWriter("intermediate.txt"));

            int locctr = 0,addr=0;
            String label, opcode, operand;
          
           int flag = 0; 
            String line = input.readLine();
            if (line != null) {
                String[] parts = line.split("\\s+");
                label = parts[0];
                opcode = parts[1];
                operand = parts[2];

                if (opcode.equals("START")) {
                    locctr = Integer.parseInt(operand, 16); 
                     addr=locctr;
                    intermediate.write(String.format("-\t%s\t%s\t%s\n", label, opcode, operand));
                    line = input.readLine();
                } else {
                    locctr = 0;
                }
                     
                
                while (line != null && !opcode.equals("END")) {
                    parts = line.split("\\s+");
                    label = parts[0];
                    opcode = parts[1];
                    operand = parts.length > 2 ? parts[2] : "";
                    flag = 0;
                    intermediate.write(String.format("%04X\t%s\t%s\t%s\n", locctr, label, opcode, operand));

                    if (!label.equals("-")) {
                        if (symtabSet.contains(label)) {
                            flag = 1; 
                         symtab.write(label + "\t" + String.format("%04X", locctr) + "\t" +flag+ "\n");
                        } else {
                            flag = 0; 
                            symtab.write(label + "\t" + String.format("%04X", locctr) + "\t" +flag+ "\n");
                        }
                    }
                    else if (label.equals("-")){
                        
                    }

                    if(!opcode.equals("WORD")&&!opcode.equals("RESW")&&!opcode.equals("RESB")&&!opcode.equals("BYTE")&&!opcode.equals("END")){
                     int b=locctr;
                     int length=b-addr;
                     result=Integer.toHexString(length).toUpperCase();
                    }
                    
                    if (opcode.equals("WORD")) {
                        locctr += 3;
                    } else if (opcode.equals("RESW")) {
                        locctr += 3 * Integer.parseInt(operand);
                    } else if (opcode.equals("RESB")) {
                        locctr += Integer.parseInt(operand);
                    } else if (opcode.equals("BYTE")) {
                        locctr += operand.length() - 3;
                    } else {
                        locctr += 3;
                    }
                
                    line = input.readLine();
                }
            }

            input.close();
            optab.close();
            symtab.close();
            intermediate.close();

            BufferedReader intermediateReader = new BufferedReader(new FileReader("intermediate.txt"));
            StringBuilder intermediateContent = new StringBuilder();
            String intermediateLine;
            while ((intermediateLine = intermediateReader.readLine()) != null) {
                intermediateContent.append(intermediateLine).append("\n");
            }
            t1.setText(intermediateContent.toString());
            intermediateReader.close();

            BufferedReader symtabReader = new BufferedReader(new FileReader("symtab.txt"));
            StringBuilder symtabContent = new StringBuilder();
            String symtabLine;
            while ((symtabLine = symtabReader.readLine()) != null) {
                symtabContent.append(symtabLine).append("\n");
            }
            t2.setText(symtabContent.toString());
            symtabReader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            t1.setText("Error in PASS1: " + ex.getMessage());
        }
    }
    public void runPass2() {
        try {
            File optabFile = new File("optab.txt");
            File symtabFile = new File("symtab.txt");
            File intermediateFile = new File("intermediate.txt");
            File finalFile = new File("final.txt");
            File recordFile = new File("record.txt");
    
            Scanner optabScanner = new Scanner(optabFile);
            Scanner symtabScanner = new Scanner(symtabFile);
            Scanner intermediateScanner = new Scanner(intermediateFile);
            PrintWriter finalWriter = new PrintWriter(finalFile);
            PrintWriter recordWriter = new PrintWriter(recordFile);
    
            int address = 0,count=0;
            String label, opcode, operand, content,newadd;
            String objectcode = "";
            String startAddress = "";
            boolean flag=false;
            objectCodeBuilder = new StringBuilder();
    
            if (intermediateScanner.hasNextLine()) {
                String line = intermediateScanner.nextLine();
                String[] parts = line.split("\\s+");
                newadd = parts[0];
                label = parts[1];
                opcode = parts[2];
                operand = parts[3];
                content = parts.length > 4 ? parts[4] : "";
                
                if (opcode.equals("START")) {
                    startAddress = operand;
                    finalWriter.printf("%s\t\t%s\t%s\n", label, opcode, operand);
                    recordWriter.printf("H^%s^%s^%s\n",label,startAddress,result);
                    recordWriter.printf("T^%s",startAddress);
                   
                    while (!opcode.equals("END")) {
                       
                        optabScanner = new Scanner(optabFile);
                        symtabScanner = new Scanner(symtabFile);
    
                       
                        while (optabScanner.hasNextLine()) {
                            String optabLine = optabScanner.nextLine();
                            String[] optabParts = optabLine.split("\\s+");
                            if (opcode.equals(optabParts[0])) {
                                objectcode = optabParts[1];
                                break;
                            }
                        }
    
                       
                        if (!operand.isEmpty()) {
                            while (symtabScanner.hasNextLine()) {
                                String symtabLine = symtabScanner.nextLine();
                                String[] symtabParts = symtabLine.split("\\s+");
                                if (operand.equals(symtabParts[0])) {
                                  
                                    address = Integer.parseInt(symtabParts[1], 16);
                                    break; 
                                }
                            }
                        }
                      
                        
                        if (opcode.equals("WORD")) {
                           
                            if (operand != null && !operand.trim().isEmpty()) {
                                int wordValue = Integer.parseInt(operand.trim());
                                objectcode = String.format("%06X", wordValue);
                                count=count+3;
                            } else {
                                objectcode = "000000"; 
                                count=count+3;
                                System.err.println("Warning: Content for WORD is empty, setting objectcode to default 000000.");
                            }
                        } else if (opcode.equals("RESW") || opcode.equals("RESB")) {
                            objectcode = ""; 
                        }
                        else if (opcode.equals("START")|| opcode.equals("END"))
                        {
                            objectcode = ""; 
                        }
                     else if(opcode.equals("BYTE"))
                     {
                        if (operand.startsWith("C'")) {
                           
                            StringBuilder asciiValue = new StringBuilder();
                            for (int i = 2; i < operand.length() - 1; i++) {
                                asciiValue.append(String.format("%02X", (int) operand.charAt(i)));
                            }
                            objectcode = asciiValue.toString();
                            count=count+1;
                         
                        }
                    }
                         else {
                            objectcode = String.format("%s%04X", objectcode,address); 
                            count=count+3;
                        }
                        
                       
                        
                        if (!opcode.equals("START")) {
                            if(content.equals(null))
                            {
                            finalWriter.printf("%s\t%s\t%s\t%s\t%s\n",newadd, label, opcode, operand, objectcode);
                            }
                            else
                            {
                                finalWriter.printf("%s\t%s\t%s\t%s\t%s\t%s\n",newadd, label, opcode, operand,content, objectcode);
                            }
                        }
                       
                       
                        if (!objectcode.isEmpty()) {
                            objectCodeBuilder.append("^").append(objectcode);
                        }
                             
                       
                
                 objectcode = "";
    
                       
                        if (intermediateScanner.hasNextLine()) {
                            line = intermediateScanner.nextLine();
                            parts = line.split("\\s+");
                            newadd=parts[0];
                            label = parts[1];
                            opcode = parts[2];
                            operand = parts.length > 3 ? parts[3] : "";
                            content = parts.length > 4 ? parts[4] : ""; 
                        } else {
                            break;
                        }
                    } 
                    if(opcode.equals("END"))
                    {
                        finalWriter.printf("%s\t%s\t%s\t%s\n",newadd, label, opcode, operand); 
                    }
    
                    if(!flag)
                    {
                   // count=count*3;
                    String value = Integer.toHexString(count).toUpperCase();
                    recordWriter.printf("^%s",value); 
                    flag=true;
                    }
                    recordWriter.printf("%s\n", objectCodeBuilder.toString());
                    recordWriter.printf("\nE^%s\n", startAddress); 
                }
    
                
                optabScanner.close();
                symtabScanner.close();
                intermediateScanner.close();
                finalWriter.close();
                recordWriter.close();
            }
    
        
            StringBuilder finalContent = new StringBuilder();
            try (BufferedReader finalReader = new BufferedReader(new FileReader("final.txt"))) {
                String finalLine;
                while ((finalLine = finalReader.readLine()) != null) {
                    finalContent.append(finalLine).append("\n");
                }
                t1.setText(finalContent.toString());
            }
    
            StringBuilder finalCon = new StringBuilder();
            try (BufferedReader finalRead = new BufferedReader(new FileReader("record.txt"))) {
                String finaly;
                while ((finaly = finalRead.readLine()) != null) {
                    finalCon.append(finaly).append("\n");
                }
                t2.setText(finalCon.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            t1.setText("Error in PASS2: " + ex.getMessage());
        }
    }
    
    
    public static void main(String[] args) {
        new Passing();
    
}
    }
    