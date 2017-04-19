  var _gaq = _gaq || [];
  if (window.location.hostname == 'localhost'){//patch for tracking localhost in chrome
    _gaq.push(['_setDomainName', 'none']);
  }
  _gaq.push(
    ['_setAccount', "UA-37306001-1"], // codenvy account
    ['_trackPageview'],
    ['exo._setAccount', "UA-1292368-18"], // eXo account
    ['exo._trackPageview']
  );
  (function(d,t){var g=d.createElement(t),s=d.getElementsByTagName(t)[0];
  g.src=('https:'==location.protocol?'//ssl':'//www')+'.google-analytics.com/ga.js';
  s.parentNode.insertBefore(g,s)}(document,'script'));