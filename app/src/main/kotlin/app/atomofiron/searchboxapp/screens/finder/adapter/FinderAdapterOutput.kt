package app.atomofiron.searchboxapp.screens.finder.adapter

import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.*

interface FinderAdapterOutput<Result : SearchResult> :
        QueryFieldHolder.OnActionListener,
        CharactersHolder.OnActionListener,
        EditOptionsHolder.FinderConfigListener,
        EditCharactersHolder.OnEditCharactersListener,
        EditMaxDepthHolder.OnEditMaxDepthListener,
        TestHolder.OnTestChangeListener,
        EditMaxSizeHolder.OnEditMaxSizeListener,
        ButtonsHolder.FinderButtonsListener,
        TargetsHolder.FinderTargetsOutput,
        TaskHolder.OnActionListener<Result>