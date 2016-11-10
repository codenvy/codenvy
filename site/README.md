Dependencies
=======

[package.json](https://github.com/codenvy/codenvy/blob/master/site/package.json) contains detailed information on both runtime and development dependencies.

To run site locally and to be able to build project for deployment you will need:
Install environment on Ubuntu 14.04
=======
```
sudo apt-get -y install git curl libdigest-sha-perl gcc
sudo apt-get -y install ruby-full build-essential ruby-compass
echo 'export PATH=$HOME/local/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
mkdir ~/local
mkdir ~/node-latest-install
cd ~/node-latest-install
curl http://nodejs.org/dist/node-latest.tar.gz | tar xz --strip-components=1
./configure --prefix=~/local
make install
curl https://www.npmjs.org/install.sh | sh
git clone git://github.com/creationix/nvm.git ~/nvm
export NVM_DIR=~/nvm
echo "export NVM_DIR=~/nvm" >> ~/.bashrc
echo "export PATH=$NVM_DIR:$PATH" >> ~/.bashrc
echo "source ~/nvm/nvm.sh" >> ~/.bashrc
source ~/.bashrc
sudo gem install jekyll
npm install -g gulp grunt-cli bower
npm install -g grunt-cli #  (For user-dashboard)
npm install -g bower # (For user-dashboard)
```
!!! BUG (It needs to fix last exported PATH by adding :$PATH)

source ~/.bashrc

Running locally
=======

To run the site on your machine

```
mvn clean install
gulp connect

```
Enabling watcher
=======

```
mvn clean install
gulp connect
gulp watch

```



The site will be available at localhost:8080


How to build
=======

Security first - make sure you are wearing a helmet.

```
mvn clean install
```

Build populates target/dist/ with 3 sets of bundles :
- stage (for staging),
- prod (for production with minified CSS, JS),
- gh (for local version).

The sets contain all the static content needed to run the site.

