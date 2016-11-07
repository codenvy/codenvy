class registry {

  file { "/opt/codenvy/config/registry":
    ensure  => "directory",
    mode    => "755",
  } ->
  file { "/opt/codenvy/config/registry/registry.env":
    ensure  => "present",
    content => template("registry/registry.erb"),
    mode    => '644',
  }
}

