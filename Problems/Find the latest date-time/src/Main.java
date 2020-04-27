import java.time.LocalDateTime;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int count = Integer.parseInt(scanner.nextLine());

        LocalDateTime[] res = new LocalDateTime[count];

        for (int i = 0; i < count; i++) {
            LocalDateTime dateTime = LocalDateTime.parse(scanner.nextLine());
            res[i] = dateTime;
        }

        LocalDateTime max = res[0];
        for (int i = 1; i < count; i++) {
            if (res[i].compareTo(max) > 0) max = res[i];
        }

        System.out.println(max);
    }
}