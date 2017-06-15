package org.infinispan.query.impl.massindex;

import java.util.function.Function;

import org.hibernate.search.backend.FlushLuceneWork;
import org.hibernate.search.backend.LuceneWork;
import org.hibernate.search.backend.PurgeAllLuceneWork;
import org.hibernate.search.backend.impl.StreamingOperationDispatcher;
import org.hibernate.search.backend.impl.batch.DefaultBatchBackend;
import org.hibernate.search.backend.spi.BatchBackend;
import org.hibernate.search.backend.spi.OperationDispatcher;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.spi.IndexedTypeIdentifier;
import org.hibernate.search.spi.IndexedTypeSet;
import org.hibernate.search.spi.SearchIntegrator;

/**
 * Decorates {@link org.hibernate.search.backend.impl.batch.DefaultBatchBackend} adding capacity of doing
 * synchronous purges and flushes.
 *
 * @author gustavonalle
 * @since 7.2
 */
public class ExtendedBatchBackend implements BatchBackend {
   private final DefaultBatchBackend defaultBatchBackend;
   private final MassIndexerProgressMonitor progressMonitor;
   private final OperationDispatcher streamingDispatcher;

   public ExtendedBatchBackend(SearchIntegrator integrator, MassIndexerProgressMonitor progressMonitor) {
      this.progressMonitor = progressMonitor;
      this.defaultBatchBackend = new DefaultBatchBackend(integrator.unwrap(ExtendedSearchIntegrator.class), progressMonitor);
      this.streamingDispatcher = new StreamingOperationDispatcher( integrator, true /* forceAsync */ );
   }

   public void purge(IndexedTypeSet entityTypes) {
      performTypeScopedOperations(entityTypes, type -> new PurgeAllLuceneWork(type));
      flush(entityTypes);
   }

   @Override
   public void enqueueAsyncWork(LuceneWork work) throws InterruptedException {
      defaultBatchBackend.enqueueAsyncWork(work);
   }

   @Override
   public void awaitAsyncProcessingCompletion() {
      defaultBatchBackend.awaitAsyncProcessingCompletion();
   }

   @Override
   public void doWorkInSync(LuceneWork work) {
      defaultBatchBackend.doWorkInSync(work);
   }

   @Override
   public void flush(IndexedTypeSet entityTypes) {
      performTypeScopedOperations(entityTypes, type -> new FlushLuceneWork(null, type));
   }

   @Override
   public void optimize(IndexedTypeSet entityTypes) {
      defaultBatchBackend.optimize(entityTypes);
   }

   private void performTypeScopedOperations(IndexedTypeSet entityTypes, Function<IndexedTypeIdentifier, LuceneWork> workFactory) {
      for (IndexedTypeIdentifier type : entityTypes) {
         streamingDispatcher.dispatch( workFactory.apply( type ), progressMonitor );
      }
   }
}
