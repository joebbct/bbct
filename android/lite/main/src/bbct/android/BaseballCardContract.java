/*
 * This file is part of BBCT for Android.
 *
 * Copyright 2012 codeguru <codeguru@users.sourceforge.net>
 *
 * BBCT for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BBCT for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bbct.android;

/**
 *
 * @author codeguru <codeguru@users.sourceforge.net>
 */
public final class BaseballCardContract {

    /**
     * The table name to use in the underlying database.
     */
    public static final String TABLE_NAME = "baseball_cards";
    /**
     * The column name for the primary key.
     */
    public static final String ID_COL_NAME = "_id";
    /**
     * The column name for the card brand.
     */
    public static final String BRAND_COL_NAME = "brand";
    /**
     * The column name for the card year.
     */
    public static final String YEAR_COL_NAME = "year";
    /**
     * The column name for the card number.
     */
    public static final String NUMBER_COL_NAME = "number";
    /**
     * The column name for the card value.
     */
    public static final String VALUE_COL_NAME = "value";
    /**
     * The column name for the card count.
     */
    public static final String COUNT_COL_NAME = "card_count";
    /**
     * The column name for the player's name.
     */
    public static final String PLAYER_NAME_COL_NAME = "player_name";
    /**
     * The column name for the player's position.
     */
    public static final String PLAYER_POSITION_COL_NAME = "player_position";
    /**
     * Convenience variable that can be used when the ContentResolver wants
     * every column from the ContentProvider.
     */
    public static final String[] PROJECTION = {BRAND_COL_NAME, YEAR_COL_NAME, NUMBER_COL_NAME, VALUE_COL_NAME,
        COUNT_COL_NAME, PLAYER_NAME_COL_NAME, PLAYER_POSITION_COL_NAME};
}
