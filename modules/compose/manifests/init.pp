class compose {
  file { "/opt/codenvy/docker-compose.yml":
    ensure  => "present",
    content => template("compose/docker-compose.yml.erb"),
    mode    => '644',
  }
}
