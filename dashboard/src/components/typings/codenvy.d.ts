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
import codenvy = _codenvy;

declare namespace _codenvy {

  export interface IUserServices {
    hasUserService: boolean;
    hasUserProfileService: boolean;
    hasAdminUserService: boolean;
    hasInstallationManagerService: boolean;
  }

  export interface ILink {
    href: string;
    method: string;
    parameters: Array<any>;
    produces: string;
    rel: string;
  }

  export interface IPermissions {
    actions: Array<string>;
    domainId: string;
    instanceId: string;
    userId: string;
  }

  export interface IRole {
    actions: Array<string>;
    description: string;
    title: string;
    name: string;
  }

  export interface IMember extends che.IProfile {
    id: string;
    roles?: Array<IRole>;
    /**
     * Role name
     */
    role?: string;
    permissions?: IPermissions;
    name?: string;
    isPending?: boolean;
  }

  export interface IUser extends che.IUser{
    links?: Array<ILink>;
  }
}
