package com.alessandro.tedesco.data;

import com.alessandro.tedesco.data.local.AppDatabase;
import com.alessandro.tedesco.data.remote.FeedService;
import com.alessandro.tedesco.settings.SettingsStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.serialization.json.Json;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.alessandro.tedesco.di.IoDispatcher")
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
public final class WordRepository_Factory implements Factory<WordRepository> {
  private final Provider<AppDatabase> dbProvider;

  private final Provider<FeedService> serviceProvider;

  private final Provider<SettingsStore> settingsProvider;

  private final Provider<Json> jsonProvider;

  private final Provider<CoroutineDispatcher> ioProvider;

  public WordRepository_Factory(Provider<AppDatabase> dbProvider,
      Provider<FeedService> serviceProvider, Provider<SettingsStore> settingsProvider,
      Provider<Json> jsonProvider, Provider<CoroutineDispatcher> ioProvider) {
    this.dbProvider = dbProvider;
    this.serviceProvider = serviceProvider;
    this.settingsProvider = settingsProvider;
    this.jsonProvider = jsonProvider;
    this.ioProvider = ioProvider;
  }

  @Override
  public WordRepository get() {
    return newInstance(dbProvider.get(), serviceProvider.get(), settingsProvider.get(), jsonProvider.get(), ioProvider.get());
  }

  public static WordRepository_Factory create(Provider<AppDatabase> dbProvider,
      Provider<FeedService> serviceProvider, Provider<SettingsStore> settingsProvider,
      Provider<Json> jsonProvider, Provider<CoroutineDispatcher> ioProvider) {
    return new WordRepository_Factory(dbProvider, serviceProvider, settingsProvider, jsonProvider, ioProvider);
  }

  public static WordRepository newInstance(AppDatabase db, FeedService service,
      SettingsStore settings, Json json, CoroutineDispatcher io) {
    return new WordRepository(db, service, settings, json, io);
  }
}
