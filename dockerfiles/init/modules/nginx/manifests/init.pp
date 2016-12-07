class nginx {

  file { "/opt/codenvy/config/nginx":
    ensure  => "directory",
    mode    => "755",
  } ->

  file { "/opt/codenvy/config/nginx/nginx.conf":
    ensure  => "present",
    content => template("nginx/http_nginx.conf.erb"),
    mode    => '644',
  }
}
