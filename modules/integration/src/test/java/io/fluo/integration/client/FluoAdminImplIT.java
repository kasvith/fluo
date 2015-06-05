/*
 * Copyright 2014 Fluo authors (see AUTHORS)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.fluo.integration.client;

import io.fluo.accumulo.util.ZookeeperUtil;
import io.fluo.api.client.FluoAdmin;
import io.fluo.api.client.FluoAdmin.AlreadyInitializedException;
import io.fluo.api.client.FluoAdmin.InitOpts;
import io.fluo.api.client.FluoAdmin.TableExistsException;
import io.fluo.core.client.FluoAdminImpl;
import io.fluo.core.util.CuratorUtil;
import io.fluo.integration.ITBaseImpl;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FluoAdminImplIT extends ITBaseImpl {

  @Test
  public void testInitializeTwiceFails() throws AlreadyInitializedException, TableExistsException {

    try (FluoAdmin admin = new FluoAdminImpl(config)) {

      InitOpts opts = new InitOpts().setClearZookeeper(true).setClearTable(true);

      admin.initialize(opts);
      admin.initialize(opts);

      opts.setClearZookeeper(false).setClearTable(false);
      try {
        admin.initialize(opts);
        fail("This should have failed");
      } catch (AlreadyInitializedException e) {
      }

      opts.setClearZookeeper(false).setClearTable(true);
      try {
        admin.initialize(opts);
        fail("This should have failed");
      } catch (AlreadyInitializedException e) {
      }

      opts.setClearZookeeper(true).setClearTable(false);
      try {
        admin.initialize(opts);
        fail("This should have failed");
      } catch (TableExistsException e) {
      }
    }

    assertTrue(conn.tableOperations().exists(config.getAccumuloTable()));
  }

  @Test
  public void testInitializeWithNoChroot() throws AlreadyInitializedException, TableExistsException {

    InitOpts opts = new InitOpts().setClearZookeeper(true).setClearTable(true);

    for (String host : new String[] {"localhost", "localhost/", "localhost:9999", "localhost:9999/"}) {
      config.setInstanceZookeepers(host);
      try (FluoAdmin fluoAdmin = new FluoAdminImpl(config)) {
        fluoAdmin.initialize(opts);
        fail("This should have failed");
      } catch (IllegalArgumentException e) {
      }
    }
  }

  @Test
  public void testInitializeLongChroot() throws Exception {

    String zk = config.getAppZookeepers();
    String longPath = "/very/long/path";
    config.setInstanceZookeepers(zk + longPath);

    InitOpts opts = new InitOpts();
    opts.setClearZookeeper(true).setClearTable(true);

    try (FluoAdmin admin = new FluoAdminImpl(config)) {
      admin.initialize(opts);
    }

    try (CuratorFramework curator = CuratorUtil.newRootFluoCurator(config)) {
      curator.start();
      Assert.assertNotNull(curator.checkExists().forPath(ZookeeperUtil.parseRoot(zk + longPath)));
    }

    String longPath2 = "/very/long/path2";
    config.setInstanceZookeepers(zk + longPath2);

    try (FluoAdmin admin = new FluoAdminImpl(config)) {
      admin.initialize(opts);
    }

    try (CuratorFramework curator = CuratorUtil.newRootFluoCurator(config)) {
      curator.start();
      Assert.assertNotNull(curator.checkExists().forPath(ZookeeperUtil.parseRoot(zk + longPath2)));
      Assert.assertNotNull(curator.checkExists().forPath(ZookeeperUtil.parseRoot(zk + longPath)));
    }
  }
}