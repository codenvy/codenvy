class lighttpd {
  file { "/opt/codenvy/config/lighttpd":
    ensure  => "directory",
    mode    => "755",
  } ->

  file { "/opt/codenvy/config/lighttpd/lighttpd.conf":
    ensure  => "present",
    content => template("lighttpd/lighttpd.conf.erb"),
    mode    => '644',
  }
}
