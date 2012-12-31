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

import android.app.Activity;
import android.app.Instrumentation;
import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import bbct.common.data.BaseballCard;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;

/**
 *
 * @author codeguru <codeguru@users.sourceforge.net>
 */
public class BaseballCardListWithDataTest extends ActivityInstrumentationTestCase2<BaseballCardList> {

    public BaseballCardListWithDataTest() {
        super(BaseballCardList.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.inst = this.getInstrumentation();

        InputStream cardInputStream = this.inst.getContext().getAssets().open(DATA_ASSET);
        this.cardInput = new BaseballCardCsvFileReader(cardInputStream, true);
        this.allCards = this.cardInput.getAllBaseballCards();

        // Create the database and populate table with test data
        BaseballCardSQLHelper sqlHelper = new BaseballCardSQLHelper(this.inst.getTargetContext());
        this.dbUtil = new DatabaseUtil();
        this.dbUtil.populateTable(allCards);

        this.activity = this.getActivity();
        this.listView = (ListView) this.activity.findViewById(android.R.id.list);

        this.newCard = new BaseballCard("codeguru apps", 1993, 1, 50000, 1, "codeguru", "Catcher");
    }

    @Override
    public void tearDown() throws Exception {
        this.activity.finish();
        this.dbUtil.deleteDatabase();
        this.cardInput.close();

        super.tearDown();
    }

    public void testPreConditions() {
        Assert.assertNotNull(this.activity);
        Assert.assertNotNull(this.listView);

        // Check that database was created with the correct version and table
        SQLiteDatabase db = this.dbUtil.getDatabase();
        Assert.assertNotNull(db);
        Assert.assertEquals(BaseballCardSQLHelper.SCHEMA_VERSION, db.getVersion());
        Assert.assertEquals(BaseballCardContract.TABLE_NAME, SQLiteDatabase.findEditTable(BaseballCardContract.TABLE_NAME));

        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards, this.listView);
    }

