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
        System.out.println("Hello World");
        System.out.println("To start merging type 'start' and press enter");

        Reader r = new Reader();

        List<String> files = new ArrayList<>();
        System.out.println("Enter path to xml file and press enter:");
        while (!scanner.hasNext("start")) {
            String line = scanner.nextLine();
            line = line.contains("\"") ? line.replace("\"", "") : line;
            System.out.print("Validating file...");
            if(Reader.isValidPath(line)) {
                System.out.println("ok");
                files.add(line);
            } else {
                System.out.println("not found");
            }
            System.out.println("Enter path to xml file and press enter:");
        }

        try {
            List<Document> docs = files.stream().map(f -> r.parse(f)).collect(Collectors.toList());
            final Document out = r.createNewDocument(docs.get(0));

            docs.stream().forEach(d -> {
                try {
                    r.copyDocumentData(d, out, false);
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
            });
            r.writeOutXml(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Merge complete, enter path to SI-number mapping (type 'skip' to skip this step):");
        String siList = scanner.nextLine();
        while (true) {
            if (!siList.equalsIgnoreCase("skip") && !Reader.isValidPath(siList)) {
                System.out.println("Map file not found, try again");
                siList = scanner.nextLine();
            } else {
                break;
            }
        }

        if(!siList.equalsIgnoreCase("skip")) {
            Map<String, String> siMap = r.loadSIMapping(siList);
            Document updatedDoc = r.updateSINumbers(r.parse("new.xml"), siMap);
            r.writeOutXml(updatedDoc, "new_with_SI.xml");

            System.out.println("SI numbers updated, press enter to exit");
        } else {
            System.out.println("Done, press enter to exit");
        }
        String exit = scanner.nextLine();
        System.out.println("exiting...");
    }
}