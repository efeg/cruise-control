/*
 * Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.cruisecontrol.monitor.sampling.aggregator;

import com.linkedin.cruisecontrol.common.LongGenerationed;
import com.linkedin.cruisecontrol.model.Entity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * The aggregation result of {@link MetricSampleAggregator#aggregate(long, long, AggregationOptions)}. 
 * 
 * <p>
 *   In the aggregation result, each entity will be represented with a {@link ValuesAndImputations}. It contains
 *   the values of each metric in each window. For memory efficiency the metric values are stored in a two-dimensional
 *   array. To get the window associated with each value, users may use the time window array returned by 
 *   {@link ValuesAndImputations#windows()}, or call {@link ValuesAndImputations#window(int)} to get the time window 
 *   in milliseconds for the index.
 * </p>
 *
 * @param <G> The entity group class. Note that the entity group will be used as a key to HashMaps, so it must have 
 *           a valid {@link Object#hashCode()} and {@link Object#equals(Object)} implementation.
 * @param <E> The entity class. Note that the entity will be used as a key to HashMaps, so it must have 
 *           a valid {@link Object#hashCode()} and {@link Object#equals(Object)} implementation.
 */
public class MetricSampleAggregationResult<G, E extends Entity<G>> extends LongGenerationed {
  private final Map<E, ValuesAndImputations> _entityValuesAndImputations;
  private final Set<E> _invalidEntities;
  private final MetricSampleCompleteness<G, E> _completeness;

  public MetricSampleAggregationResult(long generation, MetricSampleCompleteness<G, E> completeness) {
    super(generation);
    _entityValuesAndImputations = new HashMap<>();
    _invalidEntities = new HashSet<>();
    _completeness = completeness;
  }

  /**
   * Get the aggregated metric values and imputations (if any) of each entity.
   * 
   * @return A mapping from entity to aggregated metric values and potential imputations.
   */
  public Map<E, ValuesAndImputations> valuesAndImputations() {
    return _entityValuesAndImputations;
  }

  /**
   * Get the entities that are not valid. The aggregation result contains all the entities that were specified in
   * the {@link AggregationOptions} when {@link MetricSampleAggregator#aggregate(long, long, AggregationOptions)}
   * is invoked. Some of those entities may not be valid (e.g, missing data or completely unknown) those entities
   * are considered as invalid entities.
   * 
   * <p>
   *   The invalid entities returned by this method is not a complementary set of the entities returned by 
   *   {@link MetricSampleCompleteness#coveredEntities()}. The covered entities in {@link MetricSampleCompleteness}
   *   are the entities that meets the completeness requirement in {@link AggregationOptions}. It is possible for 
   *   a entity to be valid but excluded from the {@link MetricSampleCompleteness#coveredEntities()}. For example,
   * </p>
   * <p>
   *   If the {@link AggregationOptions} specifies aggregation granularity to be 
   *   {@link AggregationOptions.Granularity#ENTITY_GROUP} and an <tt>entity</tt> belongs to a group which has 
   *   other invalid entities. In this case, <tt>entity</tt> itself is still a valid entity therefore it will 
   *   not be in the set returned by this method. But since the entity group does not meet the completeness 
   *   requirement, the entire entity group is not considered as <i>"covered"</i>. So <tt>entity</tt> will not 
   *   be included in the {@link MetricSampleCompleteness#coveredEntities()} either.
   * </p>
   * 
   * @return the invalid entity set for this aggregation.
   */
  public Set<E> invalidEntities() {
    return _invalidEntities;
  }

  /**
   * Get the completeness summary of this aggregation result.
   * 
   * @see MetricSampleCompleteness 
   * 
   * @return the completeness summary of this aggregation result.
   */
  public MetricSampleCompleteness<G, E> completeness() {
    return _completeness;
  }

  // package private for modification.
  void addResult(E entity, ValuesAndImputations valuesAndImputations) {
    _entityValuesAndImputations.put(entity, valuesAndImputations);
  }

  void recordInvalidEntity(E entity) {
    _invalidEntities.add(entity);
  }

  @Override
  public void setGeneration(Long generation) {
    throw new RuntimeException("The generation of the MetricSampleAggregationResult is immutable.");
  }
}
