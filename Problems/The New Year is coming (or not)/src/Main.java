import java.time.LocalDate;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] data = scanner.nextLine().split(" ");
        LocalDate date = LocalDate.parse(data[0]);
        int days = Integer.parseInt(data[1]);

        boolean isNewYear = false;

        LocalDate resultDate = date.plusDays(days);

        isNewYear = resultDate.compareTo(resultDate.withDayOfYear(1)) == 0;

        System.out.println(isNewYear ? "true" : "false");
    }
}