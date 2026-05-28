import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Простой самодельный терминал (CLI) на Java.
 */
public class CoffeeTerminal {

    // Текущая рабочая директория, в которой находится терминал
    private static File currentDirectory = new File(System.getProperty("user.dir"));

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("==================================================");
        System.out.println(" Добро пожаловать в самодельный терминал, Ратибор! ");
        System.out.println(" Введи 'help' для просмотра списка базовых команд.");
        System.out.println("==================================================");

        // Главный цикл (REPL: Read-Eval-Print Loop)
        while (true) {
            try {
                // Выводим приглашение командной строки (путь > )
                System.out.print("\n" + currentDirectory.getCanonicalPath() + " > ");
                
                String input = scanner.nextLine().trim();
                
                // Если ничего не ввели, просто продолжаем
                if (input.isEmpty()) {
                    continue;
                }

                // Разбиваем ввод на саму команду и её аргументы
                String[] parts = input.split("\\s+");
                String command = parts[0];
                String[] cmdArgs = Arrays.copyOfRange(parts, 1, parts.length);

                // Обработка встроенных команд
                switch (command.toLowerCase()) {
                    case "exit":
                    case "quit":
                        System.out.println("Завершение работы терминала. До встречи!");
                        return; // Выход из программы
                        
                    case "help":
                        printHelp();
                        break;
                        
                    case "pwd":
                        // Печать текущей директории
                        System.out.println(currentDirectory.getCanonicalPath());
                        break;
                        
                    case "cd":
                        // Смена директории
                        changeDirectory(cmdArgs);
                        break;
                        
                    case "ls":
                    case "dir":
                        // Просмотр содержимого директории
                        listFiles();
                        break;
                        
                    case "echo":
                        // Вывод текста на экран
                        System.out.println(String.join(" ", cmdArgs));
                        break;
                        
                    default:
                        // Если команда не встроенная, пробуем выполнить её как системную
                        executeSystemCommand(parts);
                        break;
                }
            } catch (Exception e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
            }
        }
    }

    /**
     * Метод для смены текущей директории (аналог cd).
     */
    private static void changeDirectory(String[] args) {
        if (args.length == 0) {
            // Если аргументов нет, переходим в домашнюю папку пользователя
            currentDirectory = new File(System.getProperty("user.home"));
            return;
        }

        String path = args[0];
        File newDir = new File(path);
        
        // Если путь относительный, приклеиваем его к текущей директории
        if (!newDir.isAbsolute()) {
            newDir = new File(currentDirectory, path);
        }

        try {
            // Нормализуем путь (решает проблему с cd .. и cd .)
            newDir = newDir.getCanonicalFile();
            
            if (newDir.exists() && newDir.isDirectory()) {
                currentDirectory = newDir;
            } else {
                System.out.println("cd: нет такой директории: " + path);
            }
        } catch (IOException e) {
            System.out.println("cd: ошибка пути: " + e.getMessage());
        }
    }

    /**
     * Метод для вывода списка файлов и папок (аналог ls).
     */
    private static void listFiles() {
        File[] files = currentDirectory.listFiles();
        if (files == null) {
            System.out.println("ls: не удалось прочитать директорию.");
            return;
        }

        // Сначала выводим папки, потом файлы
        Arrays.sort(files, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        });

        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("[ПАПКА] " + file.getName());
            } else {
                System.out.println("        " + file.getName() + " (" + file.length() + " байт)");
            }
        }
    }

    /**
     * Вызов команд операционной системы (cmd/bash утилит).
     */
    private static void executeSystemCommand(String[] commandArgs) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commandArgs);
            // Задаем рабочую папку для процесса
            processBuilder.directory(currentDirectory);
            // Перенаправляем ввод/вывод процесса прямо в нашу консоль
            processBuilder.inheritIO(); 
            
            Process process = processBuilder.start();
            process.waitFor(); // Ждем завершения выполнения системной команды
            
        } catch (IOException e) {
            System.out.println("Неизвестная команда или программа не найдена: " + commandArgs[0]);
        } catch (InterruptedException e) {
            System.out.println("Процесс был прерван.");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Справка по встроенным командам.
     */
    private static void printHelp() {
        System.out.println("Доступные встроенные команды:");
        System.out.println("  cd <путь> - Сменить текущую директорию (поддерживает '..')");
        System.out.println("  ls        - Показать список файлов в текущей папке");
        System.out.println("  pwd       - Показать полный путь текущей директории");
        System.out.println("  echo <..> - Вывести текст на экран");
        System.out.println("  help      - Показать эту справку");
        System.out.println("  exit      - Выйти из терминала");
        System.out.println("Любая другая команда будет передана твоей операционной системе (Windows/Linux/macOS).");
    }
}