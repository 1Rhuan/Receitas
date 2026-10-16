package cc.rhuan.receitas.di

import cc.rhuan.receitas.text.AndroidTextResolver
import cc.rhuan.receitas.text.TextResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TextModule {
    @Binds abstract fun bindTextResolver(implementation: AndroidTextResolver): TextResolver
}
