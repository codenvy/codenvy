/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
(function Embed() {
    var _scripts = document.getElementsByTagName('script');
    var _script = _scripts[_scripts.length - 1];
    var _parent = _script.parentNode;

    var src = _script.src.substring(0, _script.src.indexOf("/embed.js")) + "/factory.js" + _script.src.substring(_script.src.indexOf("/embed.js") + 9);

    var po = document.createElement('script');
    po.type = "text/javascript";
    po.async = true;
    po.src = src;

    if (_script.hasAttribute("style")) {
    po.setAttribute("style", _script.getAttribute("style"));
    }

    if (_script.hasAttribute("url")) {
    po.setAttribute("url", _script.getAttribute("url"));
    }

    if (_script.hasAttribute("logo")) {
    po.setAttribute("logo", _script.getAttribute("logo"));
    }

    _parent.replaceChild(po, _script);
})();
