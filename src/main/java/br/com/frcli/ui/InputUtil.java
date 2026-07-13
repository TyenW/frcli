package br.com.frcli.ui;

import java.util.Scanner;

public class InputUtil {
    private static final Scanner scanner = new Scanner(System.in);

    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static String readStringNotEmpty(String prompt) {
        while (true) {
            String value = readString(prompt).trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Erro: Entrada não pode ser vazia!");
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Erro: Por favor, insira um número inteiro válido!");
            }
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.printf("Erro: Por favor, insira um valor entre %d e %d!\n", min, max);
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Double.parseDouble(input.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Erro: Por favor, insira um número decimal válido!");
            }
        }
    }

    public static double readDouble(String prompt, double min, double max) {
        while (true) {
            double value = readDouble(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.printf("Erro: Por favor, insira um valor entre %.2f e %.2f!\n", min, max);
        }
    }

    public static boolean readBoolean(String prompt) {
        while (true) {
            String val = readString(prompt).trim().toLowerCase();
            if (val.equals("s") || val.equals("sim") || val.equals("y") || val.equals("yes")) {
                return true;
            }
            if (val.equals("n") || val.equals("nao") || val.equals("não") || val.equals("no")) {
                return false;
            }
            System.out.println("Erro: Por favor, responda com Sim (s) ou Não (n)!");
        }
    }

    public static void pressEnterToContinue() {
        System.out.println("\nPressione Enter para continuar...");
        scanner.nextLine();
    }

    public static Double readOptionalDouble(String prompt) {
        while (true) {
            System.out.print(prompt + " (ou Enter para não alterar): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(input.replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Erro: Por favor, insira um número decimal válido ou deixe vazio!");
            }
        }
    }

    public static Integer readOptionalInt(String prompt) {
        while (true) {
            System.out.print(prompt + " (ou Enter para não alterar): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Erro: Por favor, insira um número inteiro válido ou deixe vazio!");
            }
        }
    }

    public static Boolean readOptionalBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " (s/n, ou Enter para não alterar): ");
            String val = scanner.nextLine().trim().toLowerCase();
            if (val.isEmpty()) {
                return null;
            }
            if (val.equals("s") || val.equals("sim") || val.equals("y") || val.equals("yes")) {
                return true;
            }
            if (val.equals("n") || val.equals("nao") || val.equals("não") || val.equals("no")) {
                return false;
            }
            System.out.println("Erro: Por favor, responda com Sim (s), Não (n) ou deixe vazio!");
        }
    }
}
