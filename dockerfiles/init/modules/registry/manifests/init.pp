class registry {

  file { "/opt/codenvy/config/registry":
    ensure  => "directory",
    mode    => "755",
  } ->
  file { "/opt/codenvy/config/registry/config.yml":
    ensure  => "present",
    content => template("registry/config.yml.erb"),
    mode    => '644',
  } ->
  file { "/opt/codenvy/config/registry/registry_entrypoint.sh":
    ensure  => "present",
    content => template("registry/registry_entrypoint.sh.erb"),
    mode    => '755',
  }
}

