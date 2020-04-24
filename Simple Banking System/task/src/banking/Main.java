package banking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String fileName = "";
        if (args.length > 0) {
            if ("-fileName".equals(args[0])) {
                fileName = args[1];
            }
        }

        BankingSystem bankingSystem = new BankingSystem(fileName);
        bankingSystem.start();
    }
}

class ManagerDB {

    private final String url;

    ManagerDB(String fileName) {
        url = "jdbc:sqlite:./" + fileName;
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;

    }

    public void createCards() {
        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " number TEXT NOT NULL,\n"
                + " pin TEXT NOT NULL,\n"
                + " balance INTEGER default 0\n"
                + ");";

        try (Connection conn = connect()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addCard(String number, String pin, int balance) {
        String sql = "INSERT INTO card (number, pin, balance) VALUES (?,?,?);";

        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, number);
                statement.setString(2, pin);
                statement.setInt(3, balance);
                statement.execute();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<RecordDB> getAllCards() {
        String sql = "SELECT number, pin, balance from card";

        ArrayList<RecordDB> records = new ArrayList<>();

        try (Connection conn = connect()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    records.add(new RecordDB(
                            rs.getString("number"),
                            rs.getString("pin"),
                            rs.getInt("balance")));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return records;
    }

}

class RecordDB {
    private final String number;
    private final String pin;
    private final int balance;

    RecordDB(String number, String pin, int balance) {
        this.number = number;
        this.pin = pin;
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public String getPin() {
        return pin;
    }

    public int getBalance() {
        return balance;
    }
}

class BankingSystem {

    enum State {
        MAIN_NON_AUTHORIZATION, MAIN_AUTHORIZATION
    }

    private final Scanner scanner = new Scanner(System.in);
    private boolean isWorking = true;
    private State state = State.MAIN_NON_AUTHORIZATION;
    private final Bank bank = new Bank();
    private Card currentCard;
    private final ManagerDB managerDB;

    BankingSystem(String fileName) {
        managerDB = new ManagerDB(fileName);
        managerDB.createCards();
        loadCardsIntoBank();
    }

    private void setState(State state) {
        this.state = state;
    }

    public void start() {
        while (isWorking) {
            showMenu();
            int action = Integer.parseInt(scanner.nextLine());
            System.out.println();
            runCommand(action);
            System.out.println();
        }
    }

    private void showMenu() {
        switch (state) {
            case MAIN_NON_AUTHORIZATION:
                showMainMenuNonAuthorization();
                break;
            case MAIN_AUTHORIZATION:
                showMainMenuAuthorization();
                break;
        }
    }

    private void showMainMenuNonAuthorization() {
        System.out.println("1. Create account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    private void showMainMenuAuthorization() {
        System.out.println("1. Balance");
        System.out.println("2. Log out");
        System.out.println("0. Exit");
    }

    private void runCommand(int action) {
        if (state == State.MAIN_NON_AUTHORIZATION) {
            switch (action) {
                case 1:
                    createNewCard();
                    break;
                case 2:
                    logInSystem();
                    break;
                case 0:
                    exit();
                    break;
                default:
                    System.out.println("Unknown action!");
            }
        } else if (state == State.MAIN_AUTHORIZATION) {
            switch (action) {
                case 1:
                    getBalance();
                    break;
                case 2:
                    logOutSystem();
                    break;
                case 0:
                    exit();
                    break;
                default:
                    System.out.println("Unknown action!");
            }
        }
    }

    private void createNewCard() {
        Card card = bank.createCard();

        System.out.println("Your card have been created");
        System.out.println("Your card number:");
        System.out.println(card.getNumber());
        System.out.println("Your card PIN:");
        System.out.println(card.getPinCode());
    }

    private void logInSystem() {
        System.out.println("Enter your card number:");
        String number = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String pinCode = scanner.nextLine();

        Card card = bank.cardAvailable(number, pinCode);
        if (card != null) {
            System.out.println("You have successfully logged in!");
            this.currentCard = card;
            setState(State.MAIN_AUTHORIZATION);
        } else {
            System.out.println("Wrong card number or PIN!");
        }
    }

    private void logOutSystem() {
        System.out.println("You have successfully logged out!");
        this.currentCard = null;
        setState(State.MAIN_NON_AUTHORIZATION);
    }

    private void exit() {
        isWorking = false;
        System.out.println("Bye!");
        loadCardsIntoDB();
    }

    private void getBalance() {
        if (this.currentCard == null) {
            System.out.println("Not available current card");
            return;
        }
        System.out.printf("Balance: %d", this.currentCard.getBalance());
    }

    private void loadCardsIntoBank() {
        ArrayList<RecordDB> records = managerDB.getAllCards();

        ArrayList<Card> cards = new ArrayList<>();

        for (RecordDB record : records) {
            Card card = new Card.Builder()
                    .setNumber(record.getNumber())
                    .setPinCode(record.getPin())
                    .setBalance(record.getBalance())
                    .build();

            cards.add(card);
        }

        bank.loadCards(cards);
    }

    private void loadCardsIntoDB() {

        List<Card> cards = bank.getCards();

        for (Card card : cards) {
            managerDB.addCard(card.getNumber(), card.getPinCode(), card.getBalance());
        }
    }

}

class Bank {

    private final List<Card> cards;

    Bank() {
        cards = new ArrayList<>();
    }

    public Card cardAvailable(String number, String pinCode) {
        for (Card card : cards) {
            if (card.getNumber().equals(number) && card.getPinCode().equals(pinCode)) {
                return card;
            }
        }
        return null;
    }

    public Card createCard() {
        Card newCard = new Card.Builder()
                .generateNumber()
                .generatePinCode()
                .build();

        cards.add(newCard);

        return newCard;
    }

    public void loadCards(List<Card> cards) {
        this.cards.addAll(cards);
    }

    public List<Card> getCards() {
        return cards;
    }
}

class Card {
    private final String number;
    private final String pinCode;
    private final int balance;

    private Card(String number, String pinCode, int balance) {
        this.number = number;
        this.pinCode = pinCode;
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public String getPinCode() {
        return pinCode;
    }

    public int getBalance() {
        return balance;
    }

    public static class Builder {

        private String number, pinCode;
        private int balance;
        private final Random random = new Random();

        public Builder generateNumber() {
            String number = "400000" + (1_000_000_00 + random.nextInt(8_999_999_99));
            this.number = number + getControlNumberByLuhnAlgorithm(number);

            return this;
        }

        public Builder generatePinCode() {
            this.pinCode = String.valueOf(1000 + random.nextInt(8999));
            return this;
        }

        private int getControlNumberByLuhnAlgorithm(String cardNumber) {
            int result = 0;
            for (int i = 0; i < cardNumber.length(); i++) {
                int digit = Character.getNumericValue(cardNumber.charAt(i));
                if (i % 2 == 0) {
                    int doubleDigit = digit * 2 > 9 ? digit * 2 - 9 : digit * 2;
                    result += doubleDigit;
                    continue;
                }
                result += digit;
            }
            int num = 10 - result % 10;
            return (num == 10) ? 0 : num;
        }

        public Builder setNumber(String number) {
            this.number = number;
            return this;
        }

        public Builder setPinCode(String pinCode) {
            this.pinCode = pinCode;
            return this;
        }

        public Builder setBalance(int balance) {
            this.balance = balance;
            return this;
        }

        public Card build() {
            return new Card(number, pinCode, balance);
        }

    }
}