package guillermo.lagos.svtl.di

import guillermo.lagos.svtl.file.FileVM
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { FileVM(get()) }
}