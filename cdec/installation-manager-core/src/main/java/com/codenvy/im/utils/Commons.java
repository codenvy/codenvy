/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.im.utils;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.dto.server.DtoFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static com.codenvy.im.utils.Version.valueOf;
import static java.lang.Thread.currentThread;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.newInputStream;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class Commons {

    /** Simplifies the way to combine paths. Takes care about normalization. */
    public static String combinePaths(String... items) {
        if (items.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(items[0]);
        for (int i = 1; i < items.length; i++) {
            if (!items[i - 1].endsWith("/")) {
                sb.append("/");
            }

            if (items[i].startsWith("/")) {
                sb.append(items[i].substring(1));
            } else {
                sb.append(items[i]);
            }
        }

        return sb.toString();
    }

    /**
     * Copies input stream to output stream, and stop copying if current thread was interrupted.
     *
     * @throws com.codenvy.im.utils.CopyStreamInterruptedException
     *         if current thread was interrupted
     */
    public static void copyInterruptable(InputStream in, OutputStream out) throws CopyStreamInterruptedException, IOException {
        byte[] buffer = new byte[8196];
        int length;
        while ((length = in.read(buffer)) != -1) {
            if (currentThread().isInterrupted()) {
                throw new CopyStreamInterruptedException("The copying was interrupted.");
            }

            out.write(buffer, 0, length);
        }
    }

    /** Adds query parameter to url. */
    public static String addQueryParam(String path, String key, String value) {
        return path + (path.contains("?") ? "&" : "?") + key + "=" + value;
    }

    /** Translates JSON to the list of DTO objects. */
    public static <DTO> List<DTO> createListDtoFromJson(String json, Class<DTO> dtoInterface) {
        return JsonUtils.createListDtoFromJson(json, dtoInterface);
    }

    /** Translates JSON to the list of DTO objects. */
    public static <DTO> DTO createDtoFromJson(String json, Class<DTO> dtoInterface) {
        return JsonUtils.createDtoFromJson(json, dtoInterface);
    }

    /** Translates JSON to object. */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonParseException {
        return JsonUtils.fromJson(json, clazz);
    }

    /** Translates JSON to object. */
    public static Map asMap(String json) throws JsonParseException {
        return JsonUtils.asMap(json);
    }

    /** Translates object to JSON without null fields and with order defined by @JsonPropertyOrder annotation above the class. */
    public static String toJson(Object obj) throws JsonProcessingException {
        return JsonUtils.jsonWriter.writeValueAsString(obj);
    }

    /** Translates object to JSON with properties sorted alphabetically and aligned by colons. */
    public static String toJsonWithSortedAndAlignedProperties(Map<String, String> map) throws JsonProcessingException {
        return JsonUtils.toJsonWithSortedAndAlignedProperties(map);
    }

    /** @return the version of the artifact out of path */
    public static String extractVersion(Path pathToBinaries) {
        return pathToBinaries.getParent().getFileName().toString();
    }

    /** @return the artifact name out of path */
    public static String extractArtifactName(Path pathToBinaries) {
        return pathToBinaries.getParent().getParent().getFileName().toString();
    }

    /**
     * Iterates over the given directory and treat every subdirectory name as a version number.
     * Returns all fetched versions numbers.
     *
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    public static TreeSet<Version> getVersionsList(Path dir) throws IOException {
        TreeSet<Version> versions = new TreeSet<>();

        if (!exists(dir)) {
            return versions;
        }

        try (DirectoryStream<Path> paths = newDirectoryStream(dir)) {
            Iterator<Path> pathIterator = paths.iterator();
            while (pathIterator.hasNext()) {
                try {
                    Path item = pathIterator.next();
                    if (isDirectory(item)) {
                        Version v = valueOf(item.getFileName().toString());
                        versions.add(v);
                    }
                } catch (IllegalVersionException e) {
                    // maybe it isn't a version directory
                }
            }
        }

        return versions;
    }

    /** Extract server url from url with path */
    public static String extractServerUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String result = url.getProtocol() + "://" + url.getHost();

            if (url.getPort() != -1) {
                result += ":" + url.getPort();
            }

            return  result;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Returns correct exception depending on initial type of exception. */
    public static IOException getProperException(IOException e, Artifact artifact) {
        if (e instanceof HttpException) {
            switch (((HttpException)e).getStatus()) {
                case 404:
                    return new ArtifactNotFoundException(artifact);

                case 302:
                    return new AuthenticationException();
            }
        }

        return e;
    }

    public static IOException getProperException(IOException e) {
        if (e instanceof HttpException) {
            switch (((HttpException)e).getStatus()) {
                case 403:
                    return new AuthenticationException();
            }
        }

        return e;
    }

    /** Calculates md5 sum */
    public static String calculateMD5Sum(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            try (InputStream in = newInputStream(file)) {
                int read;
                byte[] buffer = new byte[8192];
                while ((read = in.read(buffer)) != -1) {
                    md.update(buffer, 0, read);
                }
            }

            byte[] digest = md.digest();

            // convert the bytes to hex format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


    /** Set of artifacts to keep them in the specific order. */
    public static class ArtifactsSet extends TreeSet<Artifact> {
        public ArtifactsSet(Collection<Artifact> s) {
            super(new Comparator<Artifact>() {
                @Override
                public int compare(Artifact o1, Artifact o2) {
                    return o2.getPriority() - o1.getPriority();
                }
            });

            addAll(s);
        }
    }

    /**
     * Safety artifact creation.
     *
     * @throws ArtifactNotFoundException
     */
    @Nullable
    public static Artifact createArtifactOrNull(String artifactName) throws ArtifactNotFoundException {
        return artifactName == null ? null : createArtifact(artifactName);
    }

    /**
     * Safety version creation.
     *
     * @throws com.codenvy.im.utils.IllegalVersionException
     */
    @Nullable
    public static Version createVersionOrNull(String versionNumber) throws IllegalVersionException {
        return versionNumber == null ? null : Version.valueOf(versionNumber);
    }

    private static class JsonUtils {
        private JsonUtils() {
        }

        /** include only non null fields into json; write pretty printed json. */
        private static final ObjectWriter jsonWriter =
                new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter();

        /** Translates JSON to the list of DTO objects. */
        public static <DTO> List<DTO> createListDtoFromJson(String json, Class<DTO> dtoInterface) {
            return DtoFactory.getInstance().createListDtoFromJson(json, dtoInterface);
        }

        /** Translates JSON to the list of DTO objects. */
        public static <DTO> DTO createDtoFromJson(String json, Class<DTO> dtoInterface) {
            return DtoFactory.getInstance().createDtoFromJson(json, dtoInterface);
        }

        /** Translates JSON to object. */
        public static Map asMap(String json) throws JsonParseException {
            return fromJson(json, Map.class);
        }

        /** Translates JSON to object. */
        private static <T> T fromJson(String json, Class<T> clazz) throws JsonParseException {
            try {
                return new ObjectMapper().readValue(json, clazz);
            } catch (IOException e) {
                throw new JsonParseException(e);
            }
        }

        /** Translates map to JSON with entries sorted alphabetically and aligned by colons. */
        private static String toJsonWithSortedAndAlignedProperties(Map<String, String> originMap) throws JsonProcessingException {
            Map<String, String> map = new TreeMap<>(originMap);  // get safe copy of origin map in the sorted view

            removeNullValues(map);

            if (map.size() < 2) {
                return toJson(map);
            }

            final List<Integer> indentations = getIndentationsForEntryAlignment(map);

            PrettyPrinter prettyPrinter = new DefaultPrettyPrinter() {
                private int index;

                @Override
                public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
                    jg.writeRaw(getIndentationOfLength(indentations.get(index++)) + " : ");
                }

                @Override
                public DefaultPrettyPrinter createInstance() {
                    return this;
                }

                private String getIndentationOfLength(Integer length) {
                    return new String(new char[length]).replace("\0", " ");
                }
            };

            ObjectWriter writer = new ObjectMapper().writer().with(prettyPrinter);
            return writer.writeValueAsString(map);
        }

        private static List<Integer> getIndentationsForEntryAlignment(Map<String, String> map) {
            int longestKeyName = getLongestKeyName(map);
            List<Integer> indentations = new ArrayList<>(map.size());
            for (String key : map.keySet()) {
                int indentation = longestKeyName - key.length();
                indentations.add(indentation);
            }

            return indentations;
        }

        private static int getLongestKeyName(Map<String, String> map) {
            List<String> keys = new ArrayList(map.keySet());
            Collections.sort(keys, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s2.length() - s1.length();  // order from the longest key to the shortest
                }
            });

            return keys.get(0).length();
        }

        private static void removeNullValues(Map<String, String> map) {
            Map<String, String> temp = new HashMap<>(map);
            for (Map.Entry<String, String> entry : temp.entrySet()) {
                if (entry.getValue() == null) {
                    map.remove(entry.getKey());
                }
            }
        }
    }
}
