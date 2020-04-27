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

class BankingSystem {

    enum State {
        MAIN_NON_AUTHORIZATION, MAIN_AUTHORIZATION
    }

    private final Scanner scanner = new Scanner(System.in);
    private boolean isWorking = true;
    private State state = State.MAIN_NON_AUTHORIZATION;
    private final Bank bank;
    private Card currentCard;

    BankingSystem(String fileName) {
        bank = new Bank(fileName);
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
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
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
                    addIncome();
                    break;
                case 3:
                    doTransfer();
                    break;
                case 4:
                    closeAccount();
                    break;
                case 5:
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

        Card card = bank.cardAvailable(number);
        if (card != null && pinCode.equals(card.getPinCode())) {
            System.out.println("You have successfully logged in!");
            this.currentCard = card;
            setState(State.MAIN_AUTHORIZATION);
        } else {
            System.out.println("Wrong card number or PIN!");
        }
    }

    private void logOutSystem() {
        this.currentCard = null;
        setState(State.MAIN_NON_AUTHORIZATION);
        System.out.println("You have successfully logged out!");
    }

    private void exit() {
        isWorking = false;
        System.out.println("Bye!");
    }

    private void getBalance() {
        if (this.currentCard == null) {
            System.out.println("Not available current card");
            return;
        }
        System.out.printf("Balance: %d\n", this.currentCard.getBalance());
    }

    private void addIncome() {
        System.out.println("Input income:");
        int income = Integer.parseInt(scanner.nextLine());

        currentCard.addIncome(income);
        bank.updateCard(currentCard);
        System.out.println("The income have successfully added!");
    }

    private void doTransfer() {

        System.out.println("Input number card of receiver:");
        String numberCardOfReceiver = scanner.nextLine();

        if (currentCard.getNumber().equals(numberCardOfReceiver)) {
            System.out.println("You can't transfer money to the same account!");
            return;
        }
        if (!checkCardByLuhnAlgorithm(numberCardOfReceiver)) {
            System.out.println("Probably you made mistake in card number. Please try again!");
            return;
        }

        Card cardOfReceiver = bank.cardAvailable(numberCardOfReceiver);
        if (cardOfReceiver == null) {
            System.out.println("Such a card does not exist.");
        } else {
            System.out.println("How much money you want to transfer:");
            int sumOfTransfer = Integer.parseInt(scanner.nextLine());

            currentCard.subtractMoney(sumOfTransfer);
            bank.updateCard(currentCard);

            cardOfReceiver.addIncome(sumOfTransfer);
            bank.updateCard(cardOfReceiver);

            System.out.println("You have successfully transfer!");
        }

    }

    private boolean checkCardByLuhnAlgorithm(String cardNumber) {
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
        return result % 10 == 0;
    }

    private void closeAccount() {
        bank.deleteCard(currentCard);
        this.currentCard = null;
        setState(State.MAIN_NON_AUTHORIZATION);
        System.out.println("You have successfully close account!");
    }
}

class Bank {

    private final String url;

    Bank(String fileName) {
        url = "jdbc:sqlite:./" + fileName;

        createTableCard();
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

    public void createTableCard() {
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

    public void updateCard(Card card) {
        String sql = "UPDATE card SET balance = ? WHERE number = ? and pin = ?;";

        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setInt(1, card.getBalance());
                statement.setString(2, card.getNumber());
                statement.setString(3, card.getPinCode());
                statement.execute();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Card cardAvailable(String number) {
        String sql = "SELECT number, pin, balance FROM card WHERE number = ?";

        Card card = null;

        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, number);

                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    card = new Card.Builder()
                            .setNumber(rs.getString("number"))
                            .setPinCode(rs.getString("pin"))
                            .setBalance(rs.getInt("balance"))
                            .build();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return card;
    }

    public Card createCard() {
        Card newCard = new Card.Builder()
                .generateNumber()
                .generatePinCode()
                .build();

        addCard(newCard.getNumber(), newCard.getPinCode(), newCard.getBalance());

        return newCard;
    }

    public void deleteCard(Card card) {
        String sql = "DELETE FROM card WHERE number = ? and pin = ?";

        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, card.getNumber());
                statement.setString(2, card.getPinCode());

                statement.execute();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}

class Card {
    private final String number;
    private final String pinCode;
    private int balance;

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

    public void addIncome(int income) {
        balance += income;
    }

    public void subtractMoney(int money) {
        balance -= money;
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
