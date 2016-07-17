/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package portal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sadegh_M
 */
public class Course implements Runnable{
    private Thread thread;
    private String courseId;
    private String courseTitle;
    private String courseDays;
    private String coursePlace;
    private int courseCapacity;
    private int udpPort;
    private ArrayList<String> studentsIds;
    private String courseIp;
    
    Course(String courseId, String courseTitle, String courseDays, String coursePlace, int courseCapacity, int udpPort) throws UnknownHostException{
        this.courseCapacity = courseCapacity;
        this.courseDays = courseDays;
        this.courseId = courseId;
        this.coursePlace = coursePlace;
        this.courseTitle = courseTitle;
        this.udpPort = udpPort;
        this.studentsIds = new ArrayList<String>();
        this.courseIp = InetAddress.getLocalHost().getHostAddress();
        this.studentsIds = new ArrayList<String>();
    }
    
    public String getCourseId(){
        return this.courseId;
    }
    
    public boolean add(String studentId){
        if(getEmtyChairs()>0){
            studentsIds.add(studentId);
            return true;
        }else return false;
    }
    
    public int getEmtyChairs(){
        return (courseCapacity - studentsIds.size());
    }
    
    public boolean hasStudent(String studentId){
        for(String id : studentsIds){
            if(studentId.equals(id))return true;
        }
        return false;
    }
    
    public int getCoursePort(){
        return this.udpPort;
    }        
    
    public void drop(String studentId){
        for(String id : studentsIds){
            if(studentId.equals(id)){
                studentsIds.remove(id);
                return;
            }
        }
    }        

    @Override
    public void run() {
        print("UDP Port:"+udpPort+"\tIP Address:"+courseIp);
        try {
            DatagramSocket serverSocket = new DatagramSocket(udpPort);
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            while(true)
            {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String request = new String( receivePacket.getData());
                print(request);
                String[] requestInfoes = request.split("\\s+");
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();         
                sendData = makeResponse("COURSE", courseTitle+"-"+courseDays+"-"+coursePlace+"-").getBytes();
                if(requestInfoes[1].equals("INFOREQ")){
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
                }else{
                    print("invalid request");
                }                
            }
        } catch (SocketException ex) {
            System.out.println(courseId+" :can't make server Socket");
        } catch (IOException ex) {
            Logger.getLogger(Course.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void start(){
        if (thread == null)
        {
           thread = new Thread (this, courseId);
           thread.start ();
        }
    }
    
    private String makeResponse(String type, String payLoad){
        String response = courseId;
        if( type!= null)response += (" "+type);
        if( payLoad!= null)response += (" "+payLoad);
        return response;
    }
    
    private void print(String message){
        System.out.println(courseId+"\t"+message);
    }
}
