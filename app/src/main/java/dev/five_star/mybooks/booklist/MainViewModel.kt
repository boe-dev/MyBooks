package dev.five_star.mybooks.booklist

import androidx.lifecycle.*
import androidx.navigation.NavDirections
import dev.five_star.mybooks.data.BookRepository
import dev.five_star.mybooks.data.Book
import dev.five_star.mybooks.ui_common.BookItem
import dev.five_star.mybooks.ui_common.toItem
import dev.five_star.mybooks.utils.SingleLiveEvent
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class MainViewModel(private var repository: BookRepository) : ViewModel() {

    val bookList: LiveData<List<BookItem>> = repository.getAllBooks().mapLatest { it ->
        it.map { it.toItem() }
    }.asLiveData()

    private val _navigateTo = MutableLiveData<NavDirections>()
    val navigateTo: LiveData<NavDirections> = _navigateTo

    private val _openBook = MutableLiveData<Book>()
    val openBook: LiveData<Book> = _openBook

    private val _effects = SingleLiveEvent<Effect>()
    val effect: LiveData<Effect> = _effects

    private val _dialogEffect = SingleLiveEvent<DialogEffect>()
    val dialogEffect: LiveData<DialogEffect> = _dialogEffect

//    private fun getBookList() {
//        val bookItemList: List<BookItem> = repository.getAllBooks().map { it.toBookItem() }
//        _bookList.postValue(bookItemList)
//    }

    private fun openBookDetails(bookId: Int) {
        _effects.postValue(Effect.ShowDetails(bookId))
    }

    private fun addBook(bookTitle: String,  bookPages: Int) {
        if(bookTitle.isNotEmpty() && bookPages > 0) {
            // are there any differents?
            viewModelScope.launch {
                val entered = repository.addBook(title = bookTitle, pages = bookPages)
                if (entered) {
                    _dialogEffect.postValue(DialogEffect.CloseAddBook)
                } else {
                    _dialogEffect.postValue(DialogEffect.InputError)
                }
            }

        } else {
            _dialogEffect.postValue(DialogEffect.InputError)
        }
    }

    fun dataInput(event: Event) = when(event) {
        Event.ShowAddBook -> {
            _effects.postValue(Effect.AddBook)
        }
        is Event.SelectItem -> {
            openBookDetails(event.bookId)
        }
        is Event.AddBook -> {
            val bookTitle = event.bookTitleInput.trim()
            val bookPages: Int = if (event.bookPagesInput.isEmpty()) 0 else event.bookPagesInput.toInt()
            addBook(bookTitle, bookPages)
        }
    }

    sealed class Event {
        object ShowAddBook : Event()
        data class AddBook(val bookTitleInput: String, val bookPagesInput: String) : Event()
        data class SelectItem(val bookId: Int) : Event()
    }

    sealed class Effect {
        data class ShowDetails(val bookId: Int) : Effect()
        object AddBook : Effect()
    }

    sealed class DialogEffect {
        object CloseAddBook : DialogEffect()
        object InputError: DialogEffect()
    }
}

