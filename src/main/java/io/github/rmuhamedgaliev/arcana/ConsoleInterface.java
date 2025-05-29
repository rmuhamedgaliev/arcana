package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.core.GameInterface;
import io.github.rmuhamedgaliev.arcana.core.Language;

import java.util.List;
import java.util.Scanner;

/**
 * Implementation of GameInterface for console interaction.
 */
public class ConsoleInterface implements GameInterface {
    private final Scanner scanner;
    private Language currentLanguage;

    /**
     * Create a new console interface.
     */
    public ConsoleInterface() {
        this.scanner = new Scanner(System.in);
        this.currentLanguage = Language.EN;
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }

    @Override
    public int sendOptionsMessage(String message, List<String> options) {
        System.out.println(message);
        System.out.println();
        
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        
        System.out.println();
        System.out.print("Enter your choice (1-" + options.size() + "): ");
        
        int choice = -1;
        while (choice < 1 || choice > options.size()) {
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > options.size()) {
                    System.out.print("Invalid choice. Please enter a number between 1 and " + options.size() + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
        
        return choice - 1; // Convert to 0-based index
    }

    @Override
    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    @Override
    public void setCurrentLanguage(Language language) {
        this.currentLanguage = language;
    }
}
