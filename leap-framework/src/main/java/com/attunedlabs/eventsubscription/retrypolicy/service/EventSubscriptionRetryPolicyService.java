package com.attunedlabs.eventsubscription.retrypolicy.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionRetryPolicy;
import com.attunedlabs.eventsubscription.util.EventSubscriptionTrackerConstants;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;

/**
 * 
 * @author Reactiveworks42
 *
 */
public class EventSubscriptionRetryPolicyService {
	final static Logger logger = LoggerFactory.getLogger(EventSubscriptionRetryPolicyService.class);

	/**
	 * filterFailedListWithPoilcy will filter the list based on the policy
	 * properties and return the list for republishing the events those who have
	 * passed the test of policy.
	 * 
	 * @param allFailedEventList
	 * @param jsonObject
	 * @return list for failed .
	 */
	public static List<EventSubscriptionTracker> filterFailedListWithPoilcy(
			List<EventSubscriptionTracker> allFailedEventList, JSONObject retryConfigurationJSON) {
		logger.debug("inside filterFailedListWithPoilcy()..." + allFailedEventList);
		List<EventSubscriptionTracker> allFilterPolicyList = new ArrayList<>();
		for (int i = 0; i < allFailedEventList.size(); i++) {
			EventSubscriptionTracker eventSubscriptionTracker = allFailedEventList.get(i);
			// based on policy requirements remove the list from retrying
			switch (eventSubscriptionTracker.getStatus()) {
			case EventSubscriptionTrackerConstants.STATUS_NEW:
				if (normalListMeetingPolicyRequirements(eventSubscriptionTracker, retryConfigurationJSON))
					allFilterPolicyList.add(eventSubscriptionTracker);
				break;
			case EventSubscriptionTrackerConstants.STATUS_IN_PROCESS:
				if (normalListMeetingPolicyRequirements(eventSubscriptionTracker, retryConfigurationJSON))
					allFilterPolicyList.add(eventSubscriptionTracker);
				break;
			case EventSubscriptionTrackerConstants.STATUS_RETRY_IN_PROCESS:
				if (normalListMeetingPolicyRequirements(eventSubscriptionTracker, retryConfigurationJSON))
					allFilterPolicyList.add(eventSubscriptionTracker);
				break;
			case EventSubscriptionTrackerConstants.STATUS_FAILED:
				if (failedListMeetingPolicyRequirements(eventSubscriptionTracker, retryConfigurationJSON))
					allFilterPolicyList.add(eventSubscriptionTracker);
				break;
			case EventSubscriptionTrackerConstants.STATUS_RETRY_FAILED:
				if (failedListMeetingPolicyRequirements(eventSubscriptionTracker, retryConfigurationJSON))
					allFilterPolicyList.add(eventSubscriptionTracker);
				break;
			default:
				logger.error("Invaild Status Present subscription will not be considered for retry..."
						+ eventSubscriptionTracker.getStatus());
				break;
			}

		}
		logger.debug("failedListMeetingPolicyRequirements " + allFilterPolicyList);
		return allFilterPolicyList;

	}

	/**
	 * normalListMeetingPolicyRequirements will behave based on normalRetryCount
	 * specified.</br>
	 * 1)for retry count -1 retry will be continuous.</br>
	 * 2) for retry count 0 retry will be never happen. .</br>
	 * 3)for retry count less than or equals max retry count retry will be
	 * performed orElse no retry will be performed.
	 * 
	 * @param eventSubscriptionTracker
	 * @param retryConfigurationJSON
	 * @return canRetryBePerformed true or false
	 */
	private static boolean normalListMeetingPolicyRequirements(EventSubscriptionTracker eventSubscriptionTracker,
			JSONObject retryConfigurationJSON) {
		logger.debug("inside normalListMeetingPolicyRequirements()...for request: " + eventSubscriptionTracker);
		boolean retryEventDispatcherTracker = false;
		Integer retryCount = eventSubscriptionTracker.getRetryCount();

		int normalRetryCount = SubscriptionRetryPolicy.getRetryCount(retryConfigurationJSON);

		if (retryCount == null)
			retryCount = 0;

		if (normalRetryCount == -1)// continue
			retryEventDispatcherTracker = true;
		else if (normalRetryCount == 0)// no retry
			retryEventDispatcherTracker = false;
		else if (normalRetryCount > retryCount)// limit on retry
			retryEventDispatcherTracker = true;

		return retryEventDispatcherTracker;
	}

