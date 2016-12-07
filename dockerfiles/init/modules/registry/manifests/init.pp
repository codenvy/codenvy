class registry {

  file { "/opt/codenvy/config/registry":
    ensure  => "directory",
    mode    => "755",
  } ->
  file { "/opt/codenvy/config/registry/config.yml":
    ensure  => "present",
    content => template("registry/config.yml.erb"),
    mode    => '644',
  }
}

