How to install codenvy onprem from custom bundle.

1) install IM CLI, execute: `bash <(curl -L -s https://get.codenvy.com) --im-cli`

2) execute: `. ~/.bashrc`

3) Download custom codenvy onpremises bundle.

Just in case here is instruction how to build codenvy bundle:

- clone following repos, and build them with `mvn clean install -DskipTests -Dskip-validate-sources`
```
github.com/eclipse/che
github.com/codenvy/codenvy
github.com/codenvy/customer-tiaa (private, only for codenvy org members)
```

- clone `github.com/riuvshin/cdec-bundle` and build it with `mvn clean install -Ddeployment.branch=tiaa -Dcustomer.groupid=tiaa`

- take codenvy onprem bundle from 1`cdec-bundle/target/` folder and copy to instance where you have installed `IM CLI`


4) To perform installation execute: `codenvy install --binaries=cdec-bundle-5.0.0-M2-SNAPSHOT.zip codenvy 5.0.0-M2-SNAPSHOT`

Please note that `--binaries=cdec-bundle-5.0.0-M2-SNAPSHOT.zip` means that you have codenvy onprem bundle file with name `cdec-bundle-5.0.0-M2-SNAPSHOT.zip` in current directory.

