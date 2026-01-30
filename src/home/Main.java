package home;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandHandler handler = new CommandHandler(scanner);

        System.out.println("Welcome to the Eventor Xml Merger");
        handler.printCommands();

        while (true) {
            System.out.println("Enter command...");
            String input = scanner.nextLine();
            handler.handleCommand(input);
        }
    }
}