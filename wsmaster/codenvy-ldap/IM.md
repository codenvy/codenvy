How to install codenvy onprem from custom bundle.

1) install IM CLI, execute: `bash <(curl -L -s https://get.codenvy.com) --im-cli`

2) execute: `. ~/.bashrc`

3) Download custom codenvy onpremises bundle.

Just in case here is instruction how to build codenvy bundle:

- clone following repos, and build them with `mvn clean install -DskipTests -Dskip-validate-sources`
```
github.com/eclipse/che
github.com/codenvy/codenvy
```

- clone `github.com/riuvshin/cdec-bundle` and build it with `mvn clean install`

- take codenvy onprem bundle from 1`cdec-bundle/target/` folder and copy to instance where you have installed `IM CLI`


4) To perform installation execute: `codenvy install --binaries=cdec-bundle-{version}.zip codenvy {version}`

Please note that `--binaries=cdec-bundle-{version}.zip` means that you have codenvy onprem bundle file with name `cdec-bundle-{version}.zip` in current directory.

