/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.chaos.actions;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;

/**
* Action that tries to move every region of a table.
*/
public class MoveRegionsOfTableAction extends Action {
  private final long sleepTime;
  private final byte[] tableNameBytes;
  private final String tableName;

  public MoveRegionsOfTableAction(String tableName) {
    this(-1, tableName);
  }

  public MoveRegionsOfTableAction(long sleepTime, String tableName) {
    this.sleepTime = sleepTime;
    this.tableNameBytes = Bytes.toBytes(tableName);
    this.tableName = tableName;
  }

  @Override
  public void perform() throws Exception {
    HBaseAdmin admin = this.context.getHaseIntegrationTestingUtility().getHBaseAdmin();

    List<HRegionInfo> regions = admin.getTableRegions(tableNameBytes);
    Collection<ServerName> serversList = admin.getClusterStatus().getServers();
    ServerName[] servers = serversList.toArray(new ServerName[serversList.size()]);

    LOG.info("Performing action: Move regions of table " + tableName);
    for (HRegionInfo regionInfo:regions) {
      try {
        String destServerName =
          servers[RandomUtils.nextInt(servers.length)].getServerName();
        LOG.debug("Moving " + regionInfo.getRegionNameAsString() + " to " + destServerName);
        admin.move(regionInfo.getRegionName(), Bytes.toBytes(destServerName));
      } catch (Exception e) {
        LOG.debug("Error moving region", e);
      }
    }
    if (sleepTime > 0) {
      Thread.sleep(sleepTime);
    }
  }
}
