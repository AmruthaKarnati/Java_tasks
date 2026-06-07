import java.util.Scanner;

class Number_guess_game_java {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int num=1+(int)(Math.random()*100);
        int count=0;
        
        while(true){
            System.out.print("Enter the number: ");
            int guess=sc.nextInt();

            count++;
            if(guess>num){
                System.out.println("The number is too high. Try again!");
            }
            else if(guess<num){
                System.out.println("The number is too low. Try again!");
            }
            else{
                System.out.println("Congratulations! You've guessed the number.");
                System.out.println("Number of tries: "+count);
                return;
            }
        }
    }
}