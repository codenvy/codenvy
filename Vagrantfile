box             = 'centos7.2'
url             = 'https://install.codenvycorp.com/centos7.2.box'

ram             = '4096'
cpus            = '2'
bridge          = 'eth0'

http_proxy      = ""
https_proxy     = ""
codenvy_url     = "http://start.codenvy.com/install-codenvy"
codenvy_options = "--suppress --silent --license=accept"

Vagrant.configure("2") do |config|
  config.vm.box = box
  config.vm.box_url = url
  config.vm.box_download_insecure = true
  config.ssh.insert_key = false
  config.vm.network :private_network, ip: "192.168.56.110"
  config.vm.network "forwarded_port", guest: 5005, host: 5005

  config.vm.provider :virtualbox do |vbox|
    vbox.customize [
        'modifyvm', :id,
        '--memory', ram,
        '--cpus', cpus
    ]
    vbox.name = "codenvy-team"
  end

  #Adding hosts rules
  config.vm.provision "shell", inline: "echo -e \"127.0.0.1 localhost\" > /etc/hosts"
  config.vm.provision "shell", inline: "echo -e \"nameserver 8.8.8.8\n\" >> /etc/resolv.conf"

  $script = <<-SHELL
    HTTP_PROXY=$1
    HTTPS_PROXY=$2
    CODENVY_URL=$3
    CODENVY_OPTIONS=$4

    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
      echo "."
      echo "."
      echo "CODENVY: CONFIGURING PROXY"
      echo "."
      echo "."
      echo "HTTP PROXY set to: $HTTP_PROXY"
      echo "HTTPS PROXY set to: $HTTPS_PROXY"
    fi
    
    echo "."
    echo "."
    echo "CODENVY: RUNNING CODENVY INSTALLER"
    echo "."
    echo "."

    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
      bash <(curl -L -s --proxy ${HTTP_PROXY} ${CODENVY_URL}) ${CODENVY_OPTIONS} --http-proxy=${HTTP_PROXY} --https-proxy=${HTTPS_PROXY}
    else
      bash <(curl -L -s ${CODENVY_URL}) ${CODENVY_OPTIONS}
    fi

    echo "."
    echo "."
    echo "CODENVY: DOWNLOADING POPULAR STACKS"
    echo "."
    echo "."
    docker pull codenvy/ubuntu_jdk8

    echo "."
    echo "."
    echo "CODENVY: INSTALLED!"
    echo 'Add "192.168.56.110 codenvy" to your hosts file'
    echo 'Access:   http://codenvy'
    echo 'Username: admin'
    echo 'Password: password'
    echo "."
    echo "."
  SHELL

  config.vm.provision "shell" do |s|
    s.inline = $script
    s.args = [http_proxy, https_proxy, codenvy_url, codenvy_options]
  end

end
