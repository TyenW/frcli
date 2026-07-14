package br.com.frcli.ui;

import java.util.*;

/**
 * Classe utilitária para construção de menus formatados no console
 */
public class MenuBuilder {
    
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    
    private String title;
    private List<MenuItem> items;
    private boolean showBorder;
    private int borderWidth;
    
    public MenuBuilder(String title) {
        this.title = title;
        this.items = new ArrayList<>();
        this.showBorder = true;
        this.borderWidth = 60;
    }
    
    public MenuBuilder addItem(int option, String description) {
        items.add(new MenuItem(option, description, null));
        return this;
    }
    
    public MenuBuilder addItem(int option, String description, String detail) {
        items.add(new MenuItem(option, description, detail));
        return this;
    }
    
    public MenuBuilder addSeperator() {
        items.add(new MenuItem(-1, "---", null));
        return this;
    }
    
    public MenuBuilder setBorderWidth(int width) {
        this.borderWidth = width;
        return this;
    }
    
    public MenuBuilder setShowBorder(boolean show) {
        this.showBorder = show;
        return this;
    }
    
    public void display() {
        if (showBorder) {
            printBorder();
        }
        
        // Título
        printCentered(BOLD + CYAN + title + RESET, borderWidth);
        
        if (showBorder) {
            printBorder();
        }
        
        // Itens
        for (MenuItem item : items) {
            if (item.option == -1) {
                printBorder();
            } else {
                printItem(item);
            }
        }
        
        if (showBorder) {
            printBorder();
        }
    }
    
    public int getUserChoice(int minOption, int maxOption) {
        display();
        return InputUtil.readInt(BOLD + CYAN + "➜ Selecione uma opção: " + RESET, minOption, maxOption);
    }
    
    private void printItem(MenuItem item) {
        String option = BOLD + YELLOW + String.format("%2d", item.option) + RESET;
        String description = GREEN + item.description + RESET;
        
        if (item.detail != null && !item.detail.isEmpty()) {
            System.out.printf("%s. %-40s %s(%s)%s\n", option, description, BLUE, item.detail, RESET);
        } else {
            System.out.printf("%s. %s\n", option, description);
        }
    }
    
    private void printBorder() {
        System.out.println(CYAN + "═".repeat(borderWidth) + RESET);
    }
    
    private void printCentered(String text, int width) {
        int padding = (width - stripAnsi(text).length()) / 2;
        System.out.println(" ".repeat(Math.max(0, padding)) + text);
    }
    
    private static String stripAnsi(String text) {
        return text.replaceAll("\u001B\\[[0-9;]*m", "");
    }
    
    private static class MenuItem {
        int option;
        String description;
        String detail;
        
        MenuItem(int option, String description, String detail) {
            this.option = option;
            this.description = description;
            this.detail = detail;
        }
    }
}
