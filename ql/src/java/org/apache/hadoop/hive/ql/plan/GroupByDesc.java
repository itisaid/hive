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

package org.apache.hadoop.hive.ql.plan;

import java.util.ArrayList;

import org.apache.hadoop.hive.ql.udf.UDFType;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;

/**
 * GroupByDesc.
 *
 */
@Explain(displayName = "Group By Operator")
public class GroupByDesc extends AbstractOperatorDesc {
  /**
   * Group-by Mode: COMPLETE: complete 1-phase aggregation: iterate, terminate
   * PARTIAL1: partial aggregation - first phase: iterate, terminatePartial
   * PARTIAL2: partial aggregation - second phase: merge, terminatePartial
   * PARTIALS: For non-distinct the same as PARTIAL2, for distinct the same as
   *           PARTIAL1
   * FINAL: partial aggregation - final phase: merge, terminate
   * HASH: For non-distinct the same as PARTIAL1 but use hash-table-based aggregation
   * MERGEPARTIAL: FINAL for non-distinct aggregations, COMPLETE for distinct
   * aggregations.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Mode.
   *
   */
  public static enum Mode {
    COMPLETE, PARTIAL1, PARTIAL2, PARTIALS, FINAL, HASH, MERGEPARTIAL
  };

  private Mode mode;
  private boolean groupKeyNotReductionKey;
  private boolean bucketGroup;

  private ArrayList<ExprNodeDesc> keys;
  private ArrayList<org.apache.hadoop.hive.ql.plan.AggregationDesc> aggregators;
  private ArrayList<java.lang.String> outputColumnNames;
  private float groupByMemoryUsage;
  private float memoryThreshold;

  public GroupByDesc() {
  }

  public GroupByDesc(
      final Mode mode,
      final ArrayList<java.lang.String> outputColumnNames,
      final ArrayList<ExprNodeDesc> keys,
      final ArrayList<org.apache.hadoop.hive.ql.plan.AggregationDesc> aggregators,
      final boolean groupKeyNotReductionKey,float groupByMemoryUsage, float memoryThreshold) {
    this(mode, outputColumnNames, keys, aggregators, groupKeyNotReductionKey,
        false, groupByMemoryUsage, memoryThreshold);
  }

  public GroupByDesc(
      final Mode mode,
      final ArrayList<java.lang.String> outputColumnNames,
      final ArrayList<ExprNodeDesc> keys,
      final ArrayList<org.apache.hadoop.hive.ql.plan.AggregationDesc> aggregators,
      final boolean groupKeyNotReductionKey, final boolean bucketGroup,float groupByMemoryUsage, float memoryThreshold) {
    this.mode = mode;
    this.outputColumnNames = outputColumnNames;
    this.keys = keys;
    this.aggregators = aggregators;
    this.groupKeyNotReductionKey = groupKeyNotReductionKey;
    this.bucketGroup = bucketGroup;
    this.groupByMemoryUsage = groupByMemoryUsage;
    this.memoryThreshold = memoryThreshold;
  }

  public Mode getMode() {
    return mode;
  }

  @Explain(displayName = "mode")
  public String getModeString() {
    switch (mode) {
    case COMPLETE:
      return "complete";
    case PARTIAL1:
      return "partial1";
    case PARTIAL2:
      return "partial2";
    case PARTIALS:
      return "partials";
    case HASH:
      return "hash";
    case FINAL:
      return "final";
    case MERGEPARTIAL:
      return "mergepartial";
    }

    return "unknown";
  }

  public void setMode(final Mode mode) {
    this.mode = mode;
  }

  @Explain(displayName = "keys")
  public ArrayList<ExprNodeDesc> getKeys() {
    return keys;
  }

  public void setKeys(final ArrayList<ExprNodeDesc> keys) {
    this.keys = keys;
  }

  @Explain(displayName = "outputColumnNames")
  public ArrayList<java.lang.String> getOutputColumnNames() {
    return outputColumnNames;
  }

  public void setOutputColumnNames(
      ArrayList<java.lang.String> outputColumnNames) {
    this.outputColumnNames = outputColumnNames;
  }

  public float getGroupByMemoryUsage() {
    return groupByMemoryUsage;
  }

  public void setGroupByMemoryUsage(float groupByMemoryUsage) {
    this.groupByMemoryUsage = groupByMemoryUsage;
  }

  public float getMemoryThreshold() {
    return memoryThreshold;
  }

  public void setMemoryThreshold(float memoryThreshold) {
    this.memoryThreshold = memoryThreshold;
  }

  @Explain(displayName = "aggregations")
  public ArrayList<org.apache.hadoop.hive.ql.plan.AggregationDesc> getAggregators() {
    return aggregators;
  }

  public void setAggregators(
      final ArrayList<org.apache.hadoop.hive.ql.plan.AggregationDesc> aggregators) {
    this.aggregators = aggregators;
  }

  public boolean getGroupKeyNotReductionKey() {
    return groupKeyNotReductionKey;
  }

  public void setGroupKeyNotReductionKey(final boolean groupKeyNotReductionKey) {
    this.groupKeyNotReductionKey = groupKeyNotReductionKey;
  }

  @Explain(displayName = "bucketGroup")
  public boolean getBucketGroup() {
    return bucketGroup;
  }

  public void setBucketGroup(boolean dataSorted) {
    bucketGroup = dataSorted;
  }

  /**
   * Checks if this grouping is like distinct, which means that all non-distinct grouping
   * columns behave like they were distinct - for example min and max operators.
   */
  public boolean isDistinctLike() {
    ArrayList<AggregationDesc> aggregators = getAggregators();
    for(AggregationDesc ad: aggregators){
      if(!ad.getDistinct()) {
        GenericUDAFEvaluator udafEval = ad.getGenericUDAFEvaluator();
        UDFType annot = udafEval.getClass().getAnnotation(UDFType.class);
        if(annot == null || !annot.distinctLike()) {
          return false;
        }
      }
    }
    return true;
  }
}
