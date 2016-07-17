/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package portal;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sadegh_M
 */
public class Student implements Runnable{
    private Thread thread;
    private String studentId;
    private int serverPortNum;
    private double Possibility;
    private int startTime;
    private int opNum;
    private int rtt;
    private String serverIp;
    private boolean[] coursesState;
    private int[] coursesPortNum;
    private int availableCourses;
    private Socket clientSocket;
    DataOutputStream outToServer;
    DataInputStream inFromServer;
    
    
    public Student(String studentId, int serverPortNum, double Possibility, int startTime, int opNum, int rtt, String serverIp){
        this.serverPortNum = serverPortNum;
        this.studentId = studentId;
        this.Possibility = Possibility;
        this.rtt = rtt;
        this.opNum = opNum;
        this.startTime = startTime;
        this.serverIp = serverIp;  
        this.availableCourses = 0;
    }        
    
    @Override
    public void run() {
        try {          
            clientSocket = new Socket(serverIp, serverPortNum);
            print("TCP Port: "+clientSocket.getLocalPort()+" IP address: "+InetAddress.getLocalHost().getHostAddress()+"\n\t"
                    +"Course1 choose prob: "+Possibility+" Course2 choose prob: "+(1.00-Possibility));
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new DataInputStream(clientSocket.getInputStream());
            //clientSocket = new Socket(serverIp, serverPortNum);            
            tcpSend(makeRequest("HELLO", null));            
            String[] response = tcpReceive().split("\\s+");
                        
            if(response[1].equals("WELCOME")){
                print("connected to the RegServer");
                this.availableCourses = Integer.parseInt(response[2]);
                coursesState = new boolean[availableCourses];
                coursesPortNum = new int[availableCourses];
                for(int i=opNum; i > 0; i--){
                    double rand = Math.random();
                    if(rand < Possibility){
                        if(coursesState[0]){                          
                            tcpSend(makeRequest("DROP", "1"));
                            print("drop course1");
                            response = tcpReceive().split("\\s+");
                            thread.sleep(rtt*1000);
                            if(response[1].equals("ACK")){
                                coursesState[0] = false;                                 
                                print("have dropped course1");
                            }                            
                        }else{
                            tcpSend(makeRequest("ADD", "1"));
                            print("add course1");
                            response = tcpReceive().split("\\s+");
                            thread.sleep(rtt*1000);
                            if(response[1].equals("ACK")){                                
                                coursesState[0] = true;
                                print("have added course1");
                            }else if(response[1].equals("NACK")){
                                coursesState[0] = false;                                 
                                print("fail to add course1");
                            }else{
                                print("unvalid response!!!");
                            }
                        }
                    }else{
                        if(coursesState[1]){
                            tcpSend(makeRequest("DROP", "2"));
                            print("drop course2");
                            response = tcpReceive().split("\\s+");
                            thread.sleep(rtt*1000);
                            if(response[1].equals("ACK")){
                                coursesState[1] = false;
                                print("have dropped course2");
                            }
                        }else{
                            tcpSend(makeRequest("ADD", "2"));
                            print("add course2");
                            response = tcpReceive().split("\\s+");
                            thread.sleep(rtt*1000);
                            if(response[1].equals("ACK")){
                                coursesState[1] = true;
                                print("have added course2");
                            }else if(response[1].equals("NACK")){
                                coursesState[1] = false;
                                print("fail to add course2");
                            }else{
                                print("unvalid response!!!");
                            }
                        }
                    }
                }
                tcpSend(makeRequest("SUBMIT", null));
                response = tcpReceive().split("\\s+");
                if(response[1].equals("PORT")){
                    String registerResult="register for";
                    if(coursesState[0] || coursesState[1]){
                        for(String coursePort: response[2].split("-")){
                            int id = Integer.parseInt(coursePort.split(":")[0]);
                            int port = Integer.parseInt(coursePort.split(":")[1]);
                            coursesPortNum[id-1] = port;     
                            registerResult += (" course"+id+", course server port is: "+port);
                        }                        
                    }       
                    print(registerResult);
                }
            }else{
                print("server not responding!!!\n");
            }          
            
            clientSocket.close();
        } catch (IOException ex) {
            print("can't make connecction to server!!!");
        } catch (InterruptedException ex) {   
            print("can't make connecction to server!!!");
            Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
        }
                 
        if(coursesState[0] || coursesState[1]){
            try {
                DatagramSocket clientSocket = new DatagramSocket();         
                print("UDP Port: "+clientSocket.getLocalPort());

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];
                sendData = makeRequest("INFOREQ", "").getBytes();

                for(int i=0; i<availableCourses; i++){
                    if(coursesState[i]){
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(serverIp), coursesPortNum[i]);
                        clientSocket.send(sendPacket);                        
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        clientSocket.receive(receivePacket);
                        String[] modifiedSentence = new String(receivePacket.getData()).split("\\s+");
                        if(modifiedSentence[1].equals("COURSE")){
                            String[] courseInf = modifiedSentence[2].split("-");
                            print(modifiedSentence[0]+" <"+courseInf[0]+"> <"+courseInf[1]+"> <"+courseInf[2]+">");
                        }                
                    }
                }     
                clientSocket.close();                
            } catch (SocketException ex) {
                print("can't make connecction to course!!!");
            } catch (IOException ex) {
                print("can't make connecction to course!!!");
            }
        } 
        print("THE END!");
    }
    private void tcpSend(String data) throws IOException{            
        outToServer.writeUTF(data);
    }
    private String tcpReceive() throws IOException{        
        return inFromServer.readUTF();
    }
    public void start ()
    {
        if (thread == null)
        {
            thread = new Thread (this, studentId);
            thread.start ();
        }
    }
    
    private String makeRequest(String type, String payLoad){
        String response = studentId;
        if( type!= null)response += (" "+type);
        if( payLoad!= null)response += (" "+payLoad);
        return response;
    }
    
    private void print(String message){
        System.out.println(studentId+"\t"+message);
    }
}
