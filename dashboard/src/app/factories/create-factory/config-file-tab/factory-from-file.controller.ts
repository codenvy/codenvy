/*
 *  [2015] - [2017] Codenvy, S.A.
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

/* global FileReader */

/**
 * Controller for upload factory from the file.
 * @author Oleksii Orel
 */
export class FactoryFromFileCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($filter, codenvyAPI, cheNotification, FileUploader) {
    'ngInject';

    this.codenvyAPI = codenvyAPI;
    this.cheNotification = cheNotification;

    // If you want select just one file, you won't need to clear the input
    FileUploader.FileSelect.prototype.isEmptyAfterSelection = function () {
      return true;
    };

    this.uploader = new FileUploader();

    // Settings
    this.uploader.queueLimit = 1;// Maximum count of files
    this.uploader.autoUpload = true;// Automatically upload files after adding them to the queue
    this.uploader.removeAfterUpload = true;// Automatically remove files from the queue after uploading

    this.isImporting = this.uploader.isUploading;

    var ctrl = this;

    // Filters
    this.uploader.filters.push({
      name: 'sizeFilter',
      fn: function (item) {
        // File must not be smaller then some size
        let isValidSize = item.size > 0 && item.size < 500000;

        if (!isValidSize) {
          ctrl.cheNotification.showError('File size error.');
        }
        return isValidSize;
      }
    });

    this.uploader.filters.push({
      name: 'typeFilter',
      fn: function (item) {
        // File must be json
        let isValidItem = item.type === 'application/json' || item.type === '';

        if (!isValidItem) {
          ctrl.cheNotification.showError('File type error.');
        }
        return isValidItem;
      }
    });

    // Callback
    this.uploader.onAfterAddingFile = function (fileItem) {
      let uploadedFileName = fileItem._file.name;
      let reader = new FileReader();

      reader.readAsText(fileItem._file);
      reader.onload = function () {
        try {
          ctrl.factoryContent = $filter('json')(angular.fromJson(reader.result), 2);
          ctrl.cheNotification.showInfo('Successfully loaded file\'s configuration ' + uploadedFileName + '.');
        } catch (e) {
          // invalid JSON
          ctrl.factoryContent = null;
          ctrl.cheNotification.showError('Invalid JSON.');
        }
      };
      reader.onerror = function (error) {
        ctrl.cheNotification.showError(error.data.message ? error.data.message : 'Error reading file.');
        console.log('Error reading file');
      };
    };

    this.factoryContent = null;
  }

}
