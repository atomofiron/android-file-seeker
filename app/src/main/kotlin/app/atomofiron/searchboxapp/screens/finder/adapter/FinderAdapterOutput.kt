package app.atomofiron.searchboxapp.screens.finder.adapter

import app.atomofiron.searchboxapp.screens.finder.adapter.holder.*

interface FinderAdapterOutput :
        FieldHolder.OnActionListener,
        CharactersHolder.OnActionListener,
        ConfigHolder.FinderConfigListener,
        ButtonsHolder.FinderButtonsListener,
        TargetsHolder.FinderTargetsOutput,
        ProgressHolder.OnActionListener