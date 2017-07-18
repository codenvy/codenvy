/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
(function () {
    var uv = document.createElement('script');
    uv.type = 'text/javascript';
    uv.async = true;
    uv.src = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'widget.uservoice.com/jWE2fqGrmh1pa5tszJtZQA.js';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(uv, s);
})();
