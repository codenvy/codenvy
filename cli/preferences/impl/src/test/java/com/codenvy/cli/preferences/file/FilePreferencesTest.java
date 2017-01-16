/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.preferences.file;

import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.codenvy.cli.preferences.file.FakePojo.DUMB_POJO_NAME;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Stéphane Daviet
 */
public class FilePreferencesTest {
    private static final String USELESS_POJO_KEY = "uselessPojo";


    protected FilePreferences getPreferencesFile(String resourceName) throws IOException {

        File tempPreferencesFile = File.createTempFile("preferencesTest", ".json");
        tempPreferencesFile.deleteOnExit();
        try (InputStream is = FilePreferencesTest.class.getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalStateException("Unable to find the resource '" + resourceName + "'.");
            }
            Files.copy(is, tempPreferencesFile.toPath(), REPLACE_EXISTING);
        }

        // disable the save
        return new FilePreferences(tempPreferencesFile).setDisableSaveOnChanges();
    }

    @Test
    public void testPerfectPojoMatchPreferences() throws IOException {
        FilePreferences filePreferences = getPreferencesFile("perfectPojoMatchPreferences.json");

        FakePojo fakePojo = filePreferences.get(USELESS_POJO_KEY, FakePojo.class);

        assertThat(FakePojo.getDumbInstance()).isEqualToComparingFieldByField(fakePojo);
    }

    @Test
    public void testMorePropertiesThanPojoPreferences() throws IOException {
        FilePreferences filePreferences = getPreferencesFile("morePropertiesThanPojoPreferences.json");

        FakePojo fakePojo = filePreferences.get(USELESS_POJO_KEY, FakePojo.class);

        assertThat(FakePojo.getDumbInstance()).isEqualToComparingFieldByField(fakePojo);
    }

    @Test
    public void testTwoConsumersWithDifferentsPojo() throws IOException {
        FilePreferences filePreferences = getPreferencesFile("morePropertiesThanPojoPreferences.json");

        FakePojo fakePojo = filePreferences.get(USELESS_POJO_KEY, FakePojo.class);

        assertThat(FakePojo.getDumbInstance()).isEqualToComparingFieldByField(fakePojo);

        OtherPojo otherPojo = filePreferences.get(USELESS_POJO_KEY, OtherPojo.class);

        assertThat(OtherPojo.getInstance().withName(DUMB_POJO_NAME)).isEqualToComparingFieldByField(otherPojo);
    }

    @Test
    public void testLessPropertiesThanPojoPreferences() throws IOException {
        FilePreferences filePreferences = getPreferencesFile("lessPropertiesThanPojoPreferences.json");
        FakePojo fakePojo = filePreferences.get(USELESS_POJO_KEY, FakePojo.class);
        assertThat(FakePojo.getDumbInstance().withName(null)).isEqualToComparingFieldByField(fakePojo);
    }

    @Test
    public void testDeeperPerfectMatchPreferences() throws IOException {
        FilePreferences filePreferences = getPreferencesFile("deeperPerfectPojoMatchPreferences.json");

        FakePojo fakePojo = filePreferences.path("intermediateNode")
                                              .get(USELESS_POJO_KEY, FakePojo.class);

        assertThat(FakePojo.getDumbInstance()).isEqualToComparingFieldByField(fakePojo);
    }

    @Test
    public void testPutOneThenGetOther() throws IOException {
        File tempPreferencesFile = File.createTempFile("preferencesTest", ".json");
        tempPreferencesFile.deleteOnExit();
        FilePreferences filePreferences = new FilePreferences(tempPreferencesFile).setDisableSaveOnChanges();

        filePreferences.put(USELESS_POJO_KEY, FakePojo.getDumbInstance());
        OtherPojo otherPojo = filePreferences.get(USELESS_POJO_KEY, OtherPojo.class);

        assertThat(OtherPojo.getInstance()
                            .withName(DUMB_POJO_NAME)
                            .withAnotherProperty(null)).isEqualToComparingFieldByField(otherPojo);
    }

    @Test
    public void testMergeWithNoPropertyOverwrite() throws IOException {
        FilePreferences filePreferences = getPreferencesFile("perfectPojoMatchPreferences.json");

        filePreferences.merge(USELESS_POJO_KEY, OtherPojo.getInstance().withName(DUMB_POJO_NAME));
        FakePojo fakePojo = filePreferences.get(USELESS_POJO_KEY, FakePojo.class);

        assertThat(fakePojo).isEqualToComparingFieldByField(FakePojo.getDumbInstance());
    }

    @Test
    public void testOverwrite() throws IOException {
        FilePreferences filePreferences = getPreferencesFile("perfectPojoMatchPreferences.json");

        filePreferences.put(USELESS_POJO_KEY, OtherPojo.getInstance());
        OtherPojo otherPojo = filePreferences.get(USELESS_POJO_KEY, OtherPojo.class);

        assertThat(otherPojo).isEqualToComparingFieldByField(OtherPojo.getInstance());

        FakePojo fakePojo = filePreferences.get(USELESS_POJO_KEY, FakePojo.class);

        assertThat(fakePojo).isEqualToComparingFieldByField(FakePojo.getDumbInstance()
                                                                    .withName(OtherPojo.OTHER_POJO_NAME)
                                                                    .withLeitmotiv(null)
                                                                    .withCharacteristics((String[]) null));
    }

    @Test
    public void testParallelConsumption() throws IOException, InterruptedException, ExecutionException {
        final FilePreferences filePreferences = getPreferencesFile("perfectPojoMatchPreferences.json");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                filePreferences.delete(USELESS_POJO_KEY);
                return null;
            }
        });

        Future<FakePojo> futureFakePojo = executorService.submit(new Callable<FakePojo>() {
            @Override
            public FakePojo call() throws Exception {
                return filePreferences.get(USELESS_POJO_KEY, FakePojo.class);
            }
        });

        assertThat(futureFakePojo.get()).isNull();
    }

    @Test
    public void testDeepPutAndGet() throws IOException {
        FilePreferences filePreferences = getPreferencesFile("deeperPerfectPojoMatchPreferences.json");

        FakePojo fakePojo = filePreferences.path("intermediateNode")
                                              .get(USELESS_POJO_KEY, FakePojo.class);

        assertThat(FakePojo.getDumbInstance()).isEqualToComparingFieldByField(fakePojo);

        filePreferences.path("intermediateNode")
                          .put(USELESS_POJO_KEY, FakePojo.getDumbInstance().withLeitmotiv("Dumb! Dumb! Dumb!"));

        assertThat(FakePojo.getDumbInstance()).isEqualToComparingFieldByField(fakePojo);

        fakePojo = filePreferences.path("intermediateNode")
                                     .get(USELESS_POJO_KEY, FakePojo.class);

        assertThat(fakePojo).isEqualToComparingFieldByField(FakePojo.getDumbInstance().withLeitmotiv("Dumb! Dumb! Dumb!"));
    }

    @Test
    public void testPutAndGetSimpleType() throws IOException {
        File tempPreferencesFile = File.createTempFile("preferencesTest", ".json");
        tempPreferencesFile.deleteOnExit();
        FilePreferences filePreferences = new FilePreferences(tempPreferencesFile).setDisableSaveOnChanges();

        filePreferences.put("key", 1);

        filePreferences.get("key", Integer.class);
    }

    @Test
    public void testPutArrayType() throws IOException {
        File tempPreferencesFile = File.createTempFile("preferencesTest", ".json");
        tempPreferencesFile.deleteOnExit();
        FilePreferences filePreferences = new FilePreferences(tempPreferencesFile).setDisableSaveOnChanges();

        filePreferences.put("key", new String[]{"fake", "dumb", "useless"});

        filePreferences.get("key", String[].class);
    }

    @Test
    public void testPutEnumType() throws IOException {
        File tempPreferencesFile = File.createTempFile("preferencesTest", ".json");
        tempPreferencesFile.deleteOnExit();
        FilePreferences filePreferences = new FilePreferences(tempPreferencesFile).setDisableSaveOnChanges();

        filePreferences.put("key", TimeUnit.DAYS);

        filePreferences.get("key", TimeUnit.class);
    }

    @Test
    public void testPutCollectionType() throws IOException {
        File tempPreferencesFile = File.createTempFile("preferencesTest", ".json");
        tempPreferencesFile.deleteOnExit();
        FilePreferences filePreferences = new FilePreferences(tempPreferencesFile).setDisableSaveOnChanges();

        filePreferences.put("key", asList("fake", "dumb", "useless"));

        filePreferences.get("key", List.class);
    }
}
