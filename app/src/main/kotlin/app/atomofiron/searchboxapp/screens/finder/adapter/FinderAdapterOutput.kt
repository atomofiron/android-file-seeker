package app.atomofiron.searchboxapp.screens.finder.adapter

import app.atomofiron.searchboxapp.screens.finder.adapter.holder.*

interface FinderAdapterOutput :
        QueryFieldHolder.OnActionListener,
        CharactersHolder.OnActionListener,
        OptionsHolder.FinderConfigListener,
        EditCharactersHolder.OnEditCharactersListener,
        EditMaxDepthHolder.OnEditMaxDepthListener,
        TestHolder.OnTestChangeListener,
        EditMaxSizeHolder.OnEditMaxSizeListener,
        ButtonsHolder.FinderButtonsListener,
        TargetsHolder.FinderTargetsOutput,
        TaskHolder.OnActionListener