    public void testStateDestroyWithoutFilter() {
        this.activity.finish();
        Assert.assertTrue(this.activity.isFinishing());
        this.activity = this.getActivity();
        this.listView = (ListView) this.activity.findViewById(android.R.id.list);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards, this.listView);
    }

    public void testStateDestroyWithFilter() {
        this.testYearFilter();
        this.activity.finish();
        Assert.assertTrue(this.activity.isFinishing());
        this.activity = this.getActivity();
        this.listView = (ListView) this.activity.findViewById(android.R.id.list);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards, this.listView);
    }

    public void testStateDestroyClearFilter() {
        this.testClearFilter();
        this.activity.finish();
        Assert.assertTrue(this.activity.isFinishing());
        this.activity = this.getActivity();
        this.listView = (ListView) this.activity.findViewById(android.R.id.list);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards, this.listView);
    }

    public void testStatePauseWithoutFilter() {
        this.inst.callActivityOnRestart(this.activity);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards, this.listView);
    }

    public void testStatePauseWithFilter() {
        this.testYearFilter();
        this.inst.callActivityOnRestart(this.activity);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards, this.listView);
    }

    public void testStatePauseClearFilter() {
        this.testClearFilter();
        this.inst.callActivityOnRestart(this.activity);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards, this.listView);
    }

    public void testMenuLayoutWithoutFilter() {
        this.sendKeys(KeyEvent.KEYCODE_MENU);
        Assert.fail("Now how do I check the contents of the menu?");
    }

    public void testMenuLayoutWithFilter() {
        this.testYearFilter();
        this.sendKeys(KeyEvent.KEYCODE_MENU);
        Assert.fail("Now how do I check the contents of the menu?");
    }

    /**
     * Test of {@link BaseballCardList#onListItemClick}.
     */
    public void testOnListItemClick() {
        Instrumentation.ActivityMonitor detailsMonitor = new Instrumentation.ActivityMonitor(BaseballCardDetails.class.getName(), null, false);
        this.inst.addMonitor(detailsMonitor);

        int cardIndex = (int) (Math.random() * this.allCards.size());
        this.sendRepeatedKeys(cardIndex, KeyEvent.KEYCODE_DPAD_DOWN, 1, KeyEvent.KEYCODE_DPAD_CENTER);

        Activity cardDetails = this.inst.waitForMonitorWithTimeout(detailsMonitor, TIME_OUT);
        Assert.assertNotNull(cardDetails);

        EditText brandText = (EditText) cardDetails.findViewById(R.id.brand_text);
        EditText yearText = (EditText) cardDetails.findViewById(R.id.year_text);
        EditText numberText = (EditText) cardDetails.findViewById(R.id.number_text);
        EditText valueText = (EditText) cardDetails.findViewById(R.id.value_text);
        EditText countText = (EditText) cardDetails.findViewById(R.id.count_text);
        EditText playerNameText = (EditText) cardDetails.findViewById(R.id.player_name_text);
        Spinner playerPositionSpinner = (Spinner) cardDetails.findViewById(R.id.player_position_text);

        BaseballCard expectedCard = this.allCards.get(cardIndex);
        Assert.assertEquals(expectedCard.getBrand(), brandText.getText().toString());
        Assert.assertEquals(expectedCard.getYear(), Integer.parseInt(yearText.getText().toString()));
        Assert.assertEquals(expectedCard.getNumber(), Integer.parseInt(numberText.getText().toString()));
        Assert.assertEquals(expectedCard.getCount(), Integer.parseInt(countText.getText().toString()));
        Assert.assertEquals(expectedCard.getValue(), (int) (Double.parseDouble(valueText.getText().toString()) * 100));
        Assert.assertEquals(expectedCard.getPlayerName(), playerNameText.getText().toString());
        Assert.assertEquals(expectedCard.getPlayerPosition(), playerPositionSpinner.getSelectedItem().toString());

        BBCTTestUtil.clickCardDetailsDone(this.inst, cardDetails);
    }

    public void testAddDuplicateCard() throws IOException {
        this.cardInput.close();
        InputStream cardInputStream = this.inst.getContext().getAssets().open(DATA_ASSET);
        this.cardInput = new BaseballCardCsvFileReader(cardInputStream, true);
        BaseballCard card = this.cardInput.getNextBaseballCard();

        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst, this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, card);
        //Assert.fail("Check that error message is displayed.");
        BBCTTestUtil.clickCardDetailsDone(this.inst, cardDetails);
        Assert.fail("Check that error message is displayed.");
    }

    public void testAddCardToPopulatedDatabase() {
        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst, this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, this.newCard);
        BBCTTestUtil.clickCardDetailsDone(this.inst, cardDetails);

        this.allCards.add(this.newCard);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards, this.listView);
    }

    public void testAddCardMatchingCurrentFilter() {
        this.testYearFilter();

        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst, this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, this.newCard);
        BBCTTestUtil.clickCardDetailsDone(this.inst, cardDetails);

        this.expectedCards.add(this.newCard);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards, this.listView);
    }

    public void testAddCardNotMatchingCurrentFilter() {
        this.testYearFilter();

        this.newCard = new BaseballCard("codeguru apps", 1976, 1, 50000, 1, "codeguru", "Catcher");
        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst, this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, this.newCard);
        BBCTTestUtil.clickCardDetailsDone(this.inst, cardDetails);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards, this.listView);
    }

    public void testAddCardAfterClearFilter() {
        this.testClearFilter();
        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst, this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, this.newCard);
        BBCTTestUtil.clickCardDetailsDone(this.inst, cardDetails);

        this.allCards.add(this.newCard);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards, this.listView);
    }

    public void testYearFilter() {
        final int year = 1993;
        FilterInput yearInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst.sendStringSync(Integer.toString(year));
            }
        };

        Predicate<BaseballCard> yearPred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return card.getYear() == year;
            }
        };

        this.testFilter(YearFilter.class, R.id.year_filter_radio_button, yearInput, yearPred);
    }

    public void testNumberFilter() {
        final int number = 278;
        FilterInput numberInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst.sendStringSync(Integer.toString(number));
            }
        };

        Predicate<BaseballCard> numberPred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return card.getNumber() == number;
            }
        };

        this.testFilter(NumberFilter.class, R.id.number_filter_radio_button, numberInput, numberPred);
    }

    public void testYearAndNumberFilter() {
        final int year = 1993;
        final int number = 18;
        FilterInput yearAndNumberInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst.sendStringSync(Integer.toString(year));
                BaseballCardListWithDataTest.this.sendKeys(KeyEvent.KEYCODE_ENTER);
                BaseballCardListWithDataTest.this.inst.sendStringSync(Integer.toString(number));
            }
        };

        Predicate<BaseballCard> yearAndNumberPred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return card.getYear() == year && card.getNumber() == number;
            }
        };

        this.testFilter(YearAndNumberFilter.class, R.id.year_and_number_filter_radio_button, yearAndNumberInput, yearAndNumberPred);
    }

    public void testPlayerNameFilter() {
        final String playerName = "Ken Griffey Jr.";
        FilterInput playerNameInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst.sendStringSync(playerName);
            }
        };

        Predicate<BaseballCard> playerNamePred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return playerName.equals(card.getPlayerName());
            }
        };

        this.testFilter(PlayerNameFilter.class, R.id.player_name_filter_radio_button, playerNameInput, playerNamePred);
    }

    public void testClearFilter() {
        this.testYearFilter();
        Assert.assertTrue(this.inst.invokeMenuActionSync(this.activity, R.id.clear_filter_menu, 0));
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards, this.listView);
    }

    private void testFilter(Class<?> filterClass, int radioButtonId, FilterInput filterInput, Predicate<BaseballCard> filterPred) {
        Instrumentation.ActivityMonitor filterMonitor = new Instrumentation.ActivityMonitor(filterClass.getName(), null, false);
        this.inst.addMonitor(filterMonitor);

        Activity filterOptions = BBCTTestUtil.testMenuItem(this.inst, this.activity, R.id.filter_menu, FilterOptions.class);
        final Button optionsOkButton = (Button) filterOptions.findViewById(R.id.ok_button);
        final RadioButton filterRadioButton = (RadioButton) filterOptions.findViewById(radioButtonId);

        this.inst.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Assert.assertFalse(filterRadioButton.performClick());
                Assert.assertTrue(optionsOkButton.performClick());
            }
        });

        Activity filter = this.inst.waitForMonitorWithTimeout(filterMonitor, TIME_OUT);
        Assert.assertNotNull(filter);
        final Button filterOkButton = (Button) filter.findViewById(R.id.ok_button);

        filterInput.doInput();

        this.inst.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(filterOkButton.performClick());
            }
        });

        Assert.assertTrue(filter.isFinishing());
        this.inst.waitForIdleSync();
        Assert.assertTrue(filterOptions.isFinishing());

        this.expectedCards = this.filterList(this.allCards, filterPred);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards, this.listView);
    }

    private List<BaseballCard> filterList(List<BaseballCard> list, Predicate<BaseballCard> pred) {
        List<BaseballCard> filteredList = new ArrayList<BaseballCard>();

        for (BaseballCard obj : list) {
            if (pred.doTest(obj)) {
                filteredList.add(obj);
            }
        }

        return filteredList;
    }

    private interface FilterInput {

        public void doInput();
    }
    private List<BaseballCard> allCards;
    private List<BaseballCard> expectedCards;
    private Instrumentation inst = null;
    private Activity activity = null;
    private BaseballCardCsvFileReader cardInput = null;
    private DatabaseUtil dbUtil = null;
    private ListView listView = null;
    private BaseballCard newCard = null;
    private static final String DATA_ASSET = "cards.csv";
    private static final int TIME_OUT = 5 * 1000; // 5 seconds
}
