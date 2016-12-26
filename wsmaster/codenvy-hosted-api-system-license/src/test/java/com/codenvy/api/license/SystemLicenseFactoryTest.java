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
package com.codenvy.api.license;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SystemLicenseFactoryTest {
    // Custom Signed Features: | license-type = product key | expiration-date = 2020/12/01 | users = 10 |
    private static final String LICENCE_TEXT = "1c4594dd37a0a02e8522c0c7670431dcce1c2923688302fab98f037a5087\n" +
                                               "20971abd43902c86e1c841f557f4d65ca878fc5949619992437e5f949209\n" +
                                               "8d03aaccbcdefd108f1d0a52043d58d62ed6b117efa59135892acdb8555b\n" +
                                               "8ceb2ef104f884b1e9eeacba1db8a7fe493ce86e4e57fc3a76313b27f0f0\n" +
                                               "743ceef3f40eccdeedabca0d73a3144ab73312bfc7db156a6edaf8c7cc3d\n" +
                                               "e77148ffcae65e9cd5b8d5a6598f798bb8dd4144250796d05430fad1d176\n" +
                                               "2fc1cabe373bca479f781481f063012a14f693af9f1db9e19cf416914724\n" +
                                               "a53519744e66e3fc02575b949f1d182ee828e5a38aa2809807959f650bfe\n" +
                                               "963e5a86c5652ee231571f13a9636ece1fb99f98cd7cfeba11c70c9f8380\n" +
                                               "99bb541eefc460516185a46963cfc399609837eacedf2b58ebc22eeda808\n" +
                                               "7d06423fc083a48e100b756d111cc1c109c2d274479cec84c5159d434b58\n" +
                                               "7e2dad8fd7439228c5f484d1a1f80c86cfc1205d58be50a43b2a3d828d79\n" +
                                               "faa939ab47383dc54b76adcbb3536051894e8f88918f3dc1f5350ccf5008\n" +
                                               "60a615116def64c0950301baab844f85778c7486d9fe4f341b70c6a7176e\n" +
                                               "6aa00f2e25751cd07a5b8b4d7f4b108c303af48de3fbc695c472f0360dec\n" +
                                               "75aa2ecce3f0f76db4380039927f872dc70c1aa86c37af93592cfc71f65b\n" +
                                               "2404893c8ed8f3f3b701c1d7a0386257ba3d5421e32dd5965fa9dd297dde\n" +
                                               "560f1afd2574f88e2f726e6b5e2f0e9c8ebf9e3182aa89401600aa6926de\n" +
                                               "c4d1a32db0429b9214f16c25300af92bfc35e17c4b70251e82f4dbfff643\n" +
                                               "16987e8d3cf2c2e45b9ecf76984dfbc819b96347f9c595257bbe32e83947\n" +
                                               "e72e87f1e70ebabecd0eff8116878d06a17bd903074643b1ea62fe003519\n" +
                                               "a222d4d1db9d239f8084e1919f9c90a0146001c047df192899f59ff9d506\n" +
                                               "c2ad0e9cf7afe3bbe16be365e3487dffc270f7c87a8a55be04c85050bf33\n" +
                                               "eb32ad47a7a43500b6b082cbbe6156ec2bc82e5a5dce";

    // Custom Signed Features: | type = product key | expiration = 2012/10/01 | users = 10 |
    private static final String LICENSE_TEXT_EXPIRED = "1c4594dd37a0a02e8522c0c7670431dcce1c2923688302fa0145ffcc77df\n" +
                                                       "25cf3cb3397df02138c68dca517c28295d1be16b4504ffd06aa2f62ac423\n" +
                                                       "5afe6528e09054ca52bc597a2ae95ec4aee1ffd99375e9027e4996d40f92\n" +
                                                       "5c2f349cbc33efa6616e578f117c3d8a5518b1504d2f20c9ded9f5ecbe21\n" +
                                                       "b8f182243f84c70829d00309cdd6a28b20afd35a0bf322ea2b32f7e65121\n" +
                                                       "4ddf1cc997b418d6a4c00eb45d9464d47ee9c4be2e8723959315121328a0\n" +
                                                       "d241bf321448ff4a81dbdfd298dc7bd112995ff4a42ced152bb44fb7c39d\n" +
                                                       "f706c191fae1122ece0e0ab479bc842b99e2df2cd8d96ec1e407fe7fc9db\n" +
                                                       "c988a3cb24a3f6a864b95fa8a1c595981237ac715906e3b41062e9cdeadb\n" +
                                                       "f7db090ef951eca4bd2c7af4d93aeb7a4da6a9f23c62ddc439fa85c7f323\n" +
                                                       "c341a783510caf0b6cc8c6844f6690ff6b0714c9972af73ddc90de4df110\n" +
                                                       "5c073de04c7f6c732542464f019e7c271f268fa62e9826b159395dd357c4\n" +
                                                       "8d7c57d27f0cf9482902cdc0136ebc98d3249d30c724ce307db5db618fe3\n" +
                                                       "5ac34ef8ab5074f3891eac7e25db447edcfb857e18a063bc198c3acbff68\n" +
                                                       "2c321adb27b3b87c9a3646895a2ad51a3030c06c0648bf9dde1e598435a8\n" +
                                                       "9d58580ef00812891bd80d217db58634192a15bf7a18092344ad6ea8e456\n" +
                                                       "f2a3e0706b515f61dae9dcd93711200ccb43728a9bdbcf1a637c18e5eb9c\n" +
                                                       "e3b7261a96dae3c0b6c71a943e3aa0b51301b905712142de7b3ca7c389ef\n" +
                                                       "6257ce2cb8e9bed0898165214e4a20f6e9d84594b141028b9fd0fafff3c7\n" +
                                                       "ae64a58a3d46a839fdbd5076e244ae0fef66103a59e3db447721adf9e867\n" +
                                                       "e3fb30e5a1227cb7df09b192eb82af57b97b253eabeda2413814030f6435\n" +
                                                       "7d9502e220e121c8286a9f8883b213e1dfcab887c6b029e21e5098885ee2\n" +
                                                       "f6dcf3bfcfb881033d69eacf81327c16fcd03240a66c9f3f34fb95049701\n" +
                                                       "17c8a50af3fda6d845c796df77ae52408487e21aa4dd";

    private SystemLicenseFactory systemLicenseFactory;

    @BeforeMethod
    public void setUp() throws IOException {
        String testPropertiesPath = Thread.currentThread().getContextClassLoader().getResource("license-public-key.properties").getPath();
        Properties properties = new Properties();
        properties.load(new FileInputStream(testPropertiesPath));
        String testPublicKey = (String)properties.get("license-manager.public_key");
        systemLicenseFactory = new SystemLicenseFactory("testId".toCharArray(), testPublicKey);
    }

    @Test
    public void testGetNumberOfUsers() throws Exception {
        SystemLicense systemLicense = systemLicenseFactory.create(LICENCE_TEXT);

        assertEquals(systemLicense.getNumberOfUsers(), 10);
    }

    @Test
    public void testLicenseType() throws Exception {
        SystemLicense systemLicense = systemLicenseFactory.create(LICENCE_TEXT);

        assertEquals(systemLicense.getLicenseType(), SystemLicense.LicenseType.PRODUCT_KEY);
    }

    @Test
    public void testGetExpirationDate() throws Exception {
        SystemLicense systemLicense = systemLicenseFactory.create(LICENCE_TEXT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(systemLicense.getExpirationDateFeatureValue());

        assertEquals(calendar.get(Calendar.YEAR), 2020);
        assertEquals(calendar.get(Calendar.MONTH), Calendar.DECEMBER);
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 1);
    }

    @Test
    public void testCheckExpirationDateShouldReturnFalse() throws Exception {
        SystemLicense systemLicense = systemLicenseFactory.create(LICENCE_TEXT);

        assertFalse(systemLicense.isExpiredCompletely());
    }

    @Test
    public void testCheckExpirationDateShouldReturnTrueLicenseExpired() throws Exception {
        SystemLicense systemLicense = systemLicenseFactory.create(LICENSE_TEXT_EXPIRED);

        assertTrue(systemLicense.isExpiredCompletely());
    }

    @Test
    public void testGetCustomFeatures() throws Exception {
        SystemLicense systemLicense = systemLicenseFactory.create(LICENCE_TEXT);

        Map<SystemLicenseFeature, String> customFeatures = systemLicense.getFeatures();
        assertEquals(customFeatures.size(), 3);

        for (SystemLicenseFeature feature : SystemLicenseFeature.values()) {
            assertTrue(customFeatures.containsKey(feature));
        }
    }
}
