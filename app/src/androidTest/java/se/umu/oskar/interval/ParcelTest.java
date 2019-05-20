package se.umu.oskar.interval;

import android.os.Parcel;
import android.os.Parcelable;

import static org.junit.Assert.assertEquals;

abstract class ParcelTest {

    Parcel makeParcel(Parcelable before) {
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        before.writeToParcel(parcel, 0);
        return parcel;
    }

    void assertSame(int n1, int n2) {
        assertEquals(n2, n1);
    }
}

