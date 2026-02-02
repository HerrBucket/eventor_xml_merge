package home;

import org.w3c.dom.Document;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;

public class CommandHandler {

    private Scanner input;
    private Reader reader;

    public CommandHandler() {}
    public CommandHandler(Scanner scanner) {
        input = scanner;
        reader = new Reader();
    }

    public void printCommands() {
        System.out.println("Available commands:");
        System.out.println("\tmerge\t\t:\tMerge multiple xml files into one.");
        System.out.println("\tgenMasterSI\t:\tGenerate a master file for mapping an SI with a competitor.");
        System.out.println("\tupdateSI\t:\tupdate xml with SI map file.");
        System.out.println("\thelp\t\t:\tPrints this list of commands.");
        System.out.println("\texit\t\t:\tClose program");
    }

    public void handleCommand(String input) {
        if (input.equalsIgnoreCase("merge")) {
            startMerge();
        } else if (input.equalsIgnoreCase("genMasterSI")) {
            genSI();
        } else if (input.equalsIgnoreCase("updateSI")) {
            updateSI();
        } else if (input.equalsIgnoreCase("help")) {
            printCommands();
        } else if (input.equalsIgnoreCase("exit")) {
            System.out.println("Exiting...");
            System.exit(0);
        } else {
            System.out.println("Unrecognised command!");
        }
    }

    private List<File> openFileChooserMulti() {
        JDialog wrapper = new JDialog((Window) null);
        wrapper.setAlwaysOnTop(true);
        wrapper.setLocation(400,400);
        wrapper.setVisible(true);

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("./"));
        chooser.setMultiSelectionEnabled(true);
        chooser.showOpenDialog(wrapper);

        List<File> files = List.of(chooser.getSelectedFiles());

        wrapper.dispose();
        return files;
    }

    private File openFileChooser() {
        JDialog wrapper = new JDialog((Window) null);
        wrapper.setAlwaysOnTop(true);
        wrapper.setLocation(400,400);
        wrapper.setVisible(true);

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("./"));
        chooser.setMultiSelectionEnabled(false);
        chooser.showOpenDialog(wrapper);

        File files = chooser.getSelectedFile();

        wrapper.dispose();
        return files;
    }

    private void startMerge() {
        System.out.println("Select files to merge.");

        List<File> files = openFileChooserMulti();
        if(files.isEmpty())
            return;

        try {
            System.out.println("Selected files:");
            List<Document> docs = files.stream().peek(f -> System.out.println("\t"+f.getName())).map(f -> reader.parse(f)).collect(Collectors.toList());
            final Document out = reader.createNewDocument(docs.get(0));

            System.out.println("Is this relay");
            Boolean relay = "yes".equalsIgnoreCase(input.nextLine());

            docs.stream().forEach(d -> {
                try {
                    reader.copyDocumentData(d, out, false, relay);
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
            });

            System.out.println("Enter name of output file");
            String outputName = input.nextLine();
            reader.writeOutXml(out, outputName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Merge complete.");
    }

    private void genSI() {
        System.out.println("Select xml with competitors");
        File competitors = openFileChooser();
        if(competitors == null) {
            System.out.println("Could not open specified file");
            return;
        }

        System.out.println("Select list of SI numbers");
        File list = openFileChooser();
        if(list == null) {
            System.out.println("Could not open specified file");
            return;
        }

        System.out.println("Enter name of output file");
        String outputName = input.nextLine();

        List<String> siNumber = reader.loadSIList(list);
        List<String> compIDs = reader.getCompetitorList(competitors);
        Map<String, String> siMap = new HashMap<>();
        int i = 0;
        for(String s : compIDs) {
            if(i<siNumber.size())
                siMap.put(s,siNumber.get(i));
            i++;
        }

        reader.writeMappingFile(siMap, outputName);

        System.out.println("Master SI-Competitor mapping created");
    }

    private void updateSI() {
        System.out.println("Select master map file");
        File master = openFileChooser();
        if(master == null) {
            System.out.println("Could not open specified file");
            return;
        }

        System.out.println("Select file to update");
        File xml = openFileChooser();
        if(xml == null) {
            System.out.println("Could not open specified file");
            return;
        }

        System.out.println("Enter name of output file");
        String outputName = input.nextLine();

        Map<String, String> siMap = reader.loadSIMapping(master);
        Document updatedDoc = reader.updateSINumbers(reader.parse(xml), siMap);
        reader.writeOutXml(updatedDoc, outputName);

        System.out.println("SI numbers updated");
    }
}