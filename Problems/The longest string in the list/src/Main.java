import java.util.*;

public class Main {

    static void changeList(List<String> list){

        int lengthLongestString = 0;
        String longestStringOfList = "";

        for (String s : list) {
            if (s.length() > lengthLongestString) {
                lengthLongestString = s.length();
                longestStringOfList = s;
            }
        }

        for (int i = 0; i < list.size(); i++) {
            list.set(i, longestStringOfList);
        }

    }

    /* Do not change code below */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        List<String> lst = Arrays.asList(s.split(" "));
        changeList(lst);
        lst.forEach(e -> System.out.print(e + " "));
    }
}