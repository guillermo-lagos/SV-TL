package guillermo.lagos.svtl.di

import guillermo.lagos.svtl.file.FileUtil
import org.koin.dsl.module

val applicationModule = module {
    single { FileUtil() }
}