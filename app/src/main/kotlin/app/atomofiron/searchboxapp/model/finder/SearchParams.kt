package app.atomofiron.searchboxapp.model.finder

import android.os.Parcel
import android.os.Parcelable

data class SearchParams(
    val query: String,
    val useRegex: Boolean,
    val ignoreCase: Boolean,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(query)
        parcel.writeByte(if (useRegex) 1 else 0)
        parcel.writeByte(if (ignoreCase) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SearchParams> {
        override fun createFromParcel(parcel: Parcel) = SearchParams(parcel)
        override fun newArray(size: Int): Array<SearchParams?> = arrayOfNulls(size)
    }
}