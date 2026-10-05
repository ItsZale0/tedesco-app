package com.alessandro.tedesco.ui;

import com.alessandro.tedesco.data.WordRepository;
import com.alessandro.tedesco.settings.SettingsStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class TedescoViewModel_Factory implements Factory<TedescoViewModel> {
  private final Provider<WordRepository> repoProvider;

  private final Provider<SettingsStore> settingsProvider;

  public TedescoViewModel_Factory(Provider<WordRepository> repoProvider,
      Provider<SettingsStore> settingsProvider) {
    this.repoProvider = repoProvider;
    this.settingsProvider = settingsProvider;
  }

  @Override
  public TedescoViewModel get() {
    return newInstance(repoProvider.get(), settingsProvider.get());
  }

  public static TedescoViewModel_Factory create(Provider<WordRepository> repoProvider,
      Provider<SettingsStore> settingsProvider) {
    return new TedescoViewModel_Factory(repoProvider, settingsProvider);
  }

  public static TedescoViewModel newInstance(WordRepository repo, SettingsStore settings) {
    return new TedescoViewModel(repo, settings);
  }
}
