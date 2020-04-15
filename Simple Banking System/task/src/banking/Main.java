package banking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.show();
    }
}

class Menu {

    enum State {
        MAIN_NON_AUTHORIZATION, MAIN_AUTHORIZATION
    }

    private final Scanner scanner = new Scanner(System.in);
    private boolean isWorking = true;
    private State state = State.MAIN_NON_AUTHORIZATION;
    private final Bank bank = new Bank();
    private Card currentCard;

    private void setState(State state) {
        this.state = state;
    }

    public void show() {
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
    }

    private void getBalance() {
        if (this.currentCard == null) {
            System.out.println("Not available current card");
            return;
        }
        System.out.printf("Balance: %.0f", this.currentCard.getBalance());
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

}

class Card {
    private final String number;
    private final String pinCode;
    private float balance;

    private Card(String number, String pinCode) {
        this.number = number;
        this.pinCode = pinCode;
        this.balance = 0f;
    }

    public String getNumber() {
        return number;
    }

    public String getPinCode() {
        return pinCode;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public static class Builder {

        private String number, pinCode;
        private final Random random = new Random();

        public Builder generateNumber() {
            this.number = "400000" + (1_000_000_000 + random.nextInt(8_999_999_99) + random.nextInt(9));
            return this;
        }

        public Builder generatePinCode() {
            this.pinCode = String.valueOf(1000 + random.nextInt(8999));
            return this;
        }

        public Card build() {
            return new Card(number, pinCode);
        }

    }
}