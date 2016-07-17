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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sadegh_M
 */
public class RegServer implements Runnable {
    private Thread thread;
    private String serverId;
    private int tcpPort;
    private ArrayList<Course> courses;
    private String serverIp;
    //Queue<String> requests = new ArrayDeque<String>();
    
    RegServer(String scheduleFileAddr, String serverId, int tcpPort) throws UnknownHostException{
        courses = new ArrayList<Course>();
        this.serverId = serverId;
        this.tcpPort = tcpPort;
        this.serverIp = InetAddress.getLocalHost().getHostAddress();
        addCourses(scheduleFileAddr);        
    }
   
    public boolean addCourses(String scheduleFileAddr){
        try {
            File scheduleFile = new File(scheduleFileAddr);
            try(BufferedReader br = new BufferedReader(new FileReader(scheduleFile))) {
                int courseCount=0;
                for(String courseInfo; (courseInfo = br.readLine()) != null; ) {
                    String[] courseInfoes = courseInfo.split("\\s+");
                    Course course = new Course(courseInfoes[0], courseInfoes[1], courseInfoes[2], courseInfoes[3],  Integer.parseInt(courseInfoes[4]), ((courseCount*1000)+2000+tcpPort));
                    course.start();
                    courses.add(course);
                    courseCount++;
                }
            }
        }
        catch (Exception e) {
            print("ERROR:File not found at " + scheduleFileAddr);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void run() {
        print("TCP Port:"+tcpPort+"\tIP address:"+serverIp);        
        try {
            
            ServerSocket serverSocket = new ServerSocket(tcpPort);
            while(true)
            {
                Socket connectionSocket = serverSocket.accept(); 
                responceToRequest(connectionSocket);                                                                                               
            }           
        } catch (IOException ex) {
            print("can't make server Socket");
        }        
    }
    public void start ()
   {
      if (thread == null)
      {
         thread = new Thread (this, serverId);
         thread.start ();
      }
   }
    
    private void responceToRequest(Socket socket){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    print("run");
                    DataInputStream reader = new DataInputStream(socket.getInputStream());
                    DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                    boolean endRequests = false;
                    while(!endRequests){
                        String clientRequest = tcpReceive(reader);  
                        print(clientRequest);
                        String[] requestInfoes = clientRequest.split("\\s+");

                        //answer
                        if(requestInfoes.length >= 2){
                            if(requestInfoes[1].equals("HELLO")){  
                                print("Student "+requestInfoes[0]+" is online");
                                tcpSend(writer, makeResponse("WELCOME", courses.size()+""));
                            }else if(requestInfoes[1].equals("ADD")){ 
                                addCourse(writer, requestInfoes[0], requestInfoes[2]);
                            }else if(requestInfoes[1].equals("DROP")){
                                dropCourse(writer, requestInfoes[0], requestInfoes[2]);
                            }else if(requestInfoes[1].equals("SUBMIT")){
                                String ports = "";
                                String studentCourses = "";
                                for(Course course: courses){
                                    if(course.hasStudent(requestInfoes[0])){
                                        ports += ((1+courses.indexOf(course))+":"+course.getCoursePort()+"-");
                                        studentCourses += course.getCourseId()+" ";
                                    }
                                }
                                tcpSend(writer, makeResponse("PORT", ports));
                                print("Student "+requestInfoes[0]+" has registered "+studentCourses);    
                                endRequests = true;
                            }
                        }else{
                            print("unknown request!!!");
                        }
                    }
                    reader.close();
                    writer.close();
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(RegServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
    
    private void tcpSend(DataOutputStream writer, String data) throws IOException{            
        writer.writeUTF(data);
    }
    private String tcpReceive(DataInputStream reader) throws IOException{        
        return reader.readUTF();
    }
    
    private synchronized void dropCourse(DataOutputStream writer, String studentId, String courseId) throws IOException{
        print("Student "+studentId+" wants to drop "+courseId);
        int coursenum = Integer.parseInt(courseId);
        Course course = null;
        if(coursenum == 1){
            course = courses.get(0);                                                       
        }else if(coursenum == 2){
            course = courses.get(1); 
        }else{
            print("invalid course ID!!!");
        }
        if(course != null){                            
            course.drop(studentId);
            print("course "+course.getCourseId()+" is dropped for "+studentId);
            tcpSend(writer, makeResponse("ACK", null));
        }
    }
    
    private synchronized void addCourse(DataOutputStream writer, String studentId, String courseId) throws IOException{
        print("Student "+studentId+" wants to add "+courseId);
        int coursenum = Integer.parseInt(courseId);
        Course course = null;
        if(coursenum == 1){
            course = courses.get(0);                           
        }else if(coursenum == 2){
            course = courses.get(1);                            
        }else{
            print("invalid course ID!!!");
        }
        if(course != null){                            
            if(course.add(studentId)){      
                print("course "+course.getCourseId()+" is added for "+studentId);
                tcpSend(writer, makeResponse("ACK", null));
            }else{
                print("course "+course.getCourseId()+" cannot be added for "+studentId);                                 
                tcpSend(writer, makeResponse("NACK", null));
            }
        }
    }
    
    private String makeResponse(String type, String payLoad){
        String response = serverId;
        if( type!= null)response += (" "+type);
        if( payLoad!= null)response += (" "+payLoad);
        return response;
    }
    private void print(String message){
        System.out.println(serverId+"\t"+message);
    }
    public String getIpAddress(){
        return this.serverIp;
    }
    public int getPortNum(){
        return this.tcpPort;
    }
}
