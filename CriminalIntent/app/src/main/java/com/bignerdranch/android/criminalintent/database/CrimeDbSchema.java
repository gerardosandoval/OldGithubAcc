package com.bignerdranch.android.criminalintent.database;

/**
 * Created by GSMgo on 5/19/16.
 */
public class CrimeDbSchema {

    //this is just the creation of the schema of the table and the columns
    public static final class CrimeTable{
        public static final String NAME = "crimes";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";

        }
    }
}
