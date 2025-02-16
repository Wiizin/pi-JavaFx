package io.github.palexdev.materialfx.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TournamentRegulationsGenerator {

    private static final Random random = new Random();

    // Lists of possible values for regulations
    private static final String[] FORMATS = {"Single Elimination", "Double Elimination", "Round Robin", "Swiss System", "Group Stage"};
    private static final String[] RULES = {
            "All matches must start on time. Late arrivals may result in disqualification.",
            "Players must bring their own equipment (e.g., rackets, balls).",
            "No outside coaching is allowed during matches.",
            "All disputes will be resolved by the tournament referee.",
            "Matches are best of 3 sets. Finals are best of 5 sets.",
            "Players must wear appropriate sports attire.",
            "Any form of cheating will result in immediate disqualification.",
            "The tournament organizer reserves the right to modify the schedule.",
            "All participants must sign a waiver before playing.",
            "Prize money will be distributed as follows: 1st place - 60%, 2nd place - 30%, 3rd place - 10%."
    };
    private static final String[] SCORING_SYSTEMS = {"Standard", "Point-a-Rally", "First to 21", "Best of 5 Sets", "Timed Matches"};
    private static final String[] PENALTIES = {
            "Warning for first offense",
            "Point deduction for second offense",
            "Game forfeiture for repeated offenses",
            "Disqualification for severe violations"
    };

    public static void main(String[] args) {
        System.out.println(generateRandomRegulations());
    }

    public static String generateRandomRegulations() {
        StringBuilder regulations = new StringBuilder();

        // Random format
        regulations.append("Tournament Format: ").append(FORMATS[random.nextInt(FORMATS.length)]).append("\n\n");

        // Random rules
        regulations.append("Rules and Regulations:\n");
        List<String> selectedRules = getRandomSubset(RULES, 3 + random.nextInt(3)); // Select 3-5 random rules
        for (int i = 0; i < selectedRules.size(); i++) {
            regulations.append((i + 1)).append(". ").append(selectedRules.get(i)).append("\n");
        }
        regulations.append("\n");

        // Random scoring system
        regulations.append("Scoring System: ").append(SCORING_SYSTEMS[random.nextInt(SCORING_SYSTEMS.length)]).append("\n\n");

        // Random penalties
        regulations.append("Penalties:\n");
        List<String> selectedPenalties = getRandomSubset(PENALTIES, 2 + random.nextInt(2)); // Select 2-3 random penalties
        for (int i = 0; i < selectedPenalties.size(); i++) {
            regulations.append((i + 1)).append(". ").append(selectedPenalties.get(i)).append("\n");
        }

        return regulations.toString();
    }

    private static List<String> getRandomSubset(String[] array, int count) {
        List<String> subset = new ArrayList<>();
        List<String> copy = new ArrayList<>(List.of(array));
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(copy.size());
            subset.add(copy.remove(index));
        }
        return subset;
    }
}