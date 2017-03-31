class base {
  $dirs = [
    "/opt/codenvy",
    "/opt/codenvy/data",
    "/opt/codenvy/config",
    "/opt/codenvy/logs", ]
  file { $dirs:
    ensure  => "directory",
    mode    => "755",
  }->
  file { "/opt/codenvy/logs/zookeeper":
    ensure  => "directory",
    mode    => "777",
  }
  include haproxy
  include nginx
  include postgres
  include swarm
  include registry
  include codenvy
  include compose
  include rsyslog
  include lighttpd
}
