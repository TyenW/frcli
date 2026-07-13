package br.com.frcli.ui;

/**
 * Classe utilitária para formatação visual do console
 */
public class UiFormatter {
    
    // Cores ANSI
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    
    // Cores
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // Cores de fundo
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    // Estilos de linha
    public static final String THIN_BORDER = "─";
    public static final String THICK_BORDER = "═";
    public static final String DOUBLE_BORDER = "═";
    
    /**
     * Cria uma linha decorativa
     */
    public static void printLine(String character, int width) {
        System.out.println(CYAN + character.repeat(width) + RESET);
    }
    
    /**
     * Exibe um título centralizado com decoração
     */
    public static void printTitle(String title) {
        int width = 60;
        printLine(THICK_BORDER, width);
        printCentered(BOLD + CYAN + title + RESET, width);
        printLine(THICK_BORDER, width);
    }
    
    /**
     * Exibe um subtítulo
     */
    public static void printSubtitle(String subtitle) {
        System.out.println("\n" + BOLD + CYAN + "▶ " + subtitle + RESET);
        printLine(THIN_BORDER, 50);
    }
    
    /**
     * Exibe uma seção com header
     */
    public static void printSection(String sectionName) {
        System.out.println("\n" + BOLD + MAGENTA + "┌─ " + sectionName + " ─" + RESET);
    }
    
    /**
     * Exibe uma seção com finalização
     */
    public static void endSection() {
        System.out.println(BOLD + MAGENTA + "└─" + RESET);
    }
    
    /**
     * Exibe um item com cor
     */
    public static void printItem(String label, Object value) {
        System.out.printf("  %s%-20s%s: %s%s%s\n", BOLD, label, RESET, BLUE, value, RESET);
    }
    
    /**
     * Exibe um item com ícone
     */
    public static void printBullet(String text) {
        System.out.printf("  %s●%s %s\n", YELLOW, RESET, text);
    }
    
    /**
     * Exibe um item com sucesso
     */
    public static void printSuccess(String message) {
        System.out.printf("%s✓ %s%s\n", GREEN + BOLD, message, RESET);
    }
    
    /**
     * Exibe um aviso
     */
    public static void printWarning(String message) {
        System.out.printf("%s⚠ %s%s\n", YELLOW + BOLD, message, RESET);
    }
    
    /**
     * Exibe um erro
     */
    public static void printError(String message) {
        System.out.printf("%s✗ %s%s\n", RED + BOLD, message, RESET);
    }
    
    /**
     * Exibe informação
     */
    public static void printInfo(String message) {
        System.out.printf("%sℹ %s%s\n", BLUE + BOLD, message, RESET);
    }
    
    /**
     * Centraliza um texto
     */
    public static void printCentered(String text, int width) {
        int cleanLength = stripAnsi(text).length();
        int padding = Math.max(0, (width - cleanLength) / 2);
        System.out.println(" ".repeat(padding) + text);
    }
    
    /**
     * Remove códigos ANSI de uma string
     */
    public static String stripAnsi(String text) {
        return text.replaceAll("\u001B\\[[0-9;]*m", "");
    }
    
    /**
     * Formata um valor com cor baseado em seu status
     */
    public static String formatValue(double value, double max) {
        if (value <= 0) {
            return RED + "0" + RESET;
        } else if (value < max * 0.5) {
            return YELLOW + String.format("%.1f", value) + RESET;
        } else {
            return GREEN + String.format("%.1f", value) + RESET;
        }
    }
    
    /**
     * Exibe uma barra de progresso
     */
    public static void printProgressBar(double current, double max) {
        int barWidth = 30;
        int filled = (int) ((current / max) * barWidth);
        
        String bar = GREEN + "█".repeat(Math.max(0, filled)) + 
                    YELLOW + "░".repeat(Math.max(0, barWidth - filled)) + 
                    RESET;
        
        System.out.printf("[%s] %.1f / %.1f\n", bar, current, max);
    }
    
    /**
     * Exibe uma linha em branco com espaçamento
     */
    public static void printBlank() {
        System.out.println();
    }
}