	/**
	 * failedListMeetingPolicyRequirements will behave based on
	 * failedMaximumRetryCount specified.</br>
	 * 1)for retry count -1 retry will be continuous.</br>
	 * 2) for retry count 0 retry will be never happen. .</br>
	 * 3)for retry count less than or equals max retry count retry will be
	 * performed orElse no retry will be performed.
	 * 
	 * @param eventSubscriptionTracker
	 * @param retryConfigurationJSON
	 * @return canRetryBePerformed true or false
	 */
	private static boolean failedListMeetingPolicyRequirements(EventSubscriptionTracker eventSubscriptionTracker,
			JSONObject retryConfigurationJSON) {
		logger.debug("inside failedListMeetingPolicyRequirements()...for request: " + eventSubscriptionTracker);
		boolean retryEventSubscriptionTrackerr = false;
		Date eventCreatedDTM = eventSubscriptionTracker.getEventFetchedDTM();
		Integer retryCount = eventSubscriptionTracker.getRetryCount();

		int failedMaximumRetryCount = SubscriptionRetryPolicy.getRetryCount(retryConfigurationJSON);

		if (failedMaximumRetryCount == -1)// continue
			retryEventSubscriptionTrackerr = isTimeToRetry(eventCreatedDTM, retryCount, retryConfigurationJSON);
		else if (failedMaximumRetryCount == 0)// no retry
			retryEventSubscriptionTrackerr = false;
		else if (failedMaximumRetryCount > retryCount)// limit on retry
			retryEventSubscriptionTrackerr = isTimeToRetry(eventCreatedDTM, retryCount, retryConfigurationJSON);
		logger.debug("retryEventDispatcherTracker : " + retryEventSubscriptionTrackerr);

		return retryEventSubscriptionTrackerr;
	}

	/**
	 * isTimeToRetry will perform calculations considering all the properties
	 * mentioned in policy.</br>
	 * 1)if retryCount is 0 then calculation done based on timeUnit &
	 * initialRetryInterval specified in properties or default values will be
	 * used formula to calculate isTimeToRetry for retry count 0 is :-
	 * 
	 * <pre>
	 * {@code Math.abs(currentSystemTime - eventCreatedTime) >= getTimeIntervalOnUnit(failedRetryInterval);}
	 * </pre>
	 * 
	 * 2)if retryCount is greater than 0 at that time time will be computes for
	 * retry by {@link #getTimeComputedOnMultiplier}.
	 * 
	 * <pre>
	 * {@code Math.abs(currentSystemTime - eventCreatedTime) >= getTimeIntervalOnUnit(computefailedRetryIntervalOnMultiplier)
	 *                          + (getTimeIntervalOnUnit(computefailedRetryIntervalOnMultiplier) / failedRetryIntervalMultiplier);}
	 * </pre>
	 * 
	 * @param eventCreatedDTM
	 *            event creation time.
	 * @param retryCount
	 *            retryTeempt for particular eventList.
	 * @param retryConfigurationJSON
	 * @return either to retry or not based on retryCount.
	 */

