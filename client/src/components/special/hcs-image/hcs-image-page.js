/*
 * Copyright 2017-2022 EPAM Systems, Inc. (https://www.epam.com/)
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

import React from 'react';
import classNames from 'classnames';
import {inject, observer} from 'mobx-react';
import HcsImage from './hcs-image';
import styles from './hcs-image.css';

@inject((stores, params) => {
  const {location = {}} = params;
  const {query = {}} = location;
  return {
    storageId: query.storage,
    hcsImagePath: query.path
  };
})
@observer
class HcsImagePage extends React.Component {
  render () {
    const {
      storageId,
      hcsImagePath
    } = this.props;
    return (
      <HcsImage
        className={
          classNames(
            styles.hcsImagePage,
            'app-background',
            'cp-panel',
            'cp-panel-transparent',
            'no-image'
          )
        }
        storageId={storageId}
        path={hcsImagePath}
        wellViewByDefault={false}
      />
    );
  }
}

export default HcsImagePage;
