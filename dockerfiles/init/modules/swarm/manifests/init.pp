class swarm {
  file { "/opt/codenvy/config/swarm":
    ensure  => "directory",
    mode    => "755",
  } ->

  file { "/opt/codenvy/config/swarm/node_list":
    ensure  => "present",
    content => template("swarm/node_list.erb"),
    mode    => '644',
  }
}
