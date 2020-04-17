import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int size = scanner.nextInt();

        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = scanner.nextInt();
        }

        int n = scanner.nextInt();
        int m = scanner.nextInt();

        boolean contains = false;

        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] == n && arr[i + 1] == m) {
                contains = true;
                break;
            }
            if (arr[i] == m && arr[i + 1] == n) {
                contains = true;
                break;
            }
        }

        System.out.println(contains);
    }
}