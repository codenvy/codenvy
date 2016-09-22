/*
 *  [2015] - [2016] Codenvy, S.A.
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
'use strict';

const anonUsageDataPropertyName = 'anon-usage-data';

export class AutomaticUpdatesCtrl {


  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($rootScope, imsSaasAuthApi, imsPropertiesApi) {
    this.$rootScope = $rootScope;
    this.imsPropertiesApi = imsPropertiesApi;

    this.$rootScope.$watch(
      () => imsSaasAuthApi.promise,
      (newValue) => { this._updateSubscriptionStatus(newValue); }
    );
    // by default, false, until login and subscription check
    this.subscriptionOk = false;

    /* flag set when property is received */
    this.propertyReceived = false;

    this.usageData = false;

    this.imsPropertiesApi.fetchProperty(anonUsageDataPropertyName).then(() => this._propertiesReceived(this.imsPropertiesApi.getProperty(anonUsageDataPropertyName)));
  }

  _updateSubscriptionStatus(newValue) {
    if (newValue) {
      this.subscriptionOk = true;
    } else {
      this.subscriptionOk = false;
    }
    this.$rootScope.$broadcast('chePanel:disabled', { id: 'usage-data-and-automatic-install', disabled: this.isSectionDisabled() });
  }

  isSectionDisabled() {
    return !this.subscriptionOk;
  }

  usageDataChanged() {
    if (!this.usageDataDisabled) {
      this._dataChanged(anonUsageDataPropertyName, this._usageData);
    }
  }

  _dataChanged(prop, value) {
    this.imsPropertiesApi.storeProperty(prop, value).then(() => this._saveOk(prop)).catch(error => this._saveFailed(error, prop));
  }

  _propertiesReceived(value) {
    this.propertyReceived = true;
    if (typeof(value) === 'undefined') {
      this.usageData = false;
    } else if (typeof(value) === 'string') {
      switch (value) {
        case 'true':
          this.usageData = true;
          break;
        case 'false':
          this.usageData = false;
          break;
        default:
          this.usageData = false;
          this.usageDataChanged();
          break;
      }
    } else if (typeof(value) === 'boolean') {
      this.usageData = value;
    } else {
      this.usageData = false;
      this.usageDataChanged();
    }
  }

  _setDisabledProps(disabled) {
    this.usageDataDisabled = disabled;
  }

  _saveOk() {
    this._setDisabledProps(false);
  }

  _saveFailed(error, prop) {
    switch (prop) {
      case anonUsageDataPropertyName:
        this.usageDataSaveError = true;
        // restore previous value
        this.usageData = !this.usageData;
        break;
      default:
        break;
    }
    this._setDisabledProps(false);
  }

  set usageData(newValue) {
    //console.log('set usageData', newValue);
    this._usageData = newValue;
  }

  get usageData() {
    //console.log('get usageData', this._usageData);
    return this._usageData;
  }
}
