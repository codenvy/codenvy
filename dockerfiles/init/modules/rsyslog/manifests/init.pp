class rsyslog {

  file { "/opt/codenvy/config/rsyslog":
    ensure  => "directory",
    mode    => "755",
  } ->
  file { "/opt/codenvy/config/rsyslog/rsyslog.conf":
    ensure  => "present",
    content => template("rsyslog/rsyslog.conf.erb"),
    mode    => '644',
  } ->
  file { "/opt/codenvy/config/rsyslog/haproxy.conf":
    ensure  => "present",
    content => template("rsyslog/haproxy.conf.erb"),
    mode    => '644',
  } ->
  file { "/opt/codenvy/config/rsyslog/swarm.conf":
    ensure  => "present",
    content => template("rsyslog/swarm.conf.erb"),
    mode    => '644',
  } ->  file { "/opt/codenvy/config/rsyslog/zookeeper.conf":
    ensure  => "present",
    content => template("rsyslog/zookeeper.conf.erb"),
    mode    => '644',
  } ->
  file { "/opt/codenvy/config/rsyslog/registry.conf":
    ensure  => "present",
    content => template("rsyslog/registry.conf.erb"),
    mode    => '644',
  }
}

