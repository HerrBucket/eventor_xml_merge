package home;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Reader {
    public Reader() {}

    public List<String> loadSIList(File siList) {
        List<String> out = new ArrayList<>();
        try {
            Scanner sc = new Scanner(siList);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                out.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
    }

    public Map<String, String> loadSIMapping(String path) {
        File mapFile = Paths.get(path).toFile();
        return loadSIMapping(mapFile);
    }
    public Map<String, String> loadSIMapping(File mapFile) {
        Map<String, String> map = new HashMap<>();
        try {
            Scanner sc = new Scanner(mapFile);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String id = line.split(":")[0];
                String si = line.split(":")[1];
                map.put(id, si);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return map;
    }

    public List<String> getCompetitorList(File xml) {
        List<String> out = new ArrayList<>();

        Document doc = parse(xml);
        Node root = doc.getElementsByTagName("EntryList").item(0);
        List<Node> personEntries = findChildren(root, "PersonEntry");
        personEntries.stream().forEach(n -> {
            Node person = findChildren(n, "Person").stream().findFirst().orElse(null);
            Node idNode = findChildren(person, "Id").stream().findFirst().orElse(null);
            if(idNode != null && !idNode.getTextContent().isEmpty()) {
                out.add(idNode.getTextContent());
            }
        });

        return out;
    }

    public Document parse(String filepath) {
        try {
            // Specify the file path as a File object
            File xmlFile = Paths.get(filepath).toFile();
            return parse(xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document parse(File xmlFile) {
        try {
            // Create a DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file
            Document document = builder.parse(xmlFile);

            return document;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document createNewDocument(Document from) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document newDoc = builder.newDocument();
        Element root = newDoc.createElement("EntryList");
        NamedNodeMap map = from.getFirstChild().getAttributes();
        for(int i = 0; i < map.getLength(); i++) {
            Node n = map.item(i);
            root.setAttribute(n.getNodeName(), n.getNodeValue());
        }
        newDoc.appendChild(root);

        copyContent(from.getElementsByTagName("EntryList").item(0).getChildNodes(), root, newDoc, true, false);

        return newDoc;
    }

    public void copyDocumentData(Document from, Document to, boolean copyall, boolean isRelay) throws ParserConfigurationException {

        copyContent(from.getElementsByTagName("EntryList").item(0).getChildNodes(), to.getFirstChild(), to, copyall, isRelay);

    }

    public static boolean isValidPath(String path) {
        try {
            return Paths.get(path).toFile().exists();
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
    }

    public void writeOutXml(Document xmlDoc) {
        writeOutXml(xmlDoc, "new.xml");
    }
    public void writeOutXml(Document xmlDoc, String path) {
        try {
            if(!path.endsWith(".xml"))
                path += ".xml";
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xmlDoc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public void writeMappingFile(Map<String, String> map, String path) {
        if(!path.endsWith(".txt"))
            path += ".txt";
        Path filePath = Paths.get(path);
        try {
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
            List<String> list = map.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());
            for (String str : list) {
                Files.writeString(filePath, str + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyContent(NodeList list, Node root, Document newXml, boolean copyEvent, boolean isRelay) {
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            if(copyEvent) {
                if(node.getNodeType() == Node.ELEMENT_NODE && (node.getNodeName() == "Event")) {
                    Node newNode = node.cloneNode(true);
                    newXml.adoptNode(newNode);
                    root.appendChild(newNode);
                }
            } else if(isRelay && node.getNodeType() == Node.ELEMENT_NODE && (node.getNodeName() == "TeamEntry")) {
                Node newNode = node.cloneNode(true);
                newXml.adoptNode(newNode);
                root.appendChild(newNode);
            } else if(node.getNodeType() == Node.ELEMENT_NODE && (node.getNodeName() == "PersonEntry")) {
                Node newNode = node.cloneNode(true);
                newXml.adoptNode(newNode);
                root.appendChild(newNode);
            }
        }
    }

    private List<Node> findChildren(Node root, String name) {
        List<Node> out = new ArrayList<>();
        if(root != null) {
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(name))
                    out.add(n);
            }
        }
        return out;
    }

    public Document updateSINumbers(Document doc, Map<String, String> map) {
        map.forEach((k,v) -> {
            System.out.println(k + " : " + v);
            Node root = doc.getElementsByTagName("EntryList").item(0);
            List<Node> personEntries = findChildren(root, "PersonEntry");
            personEntries.stream().forEach(n -> {
                Node person = findChildren(n, "Person").stream().findFirst().orElse(null);
                Node id = findChildren(person, "Id").stream().findFirst().orElse(null);
                if(id != null && id.getTextContent().equals(k)) {
                    System.out.println("Updating id " + id.getTextContent());
                    Node card = findChildren(n, "ControlCard").stream().findFirst().orElse(null);
                    Element newCard = doc.createElement("ControlCard");
                    newCard.setAttribute("punchingSystem", "SI");
                    newCard.setTextContent(v);
                    n.insertBefore(newCard, card);
                }
            });
        });
        return doc;
    }
}