	private static boolean isTimeToRetry(Date eventCreatedDTM, Integer retryCount, JSONObject retryConfigurationJSON) {
		// calculation is done against current system time.
		long currentTime = System.currentTimeMillis();
		long eventCreatedTime = eventCreatedDTM.getTime();

		int failedRetryInterval = SubscriptionRetryPolicy.getRetryInterval(retryConfigurationJSON);
		int failedRetryIntervalMultiplier = SubscriptionRetryPolicy.getRetryIntervalMultiplier(retryConfigurationJSON);

		// if multiplier is 0 then use 1
		if (failedRetryIntervalMultiplier == 0)
			failedRetryIntervalMultiplier = 1;

		if (retryCount == 0) {
			long timeComputed = getTimeIntervalOnUnit(failedRetryInterval, retryConfigurationJSON);
			logger.debug("Current RETRY COUNT : " + retryCount);
			logger.debug(
					"Retry time : " + Math.abs(currentTime - eventCreatedTime) + " time computed : " + timeComputed);
			if (Math.abs(currentTime - eventCreatedTime) >= timeComputed) {
				logger.debug("Next Retry DateTime : " + new Date(timeComputed + eventCreatedTime));
				return true;
			}
		} else if (retryCount > 0)

		{
			long calculatedTimeUsingMultiplier = 0;
			if (failedRetryIntervalMultiplier == 1)
				calculatedTimeUsingMultiplier = getTimeComputedForMultiplierOne(failedRetryInterval, retryCount,
						retryConfigurationJSON);
			else {
				int computefailedRetryIntervalOnMultiplier = getTimeComputedOnMultiplier(failedRetryIntervalMultiplier,
						failedRetryInterval, retryCount, retryConfigurationJSON);
				// e.g: - 480000 + (480000/2) to get previous time added in
				// order to
				// trigger from created time
				calculatedTimeUsingMultiplier = getTimeIntervalOnUnit(computefailedRetryIntervalOnMultiplier,
						retryConfigurationJSON)
						+ (getTimeIntervalOnUnit(computefailedRetryIntervalOnMultiplier, retryConfigurationJSON)
								/ failedRetryIntervalMultiplier);
			}

			logger.debug("Current RETRY COUNT : " + retryCount);
			logger.debug("Retry time : " + Math.abs(currentTime - eventCreatedTime) + " time computed : "
					+ calculatedTimeUsingMultiplier);
			if (Math.abs(currentTime - eventCreatedTime) >= calculatedTimeUsingMultiplier) {
				logger.debug("Next Retry DateTime : " + new Date(calculatedTimeUsingMultiplier + eventCreatedTime));
				return true;
			}

		}
		return false;

	}

	/**
	 * time computed when multiplier is 1 based on failedRetryInterval and
	 * retryCount to get sequence of time interval.</br>
	 * for e.g :- failedRetryIntervalMultiplier = 1 , failedRetryInterval = 2
	 * ,retryCount = 5</br>
	 * sequence will generated as :- 2,3,4,5,6 </br>
	 * If the IntervalExceeds the max interval than stop the computation and
	 * return the result.
	 * 
	 * @param failedRetryInterval
	 * @param retryCount
	 * @param retryConfigurationJSON
	 * @return timeToRetry afterConverting last sequence digit based on
	 *         timeunit.
	 */
	private static long getTimeComputedForMultiplierOne(int failedRetryInterval, Integer retryCount,
			JSONObject retryConfigurationJSON) {
		int temp = 0;
		for (int i = 0; i < retryCount; i++) {
			++temp;
			if ((getTimeIntervalOnUnit(temp, retryConfigurationJSON)
					+ getTimeIntervalOnUnit(failedRetryInterval, retryConfigurationJSON)) > getTimeIntervalOnUnit(
							SubscriptionRetryPolicy.getMaximumRetryInterval(retryConfigurationJSON),
							retryConfigurationJSON)) {
				return getTimeIntervalOnUnit(SubscriptionRetryPolicy.getMaximumRetryInterval(retryConfigurationJSON),
						retryConfigurationJSON);
			}
		}
		return getTimeIntervalOnUnit(temp, retryConfigurationJSON)
				+ getTimeIntervalOnUnit(failedRetryInterval, retryConfigurationJSON);
	}

