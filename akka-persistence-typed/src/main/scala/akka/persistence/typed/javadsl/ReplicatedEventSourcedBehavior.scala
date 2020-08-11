/*
 * Copyright (C) 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.persistence.typed.javadsl

import java.util.Optional

import akka.actor.typed.BackoffSupervisorStrategy
import akka.actor.typed.Behavior
import akka.actor.typed.TypedActorContext
import akka.annotation.ApiMayChange
import akka.annotation.InternalApi
import akka.persistence.typed.internal.ReplicationContextImpl

/**
 * Base class for replicated event sourced behaviors.
 */
@ApiMayChange
abstract class ReplicatedEventSourcedBehavior[Command, Event, State](
    replicationContext: ReplicationContext,
    onPersistFailure: Optional[BackoffSupervisorStrategy])
    extends EventSourcedBehavior[Command, Event, State](replicationContext.persistenceId, onPersistFailure) {

  def this(replicationContext: ReplicationContext) = this(replicationContext, Optional.empty())

  /**
   * Override and return true to publish events to the system event stream as
   * [[akka.persistence.typed.PublishedEvent]] after they have been persisted.
   */
  def withEventPublishing: Boolean = false

  protected def getReplicationContext(): ReplicationContext = replicationContext

  /**
   * INTERNAL API: DeferredBehavior init, not for user extension
   */
  @InternalApi override def apply(context: TypedActorContext[Command]): Behavior[Command] = {
    createEventSourcedBehavior()
    // context not user extendable so there should never be any other impls
      .withReplication(replicationContext.asInstanceOf[ReplicationContextImpl])
      .withEventPublishing(withEventPublishing)
  }
}