package accordion_symphonic.ticketing.bank.camt;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class Camt053Parser {

    public List<CamtTransaction> parse(
            InputStream inputStream
    ) {
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false
            );

            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false
            );

            factory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    ""
            );

            factory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    ""
            );

            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document =
                    factory.newDocumentBuilder()
                            .parse(inputStream);

            NodeList entries =
                    elements(document.getDocumentElement(), "Ntry");

            List<CamtTransaction> result =
                    new ArrayList<>();

            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);

                String creditDebitIndicator =
                        text(entry, "CdtDbtInd");

                if (!"CRDT".equalsIgnoreCase(
                        creditDebitIndicator
                )) {
                    continue;
                }

                String status =
                        text(entry, "Sts");

                if (!"BOOK".equalsIgnoreCase(status)) {
                    continue;
                }

                Element amountElement =
                        firstElement(entry, "Amt");

                if (amountElement == null) {
                    continue;
                }

                BigDecimal amount =
                        new BigDecimal(
                                amountElement
                                        .getTextContent()
                                        .trim()
                        );

                String currency =
                        amountElement.getAttribute("Ccy");

                String bankReference =
                        text(entry, "AcctSvcrRef");

                if (bankReference == null
                        || bankReference.isBlank()) {
                    bankReference =
                            text(entry, "NtryRef");
                }

                LocalDate bookingDate =
                        extractBookingDate(entry);

                String searchableText =
                        entry.getTextContent()
                                .replaceAll("\\s+", " ")
                                .trim();

                result.add(
                        new CamtTransaction(
                                bankReference,
                                amount,
                                currency,
                                bookingDate,
                                searchableText
                        )
                );
            }

            return result;

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "CAMT-Datei konnte nicht gelesen werden.",
                    exception
            );
        }
    }

    private LocalDate extractBookingDate(
            Element entry
    ) {
        Element bookingDate =
                firstElement(entry, "BookgDt");

        if (bookingDate == null) {
            return null;
        }

        String value =
                text(bookingDate, "Dt");

        if (value == null || value.isBlank()) {
            value = text(bookingDate, "DtTm");
        }

        if (value == null || value.length() < 10) {
            return null;
        }

        return LocalDate.parse(
                value.substring(0, 10)
        );
    }

    private String text(
            Element root,
            String localName
    ) {
        Element element =
                firstElement(root, localName);

        if (element == null) {
            return null;
        }

        return element.getTextContent().trim();
    }

    private Element firstElement(
            Element root,
            String localName
    ) {
        NodeList nodes =
                elements(root, localName);

        if (nodes.getLength() == 0) {
            return null;
        }

        return (Element) nodes.item(0);
    }

    private NodeList elements(
            Element root,
            String localName
    ) {
        NodeList namespaced =
                root.getElementsByTagNameNS(
                        "*",
                        localName
                );

        if (namespaced.getLength() > 0) {
            return namespaced;
        }

        return root.getElementsByTagName(localName);
    }
}