	/**
	 * time computed based on multiplier,interval and retryCount to get sequence
	 * of time interval.</br>
	 * for e.g :- failedRetryIntervalMultiplier = 2 , failedRetryInterval = 1
	 * ,retryCount = 5</br>
	 * sequence on timeUnit will generated as :- 1,2,4,8,16 </br>
	 * If the IntervalExceeds the max interval than stop the computation and
	 * return the result.
	 * 
	 * @param failedRetryIntervalMultiplier
	 * @param failedRetryInterval
	 * @param retryCount
	 * @return timeToRetry afterConverting last sequence digit based on
	 *         timeunit.
	 */
	private static int getTimeComputedOnMultiplier(int failedRetryIntervalMultiplier, int failedRetryInterval,
			Integer retryCount, JSONObject retryConfigurationJSON) {
		int temp = 1;
		for (int i = 0; i < retryCount; i++) {
			if (i == 0)
				temp = temp * failedRetryIntervalMultiplier * failedRetryInterval;
			else
				temp = temp * failedRetryIntervalMultiplier;
			// instead of calculating further stop if maxRetryIntervalExceeds
			if (getTimeIntervalOnUnit(temp, retryConfigurationJSON) > getTimeIntervalOnUnit(
					SubscriptionRetryPolicy.getMaximumRetryInterval(retryConfigurationJSON), retryConfigurationJSON))
				return temp;

		}
		return temp;
	}

	/**
	 * returning the instance of Date but instance will contain specified
	 * interval after the created Date instance of event.
	 * 
	 * @param timeIntervalAfter
	 * @return date instance
	 */
	private static Date getAfterIntervalDateInstance(long timeIntervalAfter, long createdDTM,
			JSONObject retryConfigurationJSON) {
		long specifiedMinutesAfterIntervalTime = 0;
		switch (SubscriptionRetryPolicy.getTimeIntervalUnit(retryConfigurationJSON).toUpperCase()) {
		case SubscriptionRetryPolicy.TIMEUNIT_HOURS:
			specifiedMinutesAfterIntervalTime = createdDTM + TimeUnit.HOURS.toMillis(timeIntervalAfter);
			break;
		case SubscriptionRetryPolicy.TIMEUNIT_MINUTES:
			specifiedMinutesAfterIntervalTime = createdDTM + TimeUnit.MINUTES.toMillis(timeIntervalAfter);
			break;
		case SubscriptionRetryPolicy.TIMEUNIT_SECONDS:
			specifiedMinutesAfterIntervalTime = createdDTM + TimeUnit.SECONDS.toMillis(timeIntervalAfter);
			break;
		case SubscriptionRetryPolicy.TIMEUNIT_MILLSECONDS:
			specifiedMinutesAfterIntervalTime = createdDTM + timeIntervalAfter;
			break;
		default:
			// default will be considered based on failedTimeIntervalUnit.
			specifiedMinutesAfterIntervalTime = createdDTM + TimeUnit.MINUTES.toMillis(timeIntervalAfter);
			break;
		}

		Date calculatedMinutesBeforeDate = new Date(specifiedMinutesAfterIntervalTime);
		return calculatedMinutesBeforeDate;
	}

	/**
	 * timeInterval returned based on the TimeIntervalUnit.
	 * 
	 * @param timeInterval
	 * @return timeInterval returned based on the TimeIntervalUnit.
	 */
	private static long getTimeIntervalOnUnit(int timeInterval, JSONObject retryConfigurationJSON) {
		switch (SubscriptionRetryPolicy.getTimeIntervalUnit(retryConfigurationJSON).toUpperCase()) {
		case SubscriptionRetryPolicy.TIMEUNIT_HOURS:
			return TimeUnit.HOURS.toMillis(timeInterval);
		case SubscriptionRetryPolicy.TIMEUNIT_MINUTES:
			return TimeUnit.MINUTES.toMillis(timeInterval);
		case SubscriptionRetryPolicy.TIMEUNIT_SECONDS:
			return TimeUnit.SECONDS.toMillis(timeInterval);
		case SubscriptionRetryPolicy.TIMEUNIT_MILLSECONDS:
			return TimeUnit.MILLISECONDS.toMillis(timeInterval);
		default:
			return TimeUnit.SECONDS.toMillis(timeInterval);
		}
	}

}
