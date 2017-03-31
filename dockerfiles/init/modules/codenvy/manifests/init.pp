class codenvy {

  $config_dirs = [
    "/opt/codenvy/config/codenvy/",
    "/opt/codenvy/config/codenvy/conf",
    "/opt/codenvy/config/codenvy/conf/ssh",
    "/opt/codenvy/config/codenvy/conf/logback",
    "/opt/codenvy/config/codenvy/license"
  ]

# creating folders
  file { $config_dirs:
    ensure  => "directory",
    mode    => "755",
  }

# server.xml
  file { "/opt/codenvy/config/codenvy/conf/server.xml":
    ensure  => "present",
    content => template("codenvy/server.xml.erb"),
    mode    => "664",
  }

# creating codenvy.env
  file { "/opt/codenvy/config/codenvy/codenvy.env":
    ensure  => "present",
    content => template("codenvy/codenvy.env.erb"),
    mode    => "644",
  }

# creating oauth.properties
  file { "/opt/codenvy/config/codenvy/conf/oauth.properties":
    ensure  => "present",
    content => template("codenvy/oauth.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating email-connection.properties
  file { "/opt/codenvy/config/codenvy/conf/email-connection.properties":
    ensure  => "present",
    content => template("codenvy/email-connection.properties.erb"),
    mode    => "644",
    require => File[$config_dirs]
  }

# creating email.properties
  file { "/opt/codenvy/config/codenvy/conf/email.properties":
    ensure  => "present",
    content => template("codenvy/email.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating logback-additional-appenders.xml
  file { "/opt/codenvy/config/codenvy/conf/logback/logback-additional-appenders.xml":
    ensure  => "present",
    content => template("codenvy/logback-additional-appenders.xml.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating general.properties
  file { "/opt/codenvy/config/codenvy/conf/general.properties":
    ensure  => "present",
    content => template("codenvy/general.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating metrics.properties
  file { "/opt/codenvy/config/codenvy/conf/metrics.properties":
    ensure  => "present",
    content => template("codenvy/metrics.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating everrest.properties
  file { "/opt/codenvy/config/codenvy/conf/everrest.properties":
    ensure  => "present",
    content => template("codenvy/everrest.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating machine.properties
  file { "/opt/codenvy/config/codenvy/conf/machine.properties":
    ensure  => "present",
    content => template("codenvy/machine.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating licence.properties
  file { "/opt/codenvy/config/codenvy/conf/license.properties":
    ensure  => "present",
    content => template("codenvy/license.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating admin.properties
  file { "/opt/codenvy/config/codenvy/conf/admin.properties":
    ensure  => "present",
    content => template("codenvy/admin.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating report.properties
  file { "/opt/codenvy/config/codenvy/conf/report.properties":
    ensure  => "present",
    content => template("codenvy/report.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating ldap.properties
  file { "/opt/codenvy/config/codenvy/conf/ldap.properties":
    ensure  => "present",
    content => template("codenvy/ldap.properties.erb"),
    mode    => "644",
    require => File[$config_dirs],
  }

# creating rsyncbase.sh
  file { "/opt/codenvy/config/codenvy/conf/rsyncbase.sh":
    ensure  => "present",
    content => template("codenvy/rsyncbase.sh.erb"),
    mode    => "755",
    require => File[$config_dirs],
  }

# creating rsyncbackup.sh
  file { "/opt/codenvy/config/codenvy/conf/rsyncbackup.sh":
    ensure  => "present",
    content => template("codenvy/rsyncbackup.sh.erb"),
    mode    => "755",
    require => File[$config_dirs],
  }

# creating rsyncrestore.sh
  file { "/opt/codenvy/config/codenvy/conf/rsyncrestore.sh":
    ensure  => "present",
    content => template("codenvy/rsyncrestore.sh.erb"),
    mode    => "755",
    require => File[$config_dirs],
  }

# creating add-node.sh
  file { "/opt/codenvy/config/codenvy/conf/add-node.sh":
    ensure  => "present",
    content => template("codenvy/add-node.sh.erb"),
    mode    => "755",
    require => File[$config_dirs],
  }

# JMX
  file { "/opt/codenvy/config/codenvy/conf/jmxremote.access":
    ensure  => "present",
    content => "$jmx_username readwrite",
    mode    => "644",
  }

  file { "/opt/codenvy/config/codenvy/conf/jmxremote.password":
    ensure  => "present",
    content => "$jmx_username $jmx_password",
    mode    => "644",
  }

# key for rsync
  exec { "generate_ssh_key":
    cwd     => "/opt/codenvy/config/codenvy/conf/ssh",
    command => "/usr/bin/ssh-keygen -q -P '' -t rsa -f key.pem",
    creates => "/opt/codenvy/config/codenvy/conf/ssh/key.pem",
    require => File[$config_dirs],
  }

# creating cleanUpWorkspaceStorage.sh
  file { "/opt/codenvy/config/codenvy/conf/cleanUpWorkspaceStorage.sh":
    ensure  => "present",
    content => template("codenvy/cleanUpWorkspaceStorage.sh.erb"),
    mode    => "755",
    require => File[$config_dirs],
  }
}
