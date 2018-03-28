package com.attunedlabs.eventframework.retrypolicy.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;
import com.attunedlabs.eventframework.retrypolicy.RetryPolicy;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionRetryPolicy;

/**
 * EventFrameworkRetryPolicyService will filter the failed list and decide the
 * next retryable time for each failed subscriber based on the
 * {@link SubscriptionRetryPolicy}
 * 
 * @author Reactiveworks42
 *
 */
public class EventFrameworkRetryPolicyService {
	final static Logger logger = LoggerFactory.getLogger(EventFrameworkRetryPolicyService.class);

	/**
	 * filterFailedListWithPoilcy will filter the list based on the policy
	 * properties and return the list for republishing the events those who have
	 * passed the test of policy.
	 * 
	 * @param allFailedEventList
	 * @return list for republishing Events.
	 */
	public static List<EventDispatcherTracker> filterFailedListWithPoilcy(
			List<EventDispatcherTracker> allFailedEventList) {
		logger.debug("inside filterFailedListWithPoilcy()...");
		List<EventDispatcherTracker> allFilterPolicyList = new ArrayList<>();
		for (int i = 0; i < allFailedEventList.size(); i++) {
			EventDispatcherTracker eventDispatcherTracker = allFailedEventList.get(i);
			// based on policy requirements remove the list from retrying
			switch (eventDispatcherTracker.getStatus()) {
			case EventTrackerTableConstants.STATUS_NEW:
				if (normalListMeetingPolicyRequirements(eventDispatcherTracker))
					allFilterPolicyList.add(eventDispatcherTracker);
				break;
			case EventTrackerTableConstants.STATUS_IN_PROCESS:
				if (normalListMeetingPolicyRequirements(eventDispatcherTracker))
					allFilterPolicyList.add(eventDispatcherTracker);
				break;
			case EventTrackerTableConstants.STATUS_RETRY_IN_PROCESS:
				if (normalListMeetingPolicyRequirements(eventDispatcherTracker))
					allFilterPolicyList.add(eventDispatcherTracker);
				break;
			case EventTrackerTableConstants.STATUS_FAILED:
				if (failedListMeetingPolicyRequirements(eventDispatcherTracker))
					allFilterPolicyList.add(eventDispatcherTracker);
				break;
			case EventTrackerTableConstants.STATUS_RETRY_FAILED:
				if (failedListMeetingPolicyRequirements(eventDispatcherTracker))
					allFilterPolicyList.add(eventDispatcherTracker);
				break;
			default:
				logger.error("Invaild Status Present will be removed..." + eventDispatcherTracker.getStatus());
				allFilterPolicyList.add(eventDispatcherTracker);
				break;
			}

		}
		logger.debug("failedListMeetingPolicyRequirements " + allFailedEventList);
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
	 * @param eventDispatcherTracker
	 * @return canRetryBePerformed true or false
	 */
	private static boolean normalListMeetingPolicyRequirements(EventDispatcherTracker eventDispatcherTracker) {
		logger.debug("inside normalListMeetingPolicyRequirements()...for request: " + eventDispatcherTracker);
		boolean retryEventDispatcherTracker = false;
		Date eventCreatedDTM = eventDispatcherTracker.getEventCreatedDTM();
		Integer retryCount = eventDispatcherTracker.getRetryCount();

		int normalRetryCount = RetryPolicy.getNormalRetryCount();

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
	 * @param eventDispatcherTracker
	 * @return canRetryBePerformed true or false
	 */
	private static boolean failedListMeetingPolicyRequirements(EventDispatcherTracker eventDispatcherTracker) {
		logger.debug("inside failedListMeetingPolicyRequirements()...for request: " + eventDispatcherTracker);
		boolean retryEventDispatcherTracker = false;
		Date eventCreatedDTM = eventDispatcherTracker.getEventCreatedDTM();
		Integer retryCount = eventDispatcherTracker.getRetryCount();

		int failedMaximumRetryCount = RetryPolicy.getFailedMaximumRetryCount();
		logger.debug("retryCount : " + retryCount + " max failedretryCount " + failedMaximumRetryCount + " reqId: "
				+ eventDispatcherTracker.getRequestId());

		if (failedMaximumRetryCount == -1)// continue
			retryEventDispatcherTracker = isTimeToRetry(eventCreatedDTM, retryCount);
		else if (failedMaximumRetryCount == 0)// no retry
			retryEventDispatcherTracker = false;
		else if (failedMaximumRetryCount > retryCount)// limit on retry
			retryEventDispatcherTracker = isTimeToRetry(eventCreatedDTM, retryCount);
		logger.debug("retryEventDispatcherTracker : " + retryEventDispatcherTracker);
		return retryEventDispatcherTracker;
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
	 * @return either to retry or not based on retryCount.
	 */

	private static boolean isTimeToRetry(Date eventCreatedDTM, Integer retryCount) {
		// calculation is done against current system time.
		long currentTime = System.currentTimeMillis();
		long eventCreatedTime = eventCreatedDTM.getTime();

		int failedRetryInterval = RetryPolicy.getFailedRetryInterval();
		int failedRetryIntervalMultiplier = RetryPolicy.getFailedRetryIntervalMultiplier();

		// if multiplier is 0 then use 1
		if (failedRetryIntervalMultiplier == 0)
			failedRetryIntervalMultiplier = 1;
		logger.debug("Date considering FailedRetryInterval..."
				+ getAfterIntervalDateInstance(failedRetryInterval, eventCreatedTime));

		if (retryCount == 0)
			return Math.abs(currentTime - eventCreatedTime) >= getTimeIntervalOnUnit(failedRetryInterval);
		else if (retryCount > 0) {
			long calculatedTimeUsingMultiplier = 0;
			if (failedRetryIntervalMultiplier == 1)
				calculatedTimeUsingMultiplier = getTimeComputedForMultiplierOne(failedRetryInterval, retryCount);
			else {
				int computefailedRetryIntervalOnMultiplier = getTimeComputedOnMultiplier(failedRetryIntervalMultiplier,
						failedRetryInterval, retryCount);
				logger.debug("retry time : " + Math.abs(currentTime - eventCreatedTime) + " time computed : "
						+ getTimeIntervalOnUnit(computefailedRetryIntervalOnMultiplier));
				// e.g: - 480000 + (480000/2) to get previous time added in
				// order to
				// trigger from created time
				calculatedTimeUsingMultiplier = getTimeIntervalOnUnit(computefailedRetryIntervalOnMultiplier)
						+ (getTimeIntervalOnUnit(computefailedRetryIntervalOnMultiplier)
								/ failedRetryIntervalMultiplier);
			}
			return Math.abs(currentTime - eventCreatedTime) >= calculatedTimeUsingMultiplier;

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
	 * @return timeToRetry afterConverting last sequence digit based on
	 *         timeunit.
	 */
	private static long getTimeComputedForMultiplierOne(int failedRetryInterval, Integer retryCount) {
		int temp = 0;
		for (int i = 0; i < retryCount; i++) {
			++temp;
			if ((getTimeIntervalOnUnit(temp) + getTimeIntervalOnUnit(failedRetryInterval)) > getTimeIntervalOnUnit(
					RetryPolicy.getFailedMaximumRetryInterval())) {
				return getTimeIntervalOnUnit(RetryPolicy.getFailedMaximumRetryInterval());
			}
		}
		return getTimeIntervalOnUnit(temp) + getTimeIntervalOnUnit(failedRetryInterval);
	}

	/**
	 * time computed based on multiplier,interval and retryCount to get sequence
	 * of time interval.</br>
	 * for e.g :- failedRetryIntervalMultiplier = 2 , failedRetryInterval = 1
	 * ,retryCount = 5</br>
	 * sequence will generated as :- 1,2,4,8,16 </br>
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
			Integer retryCount) {
		int temp = 1;
		for (int i = 0; i < retryCount; i++) {
			if (i == 0)
				temp = temp * failedRetryIntervalMultiplier * failedRetryInterval;
			else
				temp = temp * failedRetryIntervalMultiplier;
			// instead of calculating further stop if maxRetryIntervalExceeds
			if (getTimeIntervalOnUnit(temp) > getTimeIntervalOnUnit(RetryPolicy.getFailedMaximumRetryInterval()))
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
	private static Date getAfterIntervalDateInstance(int timeIntervalAfter, long createdDTM) {
		long specifiedMinutesAfterIntervalTime = 0;
		switch (RetryPolicy.getFailedTimeIntervalUnit().toUpperCase()) {
		case RetryPolicy.TIMEUNIT_HOURS:
			specifiedMinutesAfterIntervalTime = createdDTM + TimeUnit.HOURS.toMillis(timeIntervalAfter);
			break;
		case RetryPolicy.TIMEUNIT_MINUTES:
			specifiedMinutesAfterIntervalTime = createdDTM + TimeUnit.MINUTES.toMillis(timeIntervalAfter);
			break;
		case RetryPolicy.TIMEUNIT_SECONDS:
			specifiedMinutesAfterIntervalTime = createdDTM + TimeUnit.SECONDS.toMillis(timeIntervalAfter);
			break;
		case RetryPolicy.TIMEUNIT_MILLSECONDS:
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
	private static long getTimeIntervalOnUnit(int timeInterval) {
		switch (RetryPolicy.getFailedTimeIntervalUnit().toUpperCase()) {
		case RetryPolicy.TIMEUNIT_HOURS:
			return TimeUnit.HOURS.toMillis(timeInterval);
		case RetryPolicy.TIMEUNIT_MINUTES:
			return TimeUnit.MINUTES.toMillis(timeInterval);
		case RetryPolicy.TIMEUNIT_SECONDS:
			return TimeUnit.SECONDS.toMillis(timeInterval);
		case RetryPolicy.TIMEUNIT_MILLSECONDS:
			return TimeUnit.MILLISECONDS.toMillis(timeInterval);
		default:
			return TimeUnit.SECONDS.toMillis(timeInterval);
		}
	}

}
