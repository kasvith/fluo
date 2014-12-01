/*
 * Copyright 2014 Fluo authors (see AUTHORS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fluo.api.client;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SubsetConfiguration;

/**
 * Client interface for Fluo. Fluo clients will have shared resources used by all objects created by the client. Therefore, {@link FluoClient#close()} must
 * called when you are finished using the client.
 */
public interface FluoClient extends AutoCloseable {
  
  /**
   * Creates a {@link LoaderExecutor} for loading data into Fluo. Use within a try-with-resources statement or call {@link LoaderExecutor#close()} when you are
   * finished using it.
   */
  public LoaderExecutor newLoaderExecutor();
  
  /**
   * Creates a {@link Snapshot} for reading data from Fluo. Use within a try-with-resources statement or call {@link Snapshot#close()} when you are finished
   * using it.
   */
  public Snapshot newSnapshot();
  
  /**
   * Creates a {@link Transaction} for reading and writing data to Fluo. Unlike the transactions provided by the {@link Loader} and {@link Observer}, users will
   * need to call {@link Transaction#commit()}. Use within a try-with-resources statement or call {@link Transaction#close()} when you are finished.
   */
  public Transaction newTransaction();

  /**
   * @return All properties w/ the prefix {@value io.fluo.api.config.FluoConfiguration#APP_PREFIX} that were set at initialization time. The configuration returned is a
   *         {@link SubsetConfiguration} using the prefix {@value io.fluo.api.config.FluoConfiguration#APP_PREFIX} The reason these properties are stored and read from zookeeper
   *         is to offer a consistent view of application config across all nodes in the cluster. So there is no need to worry w/ keeping config files
   *         consistent across a cluster. To update this configuration, use {@link FluoAdmin#updateSharedConfig()}. Changes made to the returned Configuration
   *         will not update Zookeeper.
   */
  
  public Configuration getAppConfiguration();
  
  /**
   * Closes client resources
   */
  @Override
  public void close();
}
