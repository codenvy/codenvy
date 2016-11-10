/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.plugin.webhooks.connectors;

import com.google.common.io.CharStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Jenkins implementation of {@link Connector}
 * One {@link JenkinsConnector} is configured for one Jenkins job
 *
 * @author Stephane Tournie
 */
public class JenkinsConnector implements Connector {

    private static final Logger LOG = LoggerFactory.getLogger(JenkinsConnector.class);

    // The name of the Jenkins job
    private final String jobName;
    // The URL of the XML configuration of the Jenkins job
    private final String jobConfigXmlUrl;

    /**
     * Constructor
     *
     * @param url
     *         the URL of the Jenkins instance to connect to
     * @param jobName
     *         the name of the Jenkins job
     */
    public JenkinsConnector(final String url, final String jobName) {
        this.jobName = jobName;
        this.jobConfigXmlUrl = url + "/job/" + jobName + "/config.xml";
    }

    /**
     * Add a factory link to configured Jenkins job
     *
     * @param factoryUrl
     *         the factory URL to add
     */
    @Override
    public void addFactoryLink(String factoryUrl) {
        Optional<String> jobConfigXml = getCurrentJenkinsJobConfiguration();
        jobConfigXml.ifPresent(xml -> {
            Optional<Document> configDocument = xmlToDocument(xml);
            configDocument.ifPresent(doc -> {
                Element root = doc.getDocumentElement();
                Node descriptionNode = root.getElementsByTagName("description").item(0);

                if (!descriptionNode.getTextContent().contains(factoryUrl)) {
                    updateJenkinsJobDescription(factoryUrl, doc, descriptionNode);
                } else {
                    LOG.debug("factory link {} already displayed on description of Jenkins job {}", factoryUrl, jobName);
                }
            });
        });
    }

    protected Optional<String> getCurrentJenkinsJobConfiguration() {
        try {
            URL url = new URL(jobConfigXmlUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            try {
                if (url.getUserInfo() != null) {
                    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(url.getUserInfo().getBytes()));
                    connection.setRequestProperty("Authorization", basicAuth);
                }
                connection.setRequestMethod("GET");
                connection.addRequestProperty(HttpHeaders.CONTENT_TYPE, APPLICATION_XML);
                final int responseCode = connection.getResponseCode();
                if ((responseCode / 100) != 2) {
                    InputStream in = connection.getErrorStream();
                    if (in == null) {
                        in = connection.getInputStream();
                    }
                    final String str;
                    try (Reader reader = new InputStreamReader(in)) {
                        str = CharStreams.toString(reader);
                    }
                    throw new IOException(str);
                }
                try (Reader reader = new InputStreamReader(connection.getInputStream())) {
                    return Optional.of(CharStreams.toString(reader));
                }

            } catch (IOException e) {
                LOG.error("Can't get Jenkins job configuration", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        } catch (IOException e) {
            LOG.error("Can't get Jenkins job configuration", e);
        }
        return Optional.empty();
    }

    protected void updateJenkinsJobDescription(String factoryUrl, Document configDocument, Node descriptionNode) {
        String descriptionContent = descriptionNode.getTextContent();
        descriptionNode.setTextContent(descriptionContent + "\n" + "<a href=\"" + factoryUrl + "\">" + factoryUrl + "</a>");
        String updatedJobConfigXml = documentToXml(configDocument);
        try {
            URL url = new URL(jobConfigXmlUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            try {
                if (url.getUserInfo() != null) {
                    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(url.getUserInfo().getBytes()));
                    connection.setRequestProperty("Authorization", basicAuth);
                }
                connection.setRequestMethod("POST");
                connection.addRequestProperty(HttpHeaders.CONTENT_TYPE, APPLICATION_XML);
                connection.addRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                connection.setDoOutput(true);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(updatedJobConfigXml.getBytes());
                }
                final int responseCode = connection.getResponseCode();
                if ((responseCode / 100) != 2) {
                    InputStream in = connection.getErrorStream();
                    if (in == null) {
                        in = connection.getInputStream();
                    }
                    final String str;
                    try (Reader reader = new InputStreamReader(in)) {
                        str = CharStreams.toString(reader);
                    }
                    LOG.error(str);
                } else {
                    LOG.debug("factory link {} successfully added on description of Jenkins job ", factoryUrl, jobName);
                }
            } catch (IOException e) {
                LOG.error("Can't get Jenkins job configuration", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        } catch (IOException e) {
            LOG.error("Can't get Jenkins job configuration", e);
        }
    }

    protected String documentToXml(Document configDocument) {
        DOMSource domSource = new DOMSource(configDocument);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    protected Optional<Document> xmlToDocument(String jobConfigXml) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new ByteArrayInputStream(jobConfigXml.getBytes("utf-8")));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(document);
    }
}

