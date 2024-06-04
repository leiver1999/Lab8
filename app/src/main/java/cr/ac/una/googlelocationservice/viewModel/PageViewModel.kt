package cr.ac.una.googlelocationservice.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cr.ac.una.googlelocationservice.entity.Page
import cr.ac.una.googlelocationservice.service.WikipediaService

class PageViewModel : ViewModel() {
    private var _pages: MutableLiveData<List<Page>?> = MutableLiveData()
    var pages = _pages

    var wikipediaService = WikipediaService()

    suspend fun startLoadingPages(title: String) {
        _pages.postValue(listOf())
        var lista = searchPages(title)
        _pages.postValue(lista)
    }

    private suspend fun searchPages(title: String): ArrayList<Page>? {
        return wikipediaService.apiService.getRelatedPages(title).pages
    }

    suspend fun getPagesForNotification(title: String): ArrayList<Page>? {
        return wikipediaService.apiService.getRelatedPages(title).pages
    }

    fun getPages() {

    }
}