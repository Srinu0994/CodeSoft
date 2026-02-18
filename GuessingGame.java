import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GuessingGame {
    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Number Guessing Game (Java)");

        int min = getInt("Minimum value [1]: ", 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        int defaultMax = (min < 100) ? 100 : min + 10;
        int max = getInt(String.format("Maximum value [%d]: ", defaultMax), defaultMax, min, Integer.MAX_VALUE);
        int maxAttempts = getInt("Max attempts per round (0 for unlimited) [0]: ", 0, 0, Integer.MAX_VALUE);

        int roundsPlayed = 0;
        int roundsWon = 0;
        int totalAttempts = 0;

        Deck deck = new Deck();

        while (true) {
            roundsPlayed++;
            RoundResult res = playRound(min, max, maxAttempts, deck);
            totalAttempts += res.attempts;
            if (res.won) roundsWon++;

            System.out.print("Play again? (y/N): ");
            String again = SC.nextLine().trim().toLowerCase();
            if (!again.equals("y")) break;
        }

        printSummary(roundsPlayed, roundsWon, totalAttempts);
    }

    private static class RoundResult {
        final boolean won;
        final int attempts;

        RoundResult(boolean won, int attempts) {
            this.won = won;
            this.attempts = attempts;
        }
    }

    private static class Card {
        final CardType type;
        final String name;
        final String desc;

        Card(CardType type, String name, String desc) {
            this.type = type;
            this.name = name;
            this.desc = desc;
        }
    }

    private enum CardType {
        EXTRA_ATTEMPTS,
        NARROW_RANGE,
        REVEAL_PARITY,
        REVEAL_WINDOW,
        SWAP_TARGET
    }

    private static class Deck {
        private final List<Card> cards = new ArrayList<>();
        private final Random rnd = new Random();

        Deck() {
            refill();
            shuffle();
        }

        private void refill() {
            cards.clear();
            cards.add(new Card(CardType.EXTRA_ATTEMPTS, "Extra Attempts", "Gain +2 attempts this round."));
            cards.add(new Card(CardType.NARROW_RANGE, "Narrow Range", "The guessing range is narrowed around the secret number."));
            cards.add(new Card(CardType.REVEAL_PARITY, "Reveal Parity", "Tells whether the number is even or odd."));
            cards.add(new Card(CardType.REVEAL_WINDOW, "Reveal Window", "Gives a small window where the number lies."));
            cards.add(new Card(CardType.SWAP_TARGET, "Swap Target", "The secret number is swapped for a new random one."));
        }

        private void shuffle() {
            Collections.shuffle(cards, rnd);
        }

        Card draw() {
            if (cards.isEmpty()) {
                refill();
                shuffle();
            }
            return cards.remove(cards.size() - 1);
        }
    }

    private static RoundResult playRound(int min, int max, int maxAttempts, Deck deck) {
        Random rnd = new Random();
        int target = rnd.nextInt(max - min + 1) + min;
        int attempts = 0;

        Card card = deck.draw();
        System.out.printf("You drew a card: %s - %s%n", card.name, card.desc);

        switch (card.type) {
            case EXTRA_ATTEMPTS:
                if (maxAttempts > 0) {
                    maxAttempts += 2;
                    System.out.println("You received +2 attempts this round.");
                } else {
                    System.out.println("You have unlimited attempts; card has no effect.");
                }
                break;
            case NARROW_RANGE:
                int window = Math.max(1, (max - min) / 4);
                min = Math.max(min, target - window);
                max = Math.min(max, target + window);
                System.out.printf("Range narrowed to %d - %d this round.%n", min, max);
                break;
            case REVEAL_PARITY:
                System.out.println("Hint: The number is " + (target % 2 == 0 ? "even." : "odd."));
                break;
            case REVEAL_WINDOW:
                int w = Math.max(1, (max - min) / 10 + 2);
                int low = Math.max(min, target - w);
                int high = Math.min(max, target + w);
                System.out.printf("Hint: it's between %d and %d.%n", low, high);
                break;
            case SWAP_TARGET:
                target = rnd.nextInt(max - min + 1) + min;
                System.out.println("The secret number was secretly swapped by the card!");
                break;
        }

        while (true) {
            attempts++;
            int guess = getInt(String.format("Guess a number between %d and %d: ", min, max), null, min, max);
            if (guess < target) {
                System.out.println("Too low.");
            } else if (guess > target) {
                System.out.println("Too high.");
            } else {
                System.out.printf("Correct! You guessed it in %d attempt%s.%n", attempts, attempts > 1 ? "s" : "");
                return new RoundResult(true, attempts);
            }

            if (maxAttempts > 0 && attempts >= maxAttempts) {
                System.out.printf("Out of attempts! The number was %d.%n", target);
                return new RoundResult(false, attempts);
            }
        }
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int getInt(String prompt, Integer defaultValue, int minAllowed, int maxAllowed) {
        while (true) {
            System.out.print(prompt);
            String line = SC.nextLine();
            Integer v = parseIntOrNull(line);
            if (v == null) {
                if (defaultValue != null) return defaultValue;
                System.out.println("Please enter a valid integer.");
                continue;
            }
            if (v < minAllowed) {
                System.out.printf("Value must be >= %d.%n", minAllowed);
                continue;
            }
            if (v > maxAllowed) {
                System.out.printf("Value must be <= %d.%n", maxAllowed);
                continue;
            }
            return v;
        }
    }

    private static void printSummary(int roundsPlayed, int roundsWon, int totalAttempts) {
        System.out.println("\nGame Summary");
        System.out.printf("Rounds played: %d%n", roundsPlayed);
        System.out.printf("Rounds won: %d%n", roundsWon);
        System.out.printf("Total attempts: %d%n", totalAttempts);
        if (roundsWon > 0) {
            System.out.printf("Average attempts per win: %.2f%n", (double) totalAttempts / roundsWon);
        }
        System.out.println("Thanks for playing!");
    }
}
