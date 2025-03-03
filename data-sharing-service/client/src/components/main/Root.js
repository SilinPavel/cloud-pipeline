/*
 * Copyright 2017-2019 EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import {browserHistory} from 'react-router';
import {Provider} from 'mobx-react';
import {RouterStore, syncHistoryWithStore} from 'mobx-react-router';
import AppRouter from './AppRouter';
import dataStorages from '../../models/dataStorage/DataStorages';
import S3Storage from '../../models/s3Storage/s3Storage';
import dataStorageCache from '../../models/dataStorage/DataStorageCache';
import MetadataCache from '../../models/metadata/MetadataCache';
import preferences from '../../models/preferences';

const routing = new RouterStore();
const history = syncHistoryWithStore(browserHistory, routing);
const metadataCache = new MetadataCache();
(preferences.fetch)();

const Root = () =>
  <Provider
    {...{
      routing,
      dataStorages,
      dataStorageCache,
      metadataCache,
      history,
      S3Storage,
      preferences
    }}>
    <AppRouter />
  </Provider>;

export default Root;
