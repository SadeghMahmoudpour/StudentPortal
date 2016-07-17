/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package portal_final;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sadegh_M
 */
public class MyFileReader {
    public static List<Course> schedule(String scheduleFileAddr) {
            List<Course> courses = new ArrayList<>();
            File scheduleFile = new File(scheduleFileAddr);
            try(
                BufferedReader br = new BufferedReader(new FileReader(scheduleFile))) {
                int courseCount=0;
                for(String courseInfo;courseCount < 2 && (courseInfo = br.readLine()) != null; ) {
                    courseCount++;
                    int coursePort = 4018;
                    String[] courseInfoes = courseInfo.split("\\s+");
                    Course course = new Course(courseInfoes[0], courseInfoes[1], courseInfoes[2], courseInfoes[3],  Integer.parseInt(courseInfoes[4]), coursePort);
                    course.start();
                    courses.add(course);                    
                }
            } catch (IOException ex) {
            Logger.getLogger(MyFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
            return courses;
    } 

    public static List<Student> strategy(String strategyFileAddr) {
        List<Student> students = new ArrayList<>();
        File strategyFile = new File(strategyFileAddr);
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(strategyFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(String studentInfo; (studentInfo = br.readLine()) != null; ) {
            String[] studentInfoes = studentInfo.split("\\s+");                    
            Student student = new Student(studentInfoes[0], Double.parseDouble(studentInfoes[1]), Integer.parseInt(studentInfoes[2]), Integer.parseInt(studentInfoes[3]), Integer.parseInt(studentInfoes[4]));
            students.add(student);
        }
        return students;
    }
}
