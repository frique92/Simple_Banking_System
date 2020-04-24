// Posted from EduTools plugin
import java.util.Scanner;

class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        int n = scanner.nextInt();
        int m = scanner.nextInt();
        scanner.nextLine();

        String[][] strings = new String[n][];

        for (int i = 0; i < n; i++) {
            strings[i] = scanner.nextLine().split(" ");
        }

        int swapI = scanner.nextInt();
        int swapJ = scanner.nextInt();

        for (int i = 0; i < n; i++) {
            String tmp = strings[i][swapI];
            strings[i][swapI] = strings[i][swapJ];
            strings[i][swapJ] = tmp;

            for (int j = 0; j < strings[i].length; j++) {
                System.out.print(strings[i][j] + " ");
            }
            System.out.println();
        }


    }
}