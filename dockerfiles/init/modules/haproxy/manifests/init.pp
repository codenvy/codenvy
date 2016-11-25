class haproxy {

  file { "/opt/codenvy/config/haproxy":
    ensure  => "directory",
    mode    => "755",
  } ->

  file { "/opt/codenvy/config/haproxy/haproxy.cfg":
    ensure  => "present",
    content => template("haproxy/haproxy.cfg.erb"),
    mode    => "644",
  }

  file { "/opt/codenvy/config/haproxy/maintenance.html":
    ensure  => "present",
    content => template("haproxy/maintenance.html.erb"),
    mode    => '644',
  }
}
