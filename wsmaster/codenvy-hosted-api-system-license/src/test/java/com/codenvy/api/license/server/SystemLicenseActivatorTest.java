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
package com.codenvy.api.license.server;

import com.codenvy.api.license.SystemLicense;
import com.codenvy.api.license.exception.SystemLicenseNotActivatedException;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SystemLicenseActivatorTest {
    private static final String PRODUCT_ID = "testId";

    private static final String PUBLIC_KEY = "30820122300d06092a864886f70d01010105000382010f00303032301006\n" +
                                             "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
                                             "0044fa4b331c1b406f0da88c9fac6cd6ae07d15712697acc98926991b9bG\n" +
                                             "82010a02820101008fe54f78b4da0d2818949776ec0a87297567a04f4e2d\n" +
                                             "944e42b46121e4e02acc4048eef2a9062e5f467ce5f33338b811d6f7aa47\n" +
                                             "2ae70e4aefbc20e842b225920f7f86101010326d6014d111f4ae91ff3f44\n" +
                                             "39ad79f7f646f9c0656303RSA4204813SHA512withRSA51ab395ba6fa1e0\n" +
                                             "335f12bc3072924d62ba818a9a13a50e546a00194d8822f74fad7c5a7e1c\n" +
                                             "1cf679931735e51e62d41936a7ae4f2db682ed52934a96ff8cfa77fd67de\n" +
                                             "bdd1cfd551f7c45082bae164f8a455b872b19661aec358ec8d50cd11d1ba\n" +
                                             "aab3c7830abb02215206d39c18f604a4cbf0766d8d7db06f5b990dccc914\n" +
                                             "4cecf187c6c650ecbb1c9fff617f9f50d9b090ce30dbd93bcdd95164f147\n" +
                                             "1f651b4ddd7090203010001";

    private static final String LICENCE_TEXT = "cd397537c5111de5814cfcf7d9963af05867348912b3d27699668a4503e0\n" +
                                               "21105f0862b3726e1708a824b90b4d4b9c87934ea898a22d6c0dc0995d81\n" +
                                               "aebb2db8ca65a37e9928dde224c820c1cab21eb9c674a66a4b68a748cafb\n" +
                                               "e606add9a5050f5c7a00e94ffef5eb083fe2938d99b18c07872cdd569f82\n" +
                                               "8aa5565309102bade94537c0a266c0147f92ac8e25aee2e3ce900cff61a8\n" +
                                               "f8203a1b6886954d609a2ef5b3613b83717bdcaa3988d1f208f3b38a7620\n" +
                                               "519844f2c6da72eec34a1c03a926ada756162d24c5a5d97a0e529c877f0e\n" +
                                               "b9ea750b4d950e05faddf1ada3a190362bd6c4c8c68e2fe709d3dea29510\n" +
                                               "73fdb4ed0c385d19532b271b9ffa45b1b16885529476837675dc07d031b8\n" +
                                               "419a9f44109c109ed0850feda3c17d13828e2c4c9cbef9c1e7c61047e7fc\n" +
                                               "64ca346c4e9ec67f2876d42f9477ec490070b419670be56398ce9df4c408\n" +
                                               "434d0d1c71aa00562b133e7d448b86c66ae5d7b4d39d36f57e5a75653ec2\n" +
                                               "602682a14b3a132ca348e6514818ae95f5c5ef9407bca0f41a809f4bbaeb\n" +
                                               "d8922b18c3c3696957496b67a90b81f70bc94652f7d4b67d687d54d3528a\n" +
                                               "9b81db0131f0496039324ea5bf66e96d5c0fe0aa15f5422e2e9770e8e95c\n" +
                                               "64d7462b477e0b1d38cddf9ef6d89a9e34fd95d3ebb199c1c0f508d9d79b\n" +
                                               "a6aac7d307b4d97f165cab10418c904768be5f4aa2bc4ab0e51fd254d5dc\n" +
                                               "8114a8ddf7177643b2a58e8105fb794eff70ffba33c8ed7180461eb796b7\n" +
                                               "6313cded9cb655f87865ccf18824973172f312fee29efa0345c20dc87c48\n" +
                                               "a6b805c9ab7b8a6a2e5b267696e610cc96e8c973beb841ddac4e4463c051\n" +
                                               "e87c197fa21e09be08ee1ae24b3ee9b2130b9a80b8ae5f332d263ec45bee\n" +
                                               "0577bb73cd91366bf27289142d670c3eb00dfa437f01f80151cdc80698ce\n" +
                                               "27774e4e542fae8aaa3bf8f8";

    private static final String LICENSE_ACTIVATION_TEXT = "179cfcb01eb4365ac5b807d29650f41f0551298023fe8359a065672a4f27\n" +
                                                          "d00a1098177e9428f4f8b155d7c5ca76a9fe9d46cf3c33cfacc9b09d7357\n" +
                                                          "63dc9f947de1b505f8d6389424275eae7464bc001e65af9ac319818c8a49\n" +
                                                          "1702d72e799664fb1a3f4e04ad0843e09c1b3ce10acbb3ee580ad68540f1\n" +
                                                          "9633c3841bf8d81cf712a6f1ab170229805cec3f63f08149622960872c1a\n" +
                                                          "dec9dc913fec7d7f3d4b5a41e880c4e1d2dee8aa98bdd91d1c15e8f81669\n" +
                                                          "0647c0002ddcdcae247607434c11088e4a85f30ba9c52d410b8204edbfd7\n" +
                                                          "b342380177d1ef8ad12a41edef56887229a8ff725e969ee57b298e8e0beb\n" +
                                                          "9ac9794e430dc05f2adf138d2ff254ebfbd10193947d6965bc1c3d47338a\n" +
                                                          "146e62499f2b1eeef111d067876872bde9845737aaae9a221395fd899dac\n" +
                                                          "8038e96cbf89d527458de9747264fb74790b07045a86b8e9646784f8af90\n" +
                                                          "d9edd8533398d13028b16cdafb9cbb1990b6acf6bc38288fb2ec253b2b21\n" +
                                                          "ce8d353898f7476c0c404cc44ccb56f529911da3433b7bf1ec5eca220018\n" +
                                                          "f0b9de6779264a1f289b07536a71397cfd8b7707731c24da364826badbad\n" +
                                                          "67451a62f74ad9edab768d23eb04f53a30f3873e2f3005229e3f546853d6\n" +
                                                          "da1816695cdde605d9e0b6fa0bf0cffa16e90a9cb8ac8b0d23da8faef37d\n" +
                                                          "e300dbd34e14c02f08040270cfb5bd9910a97e31a2858cc6a908027cbbba\n" +
                                                          "ff051fb1dc25ac83778253fb907d67680f546feb1177e0412ef4b2ff85d2\n" +
                                                          "50bfc70c494b8b9206da8e594498f0a4163c17d008a0a5140c3f4b6fb967\n" +
                                                          "b9e6fa36e0d19ce52e7b353977018da7e59e730b1dcb6ba29f017698ed24\n" +
                                                          "ffb1556ec278cbed3dbcf0168ca692c4d29ce59f2dc4094664271bd6fd5c\n" +
                                                          "c792d20124401e0a7a45478fe8c27fbecfb08242daae07cc48397e867740\n" +
                                                          "edf1f593a7204eac39db72d5ee677c4d38f28ed3cfe4ceb50bbae138db4a\n" +
                                                          "ec987eec9ae486c48705e33805ba5b0f9a0ea435a66056810c5b3221ec2d\n" +
                                                          "ce79917ae0f17120a63450d860a101ed74e33d0d3868771c3fed66ee5446\n" +
                                                          "2a086c56f788d04f7f85dc43c7e7f22b83d57beb0b4f967adb9dde3c5aac\n" +
                                                          "d84d8d9c7021d8a60f8d4ec2ccd147ef725e889fd2da1097216d12129cd5\n" +
                                                          "40f09d61c7a96d45cc4c9563c591572f0af9e72676a64adaa08ce331a08c\n" +
                                                          "739aa8416410c271beff9cd9447607225235ac0679e8c7afb55373bb928f\n" +
                                                          "8f47\n";

    @Mock
    private SystemLicenseStorage systemLicenseStorage;
    @Mock
    private SystemLicense        systemLicense;

    private SystemLicenseActivator systemLicenseActivator;

    @BeforeMethod
    public void setUp() throws IOException {
        when(systemLicense.getLicenseText()).thenReturn(LICENCE_TEXT);

        systemLicenseActivator = new SystemLicenseActivator(systemLicenseStorage, PUBLIC_KEY, PRODUCT_ID.toCharArray());
    }

    @Test
    public void shouldNotThrowExceptionIfActivationNotRequired() throws Exception {
        when(systemLicense.isActivationRequired()).thenReturn(false);

        systemLicenseActivator.validateActivation(systemLicense);
    }

    @Test
    public void shouldNotThrowExceptionIfCodenvyActivationLicenseTextExists() throws Exception {
        when(systemLicense.isActivationRequired()).thenReturn(true);
        when(systemLicenseStorage.loadActivatedLicense()).thenReturn(LICENSE_ACTIVATION_TEXT);

        systemLicenseActivator.validateActivation(systemLicense);
    }

    @Test(expectedExceptions = SystemLicenseNotActivatedException.class)
    public void shouldThrowExceptionIfActivationTextInvalid() throws Exception {
        when(systemLicense.isActivationRequired()).thenReturn(true);
        when(systemLicenseStorage.loadActivatedLicense()).thenReturn("some invalid activation text");

        systemLicenseActivator.validateActivation(systemLicense);
    }

    @Test(expectedExceptions = SystemLicenseNotActivatedException.class)
    public void shouldThrowExceptionIfActivationTextRequiresActivation() throws Exception {
        when(systemLicense.isActivationRequired()).thenReturn(true);
        when(systemLicenseStorage.loadActivatedLicense()).thenReturn(LICENCE_TEXT);

        systemLicenseActivator.validateActivation(systemLicense);
    }
}
