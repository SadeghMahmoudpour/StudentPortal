/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package portal_final;

import java.util.List;

/**
 *
 * @author Sadegh_M
 */
public class Portal_final {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        List<Course> courses = MyFileReader.schedule("E:\\schedule.txt");
        List<Student> students = MyFileReader.strategy("E:\\strategy.txt");

        RegServer server = new RegServer(courses);
        server.start();

        for (Student student : students) {
            new Client(student).start();
        }
    }
    
}
