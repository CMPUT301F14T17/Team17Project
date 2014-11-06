package com.ualberta.team17.controller;

import java.util.Calendar;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.StoragePolicy;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.comparators.DateComparator;

public class QAController {
	private static QAController mControllerInstance;
	DataManager mDataManager;
	
	/**
	 * Constructor, from a DataManager to act on
	 * @param model
	 */
	public QAController(DataManager model) {
		mDataManager = model;
		mControllerInstance = this;
	}
	
	public static QAController getInstance() {
		return mControllerInstance;
	}
	
	/**
	 * Log in using a given user context
	 * @param ctx
	 */
	public void login(UserContext ctx) {
		mDataManager.setUserContext(ctx);
	}
	
	/**
	 * Access existing objects via an arbitrary Filter & Sort
	 * @param filter
	 * @param sort
	 * @return An IncrementalResult
	 */
	public IncrementalResult getObjects(DataFilter filter, IItemComparator sort) {
		return mDataManager.doQuery(filter, sort);
	}
	
	/**
	 * Get all of the children of a given item
	 * @param question
	 * @param sort
	 * @return An incremental result that will be populated with the results
	 */
	public IncrementalResult getChildren(QAModel item, IItemComparator sort) {
		DataFilter filter = new DataFilter();
		filter.addFieldFilter(AuthoredItem.FIELD_PARENT, item.getUniqueId().toString(), FilterComparison.EQUALS);
		return mDataManager.doQuery(filter, sort);
	}
	
	/**
	 * Gets the current users favorites
	 * @return An incremental result that will be populated with the users favorites.
	 */
	public IncrementalResult getFavorites() {
		return mDataManager.doQuery(
				mDataManager.getUserContext().getFavorites(), new DateComparator());
	}
	
	/**
	 * Adds an item to the user's favorites
	 * @param item The item to favorite
	 */
	public void addFavorite(QAModel item) {
		mDataManager.favoriteItem(item);
	}
	
	/**
	 * Get the recently items that have been marked as recently viewed
	 * most recently with QAController::markRecentlyViewed
	 * @return An IncrementalResult that will be populated with the recently viewed items
	 */
	public IncrementalResult getRecentItems() {
		return mDataManager.doQuery(
				mDataManager.getUserContext().getRecentItems(), 
				new DateComparator());
	}
	
	/**
	 * Mark an item as recently viewed
	 * Mainly to be used for questions.
	 * @param item
	 */
	public void markRecentlyViewed(QAModel item) {
		mDataManager.markRecentlyViewed(item.getUniqueId());
	}
	
	/**
	 * Create a question object, from a creator context, title, and body
	 * @param title
	 * @param body
	 * @return The item created
	 */
	public QuestionItem createQuestion(String title, String body) {
		UserContext creator = mDataManager.getUserContext();
		QuestionItem item = new QuestionItem(new UniqueId(creator), null, creator.getUserName(), Calendar.getInstance().getTime(), body, 0, title);
		item.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(item);
		return item;
	}
	
	/**
	 * Create an answer object to a given question
	 * @param parent
	 * @param body
	 * @return
	 */
	public AnswerItem createAnswer(QuestionItem parent, String body) {
		UserContext creator = mDataManager.getUserContext();
		AnswerItem item = new AnswerItem(new UniqueId(creator), parent.getUniqueId(), creator.getUserName(), Calendar.getInstance().getTime(), body, 0);
		item.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(item);
		return item;	
	}
	
	/**
	 * Create an attachment
	 * @param parent
	 * @return
	 */
	public AttachmentItem createAttachment(QuestionItem parent) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Create a comment
	 * @param parent
	 * @param body
	 * @return
	 */
	public CommentItem createComment(AuthoredTextItem parent, String body) {
		UserContext creator = mDataManager.getUserContext();
		CommentItem item = new CommentItem(new UniqueId(creator), parent.getUniqueId(), creator.getUserName(), Calendar.getInstance().getTime(), body, 0);
		item.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(item);
		return item;
	}
}
