package com.alessandro.tedesco;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class TedescoApp_MembersInjector implements MembersInjector<TedescoApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public TedescoApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<TedescoApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new TedescoApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(TedescoApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.alessandro.tedesco.TedescoApp.workerFactory")
  public static void injectWorkerFactory(TedescoApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
