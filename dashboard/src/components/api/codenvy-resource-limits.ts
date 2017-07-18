/*
 * Copyright (c) [2015] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * This is enum of resources limits types.
 *
 * @author Ann Shumilova
 */
export enum CodenvyResourceLimits {
  RAM = <any>'RAM',
  WORKSPACE = <any>'workspace',
  RUNTIME = <any>'runtime',
  TIMEOUT = <any>'timeout'
}
