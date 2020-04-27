import java.time.LocalTime;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        LocalTime time = LocalTime.parse("20:00");

        int count = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < count; i++) {
            String[] parts = scanner.nextLine().split(" ");
            if (time.isBefore(LocalTime.parse(parts[1]))) System.out.println(parts[0]);
        }
    }
}