import java.util.ArrayList;
import java.util.Scanner;
public class StudentGradeTracker {
    public static void main(String[] args) {
        ArrayList<Student> studentList = new ArrayList<>();
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("=== Welcome to the Student Grade Management System ===");
            while (true) {
                System.out.print("\nEnter 'exit' to stop, or press Enter to add a student: ");
                String control = sc.nextLine().trim();
                if (control.equalsIgnoreCase("exit")) {
                    break;
                }
                String name = "";           
                while (true) {
                    System.out.print("Enter student name: ");
                    name = sc.nextLine().trim();   
                    if (name.isEmpty()) {
                        System.out.println("Name cannot be empty. Please try again.");
                    }
                    else if (!name.matches("^[a-zA-Z ]+$")) { 
                        System.out.println("Invalid name! Names should only contain letters and spaces.");
                    }
                    else {
                        break;
                    }
                }
                double grade = -1;
                while (true) {
                    System.out.print("Enter " + name + "'s grade: ");
                    try {
                        grade = Double.parseDouble(sc.nextLine());
                        if (grade < 0 || grade > 100) {
                            System.out.println("Invalid grade. Enter a value between 0 and 100.");
                        } else {
                            break;
                        } 
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid number for the grade.");
                    }
                }
                studentList.add(new Student(name, grade));
            }
            if (studentList.isEmpty()) {
                System.out.println("\nNo student data entered. Goodbye!");
            } else {
                display(studentList);
            }
        }
    }
    private static void display(ArrayList<Student> students) {
        double total = 0;
        Student highest = students.get(0);
        Student lowest = students.get(0);

        System.out.println("\n --- Student Grades ---");
        for (Student s : students) {
            System.out.printf("Student: %-15s | Grade: %.2f\n", s.getName(), s.getGrade());
            total += s.getGrade();
            
            if (s.getGrade() > highest.getGrade()) {
                highest = s;
            }
            if (s.getGrade() < lowest.getGrade()) {
                lowest = s;
            }
        }
        
        double average = total / students.size();
        System.out.println(" --------------------------------- ");
        System.out.println("          SUMMARY REPORT           ");
        System.out.println(" --------------------------------- ");
        System.out.printf("Total Students: %d\n", students.size());
        System.out.printf("Average Grade: %.2f\n", average);
        System.out.printf("Highest Grade: %.2f (%s)\n", highest.getGrade(), highest.getName());
        System.out.printf("Lowest Grade: %.2f (%s)\n", lowest.getGrade(), lowest.getName());
        System.out.println(" --------------------------------- ");
    }
}
class Student {
    private final String name;
    private final double grade;

    public Student(String name, double grade) {
        this.name = name;
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public double getGrade() {
        return grade;
    }
}