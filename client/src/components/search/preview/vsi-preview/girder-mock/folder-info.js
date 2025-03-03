/*
 * Copyright 2017-2021 EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import getStorageInfo from './utilities/get-storage-info';
import roleModel from '../../../../../utils/roleModel';

function folderInfo (item, resolve) {
  getStorageInfo(item?.storage)
    .then((info) => {
      if (info && info.mask) {
        resolve({_accessLevel: roleModel.writeAllowed(info) ? 1 : 0});
      } else {
        resolve({_accessLevel: 0});
      }
    })
    .catch(() => {
      resolve({_accessLevel: 0});
    });
}

folderInfo.test = function (url) {
  const exec = /^folder\/([\d]+)\/(.*)$/i.exec(url);
  if (exec && exec.length > 2) {
    return {
      storage: +exec[1],
      path: exec[2]
    };
  }
  return undefined;
};

export default folderInfo;
