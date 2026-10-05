package com.alessandro.tedesco.sync;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.alessandro.tedesco.data.WordRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class SyncWorker_Factory {
  private final Provider<WordRepository> repoProvider;

  public SyncWorker_Factory(Provider<WordRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  public SyncWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, repoProvider.get());
  }

  public static SyncWorker_Factory create(Provider<WordRepository> repoProvider) {
    return new SyncWorker_Factory(repoProvider);
  }

  public static SyncWorker newInstance(Context context, WorkerParameters params,
      WordRepository repo) {
    return new SyncWorker(context, params, repo);
  }
}
