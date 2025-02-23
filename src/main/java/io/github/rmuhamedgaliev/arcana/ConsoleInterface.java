package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.core.GameInterface;

import java.util.Scanner;

public class ConsoleInterface implements GameInterface {
    private Scanner scanner;

    public ConsoleInterface() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }

    @Override
    public String receiveInput() {
        return scanner.nextLine();
    }
}
