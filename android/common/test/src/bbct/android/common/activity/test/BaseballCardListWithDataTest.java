/*
 * This file is part of BBCT for Android.
 *
 * Copyright 2012-14 codeguru <codeguru@users.sourceforge.net>
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
package bbct.android.common.activity.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import bbct.android.common.R;
import bbct.android.common.activity.BaseballCardDetails;
import bbct.android.common.activity.BaseballCardList;
import bbct.android.common.activity.FilterOptions;
import bbct.android.common.activity.filter.FilterActivity;
import bbct.android.common.activity.filter.NumberFilter;
import bbct.android.common.activity.filter.PlayerNameFilter;
import bbct.android.common.activity.filter.TeamFilter;
import bbct.android.common.activity.filter.YearAndNumberFilter;
import bbct.android.common.activity.filter.YearFilter;
import bbct.android.common.data.BaseballCard;
import bbct.android.common.test.BBCTTestUtil;
import bbct.android.common.test.BaseballCardCsvFileReader;
import bbct.android.common.test.DatabaseUtil;
import bbct.android.common.test.Predicate;
import com.robotium.solo.Solo;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;

/**
 * Tests for the {@link BaseballCardList} activity when the database contains
 * data.
 */
public class BaseballCardListWithDataTest extends
        ActivityInstrumentationTestCase2<BaseballCardList> {

    /**
     * Create instrumented test cases for {@link BaseballCardList}.
     */
    public BaseballCardListWithDataTest() {
        super(BaseballCardList.class);
    }

    /**
     * Set up test fixture. This consists of an instance of the
     * {@link BaseballCardList} activity, its {@link ListView}, and a populated
     * database.
     *
     * @throws Exception
     *             If an error occurs while chaining to the super class.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.inst = this.getInstrumentation();

        // Create the database and populate table with test data
        InputStream cardInputStream = this.inst.getContext().getAssets()
                .open(BBCTTestUtil.CARD_DATA);
        BaseballCardCsvFileReader cardInput = new BaseballCardCsvFileReader(
                cardInputStream, true);
        this.allCards = cardInput.getAllBaseballCards();
        cardInput.close();

        this.dbUtil = new DatabaseUtil(this.inst.getTargetContext());
        this.dbUtil.populateTable(this.allCards);

        // Start Activity
        this.activity = this.getActivity();
        this.listView = (ListView) this.activity
                .findViewById(android.R.id.list);
        this.newCard = new BaseballCard("Code Guru Apps", 1993, 1, 50000, 1,
                "Code Guru", "Code Guru Devs", "Catcher");

        this.solo = new Solo(this.inst, this.activity);
    }

    /**
     * Tear down the test fixture by calling {@link Activity#finish()} and
     * deleting the database.
     *
     * @throws Exception
     *             If an error occurs while chaining to the super class.
     */
    @Override
    public void tearDown() throws Exception {
        this.solo.finishOpenedActivities();
        this.dbUtil.deleteDatabase();
        this.solo.finishOpenedActivities();

        super.tearDown();
    }

    /**
     * Check preconditions which must hold to guarantee the validity of all
     * other tests. Assert that the {@link Activity} to test and its
     * {@link ListView} are not <code>null</code>, that the {@link ListView}
     * contains the expected data, and that the database was created with the
     * correct table and populated with the correct data.
     */
    public void testPreConditions() {
        Assert.assertNotNull(this.activity);

        BBCTTestUtil.assertDatabaseCreated(this.inst.getTargetContext());
        Assert.assertTrue(this.dbUtil.containsAllBaseballCards(this.allCards));

        Assert.assertNotNull(this.listView);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards,
                this.listView);
    }

    /**
     * Test that a {@link BaseballCardList} activity without an active filter
     * will be correctly restored after it is destroyed.
     */
    public void testStateDestroyWithoutFilter() {
        this.activity.finish();
        Assert.assertTrue(this.activity.isFinishing());
        this.activity = this.getActivity();
        this.listView = (ListView) this.activity
                .findViewById(android.R.id.list);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards,
                this.listView);
    }

    /**
     * Test that a {@link BaseballCardList} activity with an active filter will
     * be correctly restored after it is destroyed.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testStateDestroyWithFilter() throws Throwable {
        this.testYearFilter();
        this.activity.finish();
        Assert.assertTrue(this.activity.isFinishing());
        this.activity = this.getActivity();
        this.listView = (ListView) this.activity
                .findViewById(android.R.id.list);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards,
                this.listView);
    }

    /**
     * Test that a {@link BaseballCardList} activity without an active filter
     * will be correctly restored after it is destroyed. This tests differs from
     * {@link #testStateDestroyWithoutFilter()} because a filter is applied and
     * then cleared before the activity is destroyed.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testStateDestroyClearFilter() throws Throwable {
        this.testClearFilter();
        this.activity.finish();
        Assert.assertTrue(this.activity.isFinishing());
        this.activity = this.getActivity();
        this.listView = (ListView) this.activity
                .findViewById(android.R.id.list);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards,
                this.listView);
    }

    /**
     * Test that a {@link BaseballCardList} activity without an active filter
     * will be correctly restored after it is paused.
     */
    public void testStatePauseWithoutFilter() {
        this.inst.callActivityOnRestart(this.activity);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards,
                this.listView);
    }

    /**
     * Test that a {@link BaseballCardList} activity with an active filter will
     * be correctly restored after it is paused.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testStatePauseWithFilter() throws Throwable {
        this.testYearFilter();
        this.inst.callActivityOnRestart(this.activity);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards,
                this.listView);
    }

    /**
     * Test that a {@link BaseballCardList} activity without an active filter
     * will be correctly restored after it is destroyed. This tests differs from
     * {@link #testStatePauseWithoutFilter()} because a filter is applied and
     * then cleared before the activity is paused.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testStatePauseClearFilter() throws Throwable {
        this.testClearFilter();
        this.inst.callActivityOnRestart(this.activity);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards,
                this.listView);
    }

    /**
     * Test that, when the user clicks on an item in the {@link ListView} of the
     * {@link BaseballCardList} activity, a {@link BaseballCardDetails} activity
     * is launched with its {@link EditText} views populated with the correct
     * data.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testOnListItemClick() throws Throwable {
        Log.d(TAG, "testOnListItemClick()");

        this.inst.waitForIdleSync();
        Instrumentation.ActivityMonitor detailsMonitor = new Instrumentation.ActivityMonitor(
                BaseballCardDetails.class.getName(), null, false);
        this.inst.addMonitor(detailsMonitor);

        int cardIndex = 3;

        Log.d(TAG, "cardIndex=" + cardIndex);

        this.sendRepeatedKeys(cardIndex, KeyEvent.KEYCODE_DPAD_DOWN, 1,
                KeyEvent.KEYCODE_DPAD_CENTER);

        Activity cardDetails = this.inst.waitForMonitorWithTimeout(
                detailsMonitor, TIME_OUT);
        Assert.assertNotNull(cardDetails);
        BaseballCard expectedCard = this.allCards.get(cardIndex - 1);
        BBCTTestUtil.assertAllEditTextContents(cardDetails, expectedCard);
        BBCTTestUtil.clickCardDetailsDone(this.solo, cardDetails);
    }

    /**
     * Test that an error message is displayed when the user attempts to add
     * baseball card data which duplicates data already in the database.
     *
     * @throws IOException
     *             If an error occurs while reading baseball card data from an
     *             asset file.
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testAddDuplicateCard() throws IOException, Throwable {
        InputStream cardInputStream = this.inst.getContext().getAssets()
                .open(BBCTTestUtil.CARD_DATA);
        BaseballCardCsvFileReader cardInput = new BaseballCardCsvFileReader(
                cardInputStream, true);
        BaseballCard card = cardInput.getNextBaseballCard();

        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst,
                this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, card);

        Assert.assertTrue(this.solo.waitForDialogToOpen());
        this.solo.clickOnButton("OK");
        Assert.assertTrue(this.solo.waitForDialogToClose());
        this.solo.finishOpenedActivities();
    }

    /**
     * Test that baseball card data is correctly added to the database when it
     * already contains data for other cards.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testAddCardToPopulatedDatabase() throws Throwable {
        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst,
                this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, this.newCard);
        BBCTTestUtil.waitForToast(this.solo, BBCTTestUtil.ADD_MESSAGE);
        BBCTTestUtil.clickCardDetailsDone(this.solo, cardDetails);

        this.allCards.add(this.newCard);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards,
                this.listView);
    }

    /**
     * Test that the {@link ListView} is updated when the user adds a new card
     * which matches the current filter.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testAddCardMatchingCurrentFilter() throws Throwable {
        this.testYearFilter();

        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst,
                this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, this.newCard);
        BBCTTestUtil.waitForToast(this.solo, BBCTTestUtil.ADD_MESSAGE);
        BBCTTestUtil.clickCardDetailsDone(this.solo, cardDetails);

        this.expectedCards.add(this.newCard);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards,
                this.listView);
    }

    /**
     * Test that the {@link ListView} is not updated when the user adds a new
     * card which does not match the current filter.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testAddCardNotMatchingCurrentFilter() throws Throwable {
        this.testYearFilter();

        this.newCard = new BaseballCard("codeguru apps", 1976, 1, 50000, 1,
                "codeguru", "codeguru devs", "Catcher");
        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst,
                this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, this.newCard);
        BBCTTestUtil.waitForToast(this.solo, BBCTTestUtil.ADD_MESSAGE);
        BBCTTestUtil.clickCardDetailsDone(this.solo, cardDetails);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards,
                this.listView);
    }

    /**
     * Test that the {@link ListView} is updated when the user adds a new card
     * after an active filter was cleared.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testAddCardAfterClearFilter() throws Throwable {
        this.testClearFilter();
        Activity cardDetails = BBCTTestUtil.testMenuItem(this.inst,
                this.activity, R.id.add_menu, BaseballCardDetails.class);
        BBCTTestUtil.addCard(this, cardDetails, this.newCard);
        BBCTTestUtil.waitForToast(this.solo, BBCTTestUtil.ADD_MESSAGE);
        BBCTTestUtil.clickCardDetailsDone(this.solo, cardDetails);

        this.allCards.add(this.newCard);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards,
                this.listView);
    }

    /**
     * Test that the "Delete Cards" menu item is disabled when no items are
     * selected in {@link ListView}.
     */
    public void testEmptySelection() {
        Assert.assertFalse(this.inst.invokeMenuActionSync(this.activity,
                R.id.delete_menu, 0));
    }

    /**
     * Test that the "Delete Cards" menu item is enabled once an item is
     * selected from {@link ListView}.
     */
    public void testSelection() throws Throwable {
        ListView lv = ((ListActivity) this.activity).getListView();
        int index = (int) (Math.random() * (lv.getChildCount() - 1)) + 1;
        final CheckedTextView ctv = (CheckedTextView) lv.getChildAt(index)
                .findViewById(R.id.checkmark);

        this.runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(ctv.performClick());
            }
        });

        Assert.assertTrue(this.inst.invokeMenuActionSync(this.activity,
                R.id.delete_menu, 0));
    }

    /**
     * Test that upon clicking on header {@link View}, all items in
     * {@link ListView} are selected.
     */
    public void testMarkAll() throws Throwable {
        ListView lv = ((ListActivity) this.activity).getListView();
        final CheckedTextView header = (CheckedTextView) lv.getChildAt(0)
                .findViewById(R.id.checkmark);

        this.runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(header.performClick());
            }
        });

        int numMarked = 0;
        for (int i = 0; i < lv.getChildCount(); i++) {
            CheckedTextView ctv = (CheckedTextView) lv.getChildAt(i)
                    .findViewById(R.id.checkmark);

            if (ctv.isChecked()) {
                numMarked++;
            }
        }

        Assert.assertEquals(lv.getChildCount(), numMarked);
    }

    /**
     * Test that the {@link ListView} displays updated card list, when user
     * deletes cards with applied filter.
     */
    public void testDeleteCardUsingFilter() throws Throwable {
        this.testYearFilter();

        ListView lv = ((ListActivity) this.activity).getListView();
        int cardIndex = (int) (Math.random() * (lv.getChildCount() - 1) + 1);
        View v = lv.getChildAt(cardIndex);
        BaseballCard toDelete = this.getBaseballCardFromView(v);

        this.expectedCards = new ArrayList<BaseballCard>();
        for (int i = 1; i < lv.getChildCount(); i++) {
            BaseballCard bc = this.getBaseballCardFromView(lv.getChildAt(i));
            if (!bc.equals(toDelete)) {
                this.expectedCards.add(bc);
            }
        }

        BBCTTestUtil.removeCard(this, this.activity, toDelete);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards,
                lv);
    }

    /**
     * Test that the {@link ListView} displays updated card list, when user
     * deletes cards without any applied filter.
     */
    public void testDeleteCardNoFilter() throws Throwable {
        this.testClearFilter();

        ListView lv = ((ListActivity) this.activity).getListView();
        int cardIndex = (int) (Math.random() * (lv.getChildCount() - 1) + 1);
        View v = lv.getChildAt(cardIndex);
        BaseballCard toDelete = this.getBaseballCardFromView(v);

        this.expectedCards = new ArrayList<BaseballCard>(this.allCards);
        this.expectedCards.remove(toDelete);

        BBCTTestUtil.removeCard(this, this.activity, toDelete);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards,
                lv);
    }

    /**
     * Test that the state of {@link CheckedTextView} is maintained when the
     * {@link BaseballCardList} activity changes orientation.
     */
    @UiThreadTest
    public void testSelectionAfterSaveInstanceState() throws Throwable {
        Log.d(TAG, "testSelectionAfterSaveInstanceState()");

        ListView lv = ((ListActivity) this.activity).getListView();
        ArrayList<Integer> indexes = new ArrayList<Integer>();

        for (int i = 0; i < 3; i++) {
            int cardIndex = (int) (Math.random() * (lv.getChildCount() - 1) + 1);
            final CheckedTextView ctv = (CheckedTextView) lv.getChildAt(
                    cardIndex).findViewById(R.id.checkmark);

            if (!ctv.isChecked()) {
                indexes.add(cardIndex);
                Assert.assertTrue(ctv.performClick());
            }
        }

        Log.d(TAG, "change orientation");
        this.activity
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Log.d(TAG, "assertions");
        lv = ((ListActivity) this.activity).getListView();
        Log.d(TAG, "lv.getChildCount()=" + lv.getChildCount());
        for (int i = 0; i < indexes.size(); i++) {
            View row = lv.getChildAt(indexes.get(i));
            Log.d(TAG, "row=" + row);
            CheckedTextView ctv = (CheckedTextView) row
                    .findViewById(R.id.checkmark);
            Assert.assertTrue(ctv.isChecked());
        }

        this.activity.finish();
        Log.d(TAG, "finished");
    }

    /**
     * Test that the state of {@link CheckedTextView} is maintained when a user
     * adds a new card.
     */
    public void testSelectionAfterAddCard() throws Throwable {
        ListView lv = ((ListActivity) this.activity).getListView();
        ArrayList<Integer> indexes = new ArrayList<Integer>();

        for (int i = 0; i < 3; i++) {
            int cardIndex = (int) (Math.random() * (lv.getChildCount() - 1) + 1);
            final CheckedTextView ctv = (CheckedTextView) lv.getChildAt(
                    cardIndex).findViewById(R.id.checkmark);

            if (!ctv.isChecked()) {
                indexes.add(cardIndex);

                this.runTestOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Assert.assertTrue(ctv.performClick());
                    }
                });
            }
        }

        this.testAddCardToPopulatedDatabase();

        lv = ((ListActivity) this.activity).getListView();
        for (int i = 0; i < indexes.size(); i++) {
            CheckedTextView ctv = (CheckedTextView) lv.getChildAt(
                    indexes.get(i)).findViewById(R.id.checkmark);
            Assert.assertTrue(ctv.isChecked());
        }
    }

    private BaseballCard getBaseballCardFromView(View v) {
        TextView playerName = (TextView) v
                .findViewById(R.id.player_name_text_view);
        TextView brand = (TextView) v.findViewById(R.id.brand_text_view);
        TextView year = (TextView) v.findViewById(R.id.year_text_view);
        TextView number = (TextView) v.findViewById(R.id.number_text_view);

        for (BaseballCard bc : this.allCards) {
            boolean isEqualPName = bc.getPlayerName().equals(
                    playerName.getText().toString());
            boolean isEqualBrand = bc.getBrand().equals(
                    brand.getText().toString());
            boolean isEqualYear = Integer.parseInt(year.getText().toString()) == bc
                    .getYear();
            boolean isEqualNumber = Integer.parseInt(number.getText()
                    .toString()) == bc.getNumber();

            if (isEqualPName && isEqualBrand && isEqualYear && isEqualNumber) {
                return bc;
            }
        }

        return null;
    }

    /**
     * Test that the {@link ListView} displays the correct cards when filtered
     * by the card year.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testYearFilter() throws Throwable {
        final int year = 1993;
        FilterInput yearInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst.sendStringSync(Integer
                        .toString(year));
            }
        };

        Predicate<BaseballCard> yearPred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return card.getYear() == year;
            }
        };

        this.testFilter(YearFilter.class, R.id.year_filter_radio_button,
                yearInput, yearPred);
    }

    /**
     * Test that the {@link ListView} displays the correct cards when filtered
     * by the card number.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testNumberFilter() throws Throwable {
        final int number = 278;
        FilterInput numberInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst.sendStringSync(Integer
                        .toString(number));
            }
        };

        Predicate<BaseballCard> numberPred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return card.getNumber() == number;
            }
        };

        this.testFilter(NumberFilter.class, R.id.number_filter_radio_button,
                numberInput, numberPred);
    }

    /**
     * Test that the {@link ListView} displays the correct cards when filtered
     * by the card year and number.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testYearAndNumberFilter() throws Throwable {
        final int year = 1993;
        final int number = 18;
        FilterInput yearAndNumberInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst.sendStringSync(Integer
                        .toString(year));
                BaseballCardListWithDataTest.this
                        .sendKeys(KeyEvent.KEYCODE_ENTER);
                BaseballCardListWithDataTest.this.inst.sendStringSync(Integer
                        .toString(number));
            }
        };

        Predicate<BaseballCard> yearAndNumberPred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return card.getYear() == year && card.getNumber() == number;
            }
        };

        this.testFilter(YearAndNumberFilter.class,
                R.id.year_and_number_filter_radio_button, yearAndNumberInput,
                yearAndNumberPred);
    }

    /**
     * Test that the {@link ListView} displays the correct cards when filtered
     * by the player name.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testPlayerNameFilter() throws Throwable {
        final String playerName = "Ken Griffey Jr.";
        FilterInput playerNameInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst
                        .sendStringSync(playerName);
            }
        };

        Predicate<BaseballCard> playerNamePred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return playerName.equals(card.getPlayerName());
            }
        };

        this.testFilter(PlayerNameFilter.class,
                R.id.player_name_filter_radio_button, playerNameInput,
                playerNamePred);
    }

    /**
     * Test that the {@link ListView} displays the correct cards when filtered
     * by the team name.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testTeamFilter() throws Throwable {
        final String team = "Mets";
        FilterInput teamInput = new FilterInput() {
            @Override
            public void doInput() {
                BaseballCardListWithDataTest.this.inst.sendStringSync(team);
            }
        };

        Predicate<BaseballCard> teamPred = new Predicate<BaseballCard>() {
            @Override
            public boolean doTest(BaseballCard card) {
                return team.equals(card.getTeam());
            }
        };

        this.testFilter(TeamFilter.class, R.id.team_filter_radio_button,
                teamInput, teamPred);
    }

    /**
     * Test that all cards are displayed after a filter is cleared.
     *
     * @throws Throwable
     *             If an error occurs while the portion of the test on the UI
     *             thread runs.
     */
    public void testClearFilter() throws Throwable {
        this.testYearFilter();
        Assert.assertTrue(this.inst.invokeMenuActionSync(this.activity,
                R.id.clear_filter_menu, 0));
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.allCards,
                this.listView);
    }

    private void testFilter(Class<? extends FilterActivity> filterClass,
            int radioButtonId, FilterInput filterInput,
            Predicate<BaseballCard> filterPred) throws Throwable {
        Instrumentation.ActivityMonitor filterMonitor = new Instrumentation.ActivityMonitor(
                filterClass.getName(), null, false);
        this.inst.addMonitor(filterMonitor);

        Activity filterOptions = BBCTTestUtil.testMenuItem(this.inst,
                this.activity, R.id.filter_menu, FilterOptions.class);
        final RadioButton filterRadioButton = (RadioButton) filterOptions
                .findViewById(radioButtonId);

        this.runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                Assert.assertFalse(filterRadioButton.performClick());
            }
        });

        Activity filter = this.inst.waitForMonitorWithTimeout(filterMonitor,
                TIME_OUT);
        Assert.assertNotNull(filter);
        final Button filterOkButton = (Button) filter
                .findViewById(R.id.ok_button);

        filterInput.doInput();

        this.runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(filterOkButton.performClick());
            }
        });

        Assert.assertTrue(filter.isFinishing());
        this.inst.waitForIdleSync();
        Assert.assertTrue(filterOptions.isFinishing());

        this.expectedCards = this.filterList(this.allCards, filterPred);
        BBCTTestUtil.assertListViewContainsItems(this.inst, this.expectedCards,
                this.listView);
    }

    private List<BaseballCard> filterList(List<BaseballCard> list,
            Predicate<BaseballCard> pred) {
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
    private Solo solo = null;
    private Instrumentation inst = null;
    private Activity activity = null;
    private DatabaseUtil dbUtil = null;
    private ListView listView = null;
    private BaseballCard newCard = null;
    private static final int TIME_OUT = 5 * 1000; // 5 seconds
    private static final String TAG = BaseballCardListWithDataTest.class
            .getName();
}
