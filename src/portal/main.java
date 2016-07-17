/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package portal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Sadegh_M
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException {
        RegServer server = new RegServer("E:\\schedule.txt", "RegistrationServer", 2018);
        server.start();
        String strategyFileAddr = "E:\\strategy.txt";
        try {
            File strategyFile = new File(strategyFileAddr);
            try(BufferedReader br = new BufferedReader(new FileReader(strategyFile))) {
                for(String studentInfo; (studentInfo = br.readLine()) != null; ) {
                    String[] studentInfoes = studentInfo.split("\\s+");                    
                    String serverIpAddress = server.getIpAddress();
                    int serverPortNum = server.getPortNum();
                    Student student = new Student(studentInfoes[0], serverPortNum, Double.parseDouble(studentInfoes[1]), Integer.parseInt(studentInfoes[2]), Integer.parseInt(studentInfoes[3]), Integer.parseInt(studentInfoes[4]), serverIpAddress);
                    student.start();
                }
            }
        }
        catch (Exception e) {
            System.out.println("File not found at " + strategyFileAddr);
        }
    }
    
}
