class postgres {
  file { "/opt/codenvy/config/postgres":
    ensure  => "directory",
    mode    => "755",
  } ->
  file { "/opt/codenvy/config/postgres/postgresql.conf":
    content                 => template('postgres/postgresql.conf.erb'),
    ensure                  => file,
    mode                    => "644",
  }->
  file { "/opt/codenvy/config/postgres/postgres.env":
    content                 => template('postgres/postgres.env.erb'),
    ensure                  => file,
    mode                    => "644",
  }
}
