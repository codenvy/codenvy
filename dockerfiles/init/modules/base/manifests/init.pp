class base {
  $dirs = [
    "/opt/codenvy",
    "/opt/codenvy/data",
    "/opt/codenvy/config",
    "/opt/codenvy/logs", ]
  file { $dirs:
    ensure  => "directory",
    mode    => "755",
  }
  include haproxy
  include nginx
  include postgres
  include swarm
  include registry
  include codenvy
  include compose
}
