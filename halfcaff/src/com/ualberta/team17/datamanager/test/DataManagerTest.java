package com.ualberta.team17.datamanager.test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.R;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.IDataSourceAvailableListener;
import com.ualberta.team17.datamanager.IDataSourceManager;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.LocalDataManager;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.comparators.IdComparator;
import com.ualberta.team17.view.QuestionListActivity;

public class DataManagerTest extends ActivityInstrumentationTestCase2<QuestionListActivity> {
	DataFilter dataFilter;
	IncrementalResult result;
	UserContext userContext;
	LocalDataManager dataManager;
	NetworkDataManager netDataManager;
	DataManager manager;
	QAController controller;
	
	/**
	 * Waits for at least numResults results to arrive in the IncrementalResult before returning, or before 
	 * the maximum amount of time (maxWaitSeconds) has surpassed.
	 *
	 * @param result The IncrementalResult to wait on.
	 * @param numResults The minimum number of results to wait for.
	 * @return True if the results were returned before the max wait time.
	 */
	private boolean waitForResults(final IncrementalResult result, final int numResults) {
		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();

		// If the data source becomes unavailable, signal the condition so we don't wait unnecessarily
		dataManager.addDataSourceAvailableListener(new IDataSourceAvailableListener() {
			@Override
			public void DataSourceAvailable(IDataSourceManager manager) {
				if (!manager.isAvailable()) {
					lock.lock();
					condition.signal();
					lock.unlock();
				}
			}
		});

		result.addObserver(new IIncrementalObserver() {
			@Override
			public void itemsArrived(List<QAModel> item, int index) {
				if (result.getCurrentResults().size() >= numResults) {
					lock.lock();
					condition.signal();
					lock.unlock();
				}
			}
		});

		lock.lock();

		boolean success = false;
		try {
			long maxWaitSeconds = 2;
			success = condition.await(maxWaitSeconds , TimeUnit.SECONDS) && dataManager.isAvailable();
		} catch (InterruptedException e) {

		}

		return success;
	}

	
	public DataManagerTest() {
		super(QuestionListActivity.class);
	}
	
	public static final String TEST_DATA = "[[\"Question\",{\"id\":\"c4ca4238a0b92382dcc509a6f75849b\",\"parent\":\"0\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testBody\",\"title\":\"testTitle\"}],[\"Answer\",{\"id\":\"c81e728d9d4c2f636f67f89cc14862c\",\"parent\":\"c4ca4238a0b92382dcc509a6f75849b\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testReply\"}],[\"Answer\",{\"id\":\"eccbc87e4b5ce2fe28308fd9f2a7baf3\",\"parent\":\"c4ca4238a0b92382dcc509a6f75849b\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testReply2\"}],[\"Comment\",{\"id\":\"a87ff679a2f3e71d9181a67b7542122c\",\"parent\":\"c81e728d9d4c2f636f67f89cc14862c\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"comment!\"}]]";
	
	public void test_DataManager() {
		// User context
		userContext = new UserContext("test_user");
		
		// Make local data manager, and set the data to the test data set
		dataManager = new LocalDataManager(getActivity(), userContext);
		dataManager.writeTestData(TEST_DATA);
		
		// Make net data manager
		Resources resources = getInstrumentation().getTargetContext().getResources();
		netDataManager = new NetworkDataManager(
				resources.getString(R.string.esTestServer),
				resources.getString(R.string.esTestIndex));
		
		// Make the manager
		manager = new DataManager(getActivity(), dataManager, netDataManager);
		
		// Make the controller
		controller = new QAController(manager);
		
		// Wait for the local data to be loaded
		dataManager.waitForData();
		
		// Do a query
		DataFilter f = new DataFilter();
		f.addFieldFilter(AuthoredTextItem.FIELD_BODY, "testReply", FilterComparison.EQUALS);
		IncrementalResult r = controller.getObjects(f, new IdComparator());
		
		// Get the results
		assertTrue(waitForResults(r, 1));
		assertEquals("testReply", r.getCurrentResults().get(0).getField(AuthoredTextItem.FIELD_BODY));
	}
}
